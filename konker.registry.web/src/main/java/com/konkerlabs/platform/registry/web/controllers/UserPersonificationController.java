package com.konkerlabs.platform.registry.web.controllers;

import java.util.Collections;
import java.util.Locale;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.UserService;
import com.konkerlabs.platform.registry.web.forms.UserForm;


@Controller()
@Scope("request")
@RequestMapping("/personification")
public class UserPersonificationController implements ApplicationContextAware {

    @Autowired
    private UserService userService;

    private User user;

    public enum Messages {
        USER_PERSONIFICATED_SUCCESSFULLY("controller.user.personification.success"),
        USER_PERSONIFICATION_NOT_FOUND("controller.recover.user.does.not.exist");

        public String getCode() {
            return code;
        }

        private String code;

        Messages(String code) {
            this.code = code;
        }
    }

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public UserPersonificationController(UserService userService, User user) {
        this.user = user;
    }
    
    @PreAuthorize("hasAuthority('USER_PERSONIFICATION')")
    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView personificationPage(
    		HttpServletRequest request, 
    		HttpServletResponse response, 
    		Locale locale) {
    	
    	ModelAndView mv = new ModelAndView("users/personification")
    			.addObject("user", new UserForm())
    			.addObject("action", "/personification"); 
    	
    	return mv;
    }

    @PreAuthorize("hasAuthority('USER_PERSONIFICATION')")
	@RequestMapping(method = RequestMethod.POST)
	public ModelAndView execute(
			UserForm userForm, 
			RedirectAttributes redirectAttributes, 
			Locale locale) {
		
		ServiceResponse<User> serviceResponse = userService.findByEmail(userForm.getEmail());
		
		if (serviceResponse.isOk() && Optional.ofNullable(serviceResponse.getResult()).isPresent()) {
			Authentication authentication = new UsernamePasswordAuthenticationToken(
					serviceResponse.getResult(), 
					serviceResponse.getResult().getPassword(), 
					serviceResponse.getResult().getAuthorities());
			SecurityContextHolder.getContext().setAuthentication(authentication);
			
			redirectAttributes.addFlashAttribute("message", 
					applicationContext.getMessage(Messages.USER_PERSONIFICATED_SUCCESSFULLY.getCode(), null, locale));
		} else {
			
			redirectAttributes.addFlashAttribute("errors", Collections.singletonList(
					applicationContext.getMessage(Messages.USER_PERSONIFICATION_NOT_FOUND.getCode(), null, locale)));
			return new ModelAndView("redirect:/personification");
		}

		return new ModelAndView("redirect:/");
	}

}
