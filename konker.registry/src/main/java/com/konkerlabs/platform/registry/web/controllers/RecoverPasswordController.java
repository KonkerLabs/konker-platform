package com.konkerlabs.platform.registry.web.controllers;

import java.io.*;
import java.net.URLEncoder;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.mail.MessagingException;

import com.konkerlabs.platform.registry.web.forms.UserForm;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.konkerlabs.platform.registry.business.model.Token;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.services.api.EmailService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.TokenService;
import com.konkerlabs.platform.registry.business.services.api.UserService;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;


@Controller()
@Scope("request")
@RequestMapping("/recoverpassword")
public class RecoverPasswordController implements ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecoverPasswordController.class);

    private static Config config = ConfigFactory.load().getConfig("email");
    private static final Config recaptchaConfig = ConfigFactory.load().getConfig("recaptcha");
    private static final String secretKey = recaptchaConfig.getString("secretKey");
    private static final String siteKey = recaptchaConfig.getString("siteKey");
    private static final String host = recaptchaConfig.getString("host");
    private static final String URL = config.getString("baseurl");

    public enum Messages {
        USER_DOES_NOT_EXIST("controller.recover.user.does.not.exist");

        public String getCode() {
            return code;
        }

        private String code;

        Messages(String code) {
            this.code = code;
        }
    }

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserService userService;

    @Autowired
    private TokenService tokenService;

    private ApplicationContext applicationContext;

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView recoveryPasswordPage() {
        return new ModelAndView("recover-password")
                .addObject("siteKey", siteKey);
    }

    @RequestMapping(value = "/email", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody Boolean sendEmailRecover(@RequestBody String body, Locale locale) {
        Map<String, Object> requestMap = Collections.emptyMap();
        Map recaptchaValidationMap = Collections.emptyMap();
        try {
            requestMap = new ObjectMapper().readValue(body, HashMap.class);
            String recaptchaResponse = (String) requestMap.get("recaptcha");
            recaptchaValidationMap = getRecaptchaValidationMap(
                    secretKey, recaptchaResponse , host);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!recaptchaValidationMap.isEmpty() && !requestMap.isEmpty() && (Boolean) recaptchaValidationMap.get("success")) {
            try {
                String email = (String) requestMap.get("email");
                if (!Optional.ofNullable(email).isPresent()) {
                    return Boolean.FALSE;
                }

                ServiceResponse<User> response = userService.findByEmail(email);
                User user = response.getResult();
                if (!Optional.ofNullable(user).isPresent()) {
                    return Boolean.FALSE;
                }

                ServiceResponse<String> responseToken = tokenService.generateToken(TokenService.Purpose.RESET_PASSWORD, user, Duration.ofMinutes(60));

                Map<String, Object> templateParam = new HashMap<>();
                templateParam.put("link", URL.concat("recoverpassword/").concat(responseToken.getResult()));
                templateParam.put("name", "Konker Labs");
                templateParam.put("subscriptionDate", new Date());
                templateParam.put("hobbies", Arrays.asList("Cinema", "Sports"));

                emailService.send("no-reply@konkerlab.com",
                        Collections.singletonList(user),
                        Collections.emptyList(),
                        "Recover Password",
                        "email-recover-pass",
                        templateParam,
                        locale);
                return Boolean.TRUE;
            } catch (MessagingException e) {
                LOGGER.equals(e);
                return Boolean.FALSE;
            }
        } else {
            return Boolean.FALSE;
        }
    }

    @RequestMapping(value = "/{token}", method = RequestMethod.GET)
    public ModelAndView showResetPage(@PathVariable("token") String token,
                                      Locale locale) {
        ServiceResponse<Token> serviceResponse = tokenService.getToken(token);

        if (!Optional.ofNullable(serviceResponse).isPresent() ||
                !Optional.ofNullable(serviceResponse.getResult()).isPresent()) {

            List<String> messages = serviceResponse.getResponseMessages()
                    .entrySet()
                    .stream()
                    .map(message -> applicationContext.getMessage(message.getKey(), message.getValue(), locale))
                    .collect(Collectors.toList());

            return new ModelAndView("reset-password")
                    .addObject("errors", messages)
                    .addObject("isExpired", true);
        }

        if (serviceResponse.getResult().getIsExpired()) {
            List<String> messages = new ArrayList<>();
            messages.add(applicationContext.getMessage(TokenService.Validations.EXPIRED_TOKEN.getCode(), null, locale));

            return new ModelAndView("reset-password")
                    .addObject("errors", messages)
                    .addObject("isExpired", true);
        }

        ServiceResponse<User> responseUser = userService.findByEmail(serviceResponse.getResult().getUserEmail());

        return new ModelAndView("reset-password")
                .addObject("user", responseUser.getResult());
    }

    @RequestMapping(method = RequestMethod.POST)
    public ModelAndView resetPassword(@ModelAttribute("userForm") UserForm userForm, Locale locale) {
        ServiceResponse<User> response = userService.findByEmail(userForm.getEmail());

        if (!Optional.ofNullable(response.getResult()).isPresent()) {
            List<String> messages = new ArrayList<>();
            messages.add(applicationContext.getMessage(Messages.USER_DOES_NOT_EXIST.getCode(), null, locale));
            return new ModelAndView("reset-password")
                    .addObject("errors", messages);
        }

        User user = response.getResult();

        userService.save(user, user.getPassword(), userForm.getNewPassword(), userForm.getNewPasswordConfirmation());
        tokenService.invalidateToken(userForm.getToken());

        return new ModelAndView("redirect:/login");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private Map<String, Object> getRecaptchaValidationMap(String secret, String response, String host) {
        String charset = java.nio.charset.StandardCharsets.UTF_8.name();
        String url = "https://www.google.com/recaptcha/api/siteverify";
        String query = null;
        try {
            query = String.format("secret=%s&response=%s&remoteip=%s",
                    URLEncoder.encode(secret, charset),
                    URLEncoder.encode(response, charset),
                    URLEncoder.encode(host, charset));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(url + "?" + query);

        HttpResponse httpResponse;
        try {
            httpResponse = client.execute(post);
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(httpResponse.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                result.append(line);
            }

            return new ObjectMapper().readValue(result.toString(), HashMap.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Collections.emptyMap();
    }
}
