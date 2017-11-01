package com.konkerlabs.platform.registry.web.controllers;


import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.Token;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.model.User.JobEnum;
import com.konkerlabs.platform.registry.business.model.enumerations.Language;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.TokenService;
import com.konkerlabs.platform.registry.business.services.api.UserService;
import com.konkerlabs.platform.registry.web.forms.UserForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;


@Controller()
@Scope("request")
@RequestMapping("/subscription")
@Profile("email")
public class UserSubscriptionController implements ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserSubscriptionController.class);

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
    private UserService userService;

    @Autowired
    private TokenService tokenService;

    private ApplicationContext applicationContext;
    
    
    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView form(UserForm userForm) {
    	
    	return new ModelAndView("subscription/form")
    			.addObject("user", userForm)
    			.addObject("allJobs", JobEnum.values())
    			.addObject("action", "/subscription");
    }

    @RequestMapping(value = "/successpage", method = RequestMethod.GET)
    public ModelAndView showSuccessPage() {
        UserForm userForm = new UserForm();
    	return new ModelAndView("subscription/success").addObject("user", userForm);
    }

    @RequestMapping(method = RequestMethod.POST)
    public  ModelAndView save(UserForm userForm, RedirectAttributes redirectAttributes, Locale locale) {
    	User user = userForm.toModel();
    	user.setTenant(Tenant.builder().name(userForm.getTenantName()).build());
    	user.setLanguage(Language.valueOf(
    			Optional.ofNullable(locale.toString())
    				.filter(l -> l.startsWith("pt"))
    				.orElse(locale.getLanguage()).toUpperCase()));

        ServiceResponse<User> serviceResponse = userService.createAccount(
        		user, 
        		userForm.getNewPassword(),
        		userForm.getNewPasswordConfirmation());

        if (serviceResponse.isOk()) {        	
        	return new ModelAndView("subscription/successpage")
                    .addObject("user", userForm);
        }else {
        	List<String> messages = serviceResponse.getResponseMessages()
                    .entrySet().stream()
                    .map(message -> applicationContext.getMessage(message.getKey(), message.getValue(), locale))
                    .collect(Collectors.toList());
        	
        	return new ModelAndView("subscription/form")
        			.addObject("user", userForm)
        			.addObject("allJobs", JobEnum.values())
        			.addObject("action", "/subscription")
        			.addObject("errors", messages);
        }

    }

    @RequestMapping(value = "/{token}", method = RequestMethod.GET)
    public ModelAndView showEmailValidationPage(@PathVariable("token") String token, RedirectAttributes redirectAttributes, Locale locale) {
        ServiceResponse<Token> serviceResponse = tokenService.getToken(token);
        ServiceResponse<Boolean> validToken = tokenService.isValidToken(token);


        if (!Optional.ofNullable(serviceResponse).isPresent()
                || !Optional.ofNullable(serviceResponse.getResult()).isPresent()) {

            List<String> messages = serviceResponse.getResponseMessages().entrySet().stream()
                    .map(message -> applicationContext.getMessage(message.getKey(), message.getValue(), locale))
                    .collect(Collectors.toList());

            return form(UserForm.builder().build())
            		.addObject("errors", applicationContext.getMessage(TokenService.Validations.INVALID_EXPIRED_TOKEN.getCode(), null, locale));
        }


        if (serviceResponse.getResult().getIsExpired() || !validToken.getResult()) {
            
            return form(UserForm.builder().build())
            		.addObject("errors", applicationContext.getMessage(TokenService.Validations.INVALID_EXPIRED_TOKEN.getCode(), null, locale));
        }


        ServiceResponse<User> responseUser = userService.findByEmail(serviceResponse.getResult().getUserEmail());
        
        User user = responseUser.getResult();        
        user.setActive(true);
       
        ServiceResponse<User> saveResponse = userService.save(user, "", "");
        
        if (saveResponse.isOk()) {
        	tokenService.invalidateToken(serviceResponse.getResult().getToken());
        }

        redirectAttributes.addFlashAttribute("message", 
        		applicationContext.getMessage(UserService.Messages.USER_ACTIVATED_SUCCESSFULLY.getCode(), null, locale));
        
        return new ModelAndView("redirect:/login");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
