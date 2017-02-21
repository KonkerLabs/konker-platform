package com.konkerlabs.platform.registry.web.controllers;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.model.enumerations.DateFormat;
import com.konkerlabs.platform.registry.business.model.enumerations.Language;
import com.konkerlabs.platform.registry.business.model.enumerations.LogLevel;
import com.konkerlabs.platform.registry.business.model.enumerations.TimeZone;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.TenantService;
import com.konkerlabs.platform.registry.business.services.api.UserService;
import com.konkerlabs.platform.registry.web.forms.UserForm;
import com.konkerlabs.platform.registry.web.services.api.AvatarService;


@Controller()
@Scope("request")
@RequestMapping("/me")
public class UserController implements ApplicationContextAware {

    @Autowired
    private UserService userService;
    @Autowired
    private AvatarService avatarService;
    @Autowired
    private TenantService tenantService;

    private User user;

    public enum Messages {
        USER_UPDATED_SUCCESSFULLY("controller.user.updated.success");

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

    public UserController(UserService userService, User user) {
        this.user = user;
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    public ModelAndView userPage(HttpServletRequest request, HttpServletResponse response, Locale locale) {

        ModelAndView mv = new ModelAndView("users/form")
                .addObject("user", new UserForm().fillFrom(user))
                .addObject("action", "/me")
                .addObject("dateformats", DateFormat.values())
                .addObject("languages", Language.values())
                .addObject("timezones", TimeZone.values())
                .addObject("loglevels", LogLevel.values());

        return mv;
    }

	@RequestMapping(value = "", method = RequestMethod.POST)
	public ModelAndView save(UserForm userForm, RedirectAttributes redirectAttributes, Locale locale) {

		User fromForm = userForm.toModel();
		fromForm.setEmail(this.user.getEmail());
		
		// update avatar
		ServiceResponse<User> avatarServiceResponse = avatarService.updateAvatar(fromForm);
		if (!avatarServiceResponse.isOk()) {
			return redirectErrorMessages(redirectAttributes, locale, avatarServiceResponse);
		}
		
		// update user
		ServiceResponse<User> serviceResponse = userService.save(fromForm, userForm.getOldPassword(),
				userForm.getNewPassword(), userForm.getNewPasswordConfirmation());

		if (!serviceResponse.getStatus().equals(ServiceResponse.Status.OK)) {
			return redirectErrorMessages(redirectAttributes, locale, serviceResponse);
		}

		// update tenant
		LogLevel newLogLevel = userForm.getLogLevel();
		ServiceResponse<Tenant> tenServiceResponse = tenantService.updateLogLevel(this.user.getTenant(),
				newLogLevel);

		if (!tenServiceResponse.getStatus().equals(ServiceResponse.Status.OK)) {
			return redirectErrorMessages(redirectAttributes, locale, tenServiceResponse);
		} else {
			user.getTenant().setLogLevel(newLogLevel);
		}

		// success
		redirectAttributes.addFlashAttribute("message", applicationContext
				.getMessage(Messages.USER_UPDATED_SUCCESSFULLY.getCode(), null, locale));

		return new ModelAndView("redirect:/me");
	}

	private ModelAndView redirectErrorMessages(RedirectAttributes redirectAttributes, Locale locale,
			ServiceResponse<?> serviceResponse) {
		List<String> messages = serviceResponse.getResponseMessages().entrySet().stream()
				.map(message -> applicationContext.getMessage(message.getKey(), message.getValue(), locale))
				.collect(Collectors.toList());
		redirectAttributes.addFlashAttribute("errors", messages);

		return new ModelAndView("redirect:/me");
	}

}
