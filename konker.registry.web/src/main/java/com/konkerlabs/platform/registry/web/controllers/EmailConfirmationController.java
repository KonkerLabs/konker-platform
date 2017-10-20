package com.konkerlabs.platform.registry.web.controllers;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.konkerlabs.platform.registry.business.model.Token;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.services.api.EmailService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.TokenService;
import com.konkerlabs.platform.registry.business.services.api.UserService;
import com.konkerlabs.platform.registry.config.EmailConfig;
import com.konkerlabs.platform.registry.config.RecaptchaConfig;
import com.konkerlabs.platform.registry.web.forms.UserForm;
import com.konkerlabs.platform.registry.web.services.api.CaptchaService;

@Controller()
@Scope("request")
@RequestMapping("/validateemail")
@Profile("email")
public class EmailConfirmationController implements ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailConfirmationController.class);

    public enum Messages {
        USER_DOES_NOT_EXIST("controller.recover.user.does.not.exist"), USER_EMAIL_SUBJECT(
                "controller.recover.user.email.subject"), USER_CHANGE_PASSWORD_SUCCESS(
                        "controller.recover.user.success");

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

    @Autowired
    private EmailConfig emailConfig;

    private ApplicationContext applicationContext;



    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/email", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody Boolean sendEmail(@RequestBody String body, Locale locale) {
        Map<String, Object> requestMap = Collections.emptyMap();
        

        try {
            requestMap = new ObjectMapper().readValue(body, HashMap.class);  

        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        

        if (!requestMap.isEmpty()) {
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
  
                ServiceResponse<String> responseToken = tokenService.generateToken(TokenService.Purpose.VALIDATE_EMAIL,
                        user, Duration.ofMinutes(30));


                Map<String, Object> templateParam = new HashMap<>();
                templateParam.put("link",
                        emailConfig.getBaseurl().concat("validateemail/").concat(responseToken.getResult()));
                templateParam.put("name", Optional.ofNullable(user.getName()).orElse(""));

                
                emailService.send(emailConfig.getSender(), Collections.singletonList(user), Collections.emptyList(),
                        applicationContext.getMessage(Messages.USER_EMAIL_SUBJECT.getCode(), null,
                                user.getLanguage().getLocale()),
                        "html/email-recover-pass", templateParam, user.getLanguage().getLocale());
                return Boolean.TRUE;
            } catch (MessagingException e) {
                LOGGER.warn(e.getLocalizedMessage());
                return Boolean.FALSE;
            }
        } else {
            return Boolean.FALSE;
        }
    }

 
    

    @RequestMapping(value = "/{token}", method = RequestMethod.GET)
    public ModelAndView showEmailValidationPage(@PathVariable("token") String token, Locale locale) {
        ServiceResponse<Token> serviceResponse = tokenService.getToken(token);
        ServiceResponse<Boolean> validToken = tokenService.isValidToken(token);


        if (!Optional.ofNullable(serviceResponse).isPresent()
                || !Optional.ofNullable(serviceResponse.getResult()).isPresent()) {

            List<String> messages = serviceResponse.getResponseMessages().entrySet().stream()
                    .map(message -> applicationContext.getMessage(message.getKey(), message.getValue(), locale))
                    .collect(Collectors.toList());

            return new ModelAndView("validate-email")
            		.addObject("user", User.builder().build())
                    .addObject("errors", messages)
                    .addObject("isExpired", true);
        }


        if (serviceResponse.getResult().getIsExpired() || !validToken.getResult()) {
            List<String> messages = new ArrayList<>();
            messages.add(applicationContext.getMessage(TokenService.Validations.EXPIRED_TOKEN.getCode(), null, locale));

            return new ModelAndView("validate-email")
            		.addObject("user", User.builder().build())
                    .addObject("errors", messages)
                    .addObject("isExpired", true);
        }


        ServiceResponse<User> responseUser = userService.findByEmail(serviceResponse.getResult().getUserEmail());
        
        

        return new ModelAndView("validate-email")
        		.addObject("user", responseUser.getResult())
        		.addObject("token", token);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
