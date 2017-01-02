package com.konkerlabs.platform.registry.web.controllers;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
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
	public String recoveryPasswordPage() {
		return "recover-password";
	}

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody Boolean sendEmailRecover(@RequestBody String body,
    							Locale locale) {
		try {
			HashMap<String, Object> mapEmail = new ObjectMapper().readValue(body, HashMap.class);
			String email = (String) mapEmail.get("email");
			
			if (!Optional.ofNullable(email).isPresent()) {
				return Boolean.FALSE;
			}
			
			ServiceResponse<User> response = userService.findByEmail(email);
			User user = response.getResult();
			if(!Optional.ofNullable(user).isPresent()) {
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
		} catch (MessagingException | IOException e) {
			LOGGER.equals(e);
		}
    	
        return Boolean.TRUE;
    }

	@RequestMapping(value = "/{token}", method = RequestMethod.GET)
    public ModelAndView resetPassword(@PathVariable("token") String token,
                             Locale locale) {
		
		ServiceResponse<Token> serviceResponse = tokenService.getToken(token);
		
		if (!Optional.ofNullable(serviceResponse).isPresent() || 
				!Optional.ofNullable(serviceResponse.getResult()).isPresent() ||
				serviceResponse.getResult().getIsExpired()) {
			
		}
		
		
        return new ModelAndView("redirect:/login");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
