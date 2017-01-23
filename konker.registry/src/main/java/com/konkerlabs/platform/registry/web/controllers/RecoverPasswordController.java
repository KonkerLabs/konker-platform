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
import com.konkerlabs.platform.registry.business.services.api.CaptchaService;
import com.konkerlabs.platform.registry.business.services.api.EmailService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.TokenService;
import com.konkerlabs.platform.registry.business.services.api.UserService;
import com.konkerlabs.platform.registry.config.EmailConfig;
import com.konkerlabs.platform.registry.config.RecaptchaConfig;
import com.konkerlabs.platform.registry.web.forms.UserForm;


@Controller()
@Scope("request")
@RequestMapping("/recoverpassword")
@Profile("email")
public class RecoverPasswordController implements ApplicationContextAware {

	private static final Logger LOGGER = LoggerFactory.getLogger(RecoverPasswordController.class);

    public enum Messages {
        USER_DOES_NOT_EXIST("controller.recover.user.does.not.exist"),
        USER_EMAIL_SUBJECT("controller.recover.user.email.subject"),
        USER_CHANGE_PASSWORD_SUCCESS("controller.recover.user.success");

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
    private CaptchaService captchaService;
    
    @Autowired
    private RecaptchaConfig recaptchaConfig;
    
    @Autowired
    private EmailConfig emailConfig;

    private ApplicationContext applicationContext;

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView recoveryPasswordPage() {
        return new ModelAndView("recover-password")
                .addObject("siteKey", recaptchaConfig.getSiteKey());
    }

    @SuppressWarnings("unchecked")
	@RequestMapping(value = "/email", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody Boolean sendEmailRecover(@RequestBody String body, Locale locale) {
    	Map<String, Object> requestMap = Collections.emptyMap();
		Boolean isValidCaptcha = Boolean.FALSE;
    	try {
    		requestMap = new ObjectMapper().readValue(body, HashMap.class);
    		String recaptchaResponse = (String) requestMap.get("recaptcha");
			isValidCaptcha = captchaService.validateCaptcha(
					recaptchaConfig.getSecretKey(), recaptchaResponse , recaptchaConfig.getHost()).getResult();
    	} catch (IOException e) {
    		e.printStackTrace();
    	}

    	if (!requestMap.isEmpty() && isValidCaptcha) {
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

    			ServiceResponse<String> responseToken = tokenService.generateToken(TokenService.Purpose.RESET_PASSWORD, user, Duration.ofMinutes(15));

    			Map<String, Object> templateParam = new HashMap<>();
    			templateParam.put("link", emailConfig.getBaseurl().concat("recoverpassword/").concat(responseToken.getResult()));
    			templateParam.put("name", user.getName());

    			emailService.send(emailConfig.getSender(),
    					Collections.singletonList(user),
    					Collections.emptyList(),
    					applicationContext.getMessage(Messages.USER_EMAIL_SUBJECT.getCode(), null, user.getLanguage().getLocale()),
    					"html/email-recover-pass",
    					templateParam,
    					user.getLanguage().getLocale());
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
    public ModelAndView showResetPage(@PathVariable("token") String token,
                                      Locale locale) {
        ServiceResponse<Token> serviceResponse = tokenService.getToken(token);
        ServiceResponse<Boolean> validToken = tokenService.isValidToken(token);

        if (!Optional.ofNullable(serviceResponse).isPresent() ||
                !Optional.ofNullable(serviceResponse.getResult()).isPresent()) {

            List<String> messages = serviceResponse.getResponseMessages()
                    .entrySet()
                    .stream()
                    .map(message -> applicationContext.getMessage(message.getKey(), message.getValue(), locale))
                    .collect(Collectors.toList());

            return new ModelAndView("reset-password")
		    .addObject("user", User.builder().build())
                    .addObject("errors", messages)
                    .addObject("isExpired", true);
        }

        if (serviceResponse.getResult().getIsExpired() || !validToken.getResult()) {
            List<String> messages = new ArrayList<>();
            messages.add(applicationContext.getMessage(TokenService.Validations.EXPIRED_TOKEN.getCode(), null, locale));

            return new ModelAndView("reset-password")
		    .addObject("user", User.builder().build())
                    .addObject("errors", messages)
                    .addObject("isExpired", true);
        }

        ServiceResponse<User> responseUser = userService.findByEmail(serviceResponse.getResult().getUserEmail());

        return new ModelAndView("reset-password")
                .addObject("user", responseUser.getResult())
                .addObject("token", token);
    }

	@RequestMapping(method = RequestMethod.POST)
	public ModelAndView resetPassword(@ModelAttribute("userForm") UserForm userForm, 
			RedirectAttributes redirectAttributes, 
			Locale locale) {
		ServiceResponse<User> response = userService.findByEmail(userForm.getEmail());

		if (!Optional.ofNullable(response.getResult()).isPresent()) {
			List<String> messages = new ArrayList<>();
			messages.add(applicationContext.getMessage(Messages.USER_DOES_NOT_EXIST.getCode(), null, locale));
			return new ModelAndView("reset-password")
	        		.addObject("errors", messages)
	        		.addObject("user", User.builder().build());
		}

		User user = response.getResult();

		ServiceResponse<User> saveResponse = userService.save(user, userForm.getNewPassword(), userForm.getNewPasswordConfirmation());
		if (!Optional.ofNullable(saveResponse.getResult()).isPresent()) {
			List<String> messages = saveResponse.getResponseMessages()
					.entrySet()
					.stream()
					.map(message -> applicationContext.getMessage(message.getKey(), message.getValue(), locale))
					.collect(Collectors.toList());

			return new ModelAndView("reset-password")
				.addObject("user", user)
				.addObject("errors", messages);
		}

		tokenService.invalidateToken(userForm.getToken());
		
		redirectAttributes.addFlashAttribute("message", applicationContext
				.getMessage(Messages.USER_CHANGE_PASSWORD_SUCCESS.getCode(), null, locale));

		return new ModelAndView("redirect:/login");
	}

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
