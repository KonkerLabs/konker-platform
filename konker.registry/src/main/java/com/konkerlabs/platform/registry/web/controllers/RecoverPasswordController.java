package com.konkerlabs.platform.registry.web.controllers;

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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.services.api.EmailService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.TokenService;
import com.konkerlabs.platform.registry.business.services.api.UserService;
import com.konkerlabs.platform.registry.web.forms.UserForm;


@Controller()
@Scope("request")
@RequestMapping("/recoverpassword")
public class RecoverPasswordController implements ApplicationContextAware {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RecoverPasswordController.class);

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
    public @ResponseBody Boolean sendEmailRecover(@RequestParam String email, 
    							Locale locale) {

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
    	templateParam.put("link", responseToken.getResult());
    	templateParam.put("name", "Konker Labs");
    	templateParam.put("subscriptionDate", new Date());
    	templateParam.put("hobbies", Arrays.asList("Cinema", "Sports"));
    	
		try {
			emailService.send("no-reply@konkerlab.com", 
					Collections.singletonList(user), 
					Collections.emptyList(), 
					"Recover Password", 
					"email-recover-pass", 
					templateParam, 
					locale);
		} catch (MessagingException e) {
			LOGGER.equals(e);
		}
    	
        return Boolean.TRUE;
    }

    @RequestMapping(method = RequestMethod.POST)
    public ModelAndView resetPassword(UserForm userForm,
                             RedirectAttributes redirectAttributes,
                             Locale locale) {

        
        return new ModelAndView("redirect:/me");
    }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
