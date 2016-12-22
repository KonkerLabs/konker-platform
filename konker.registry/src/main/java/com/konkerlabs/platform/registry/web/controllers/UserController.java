package com.konkerlabs.platform.registry.web.controllers;

import com.amazonaws.services.rds.model.Option;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.model.enumerations.DateFormat;
import com.konkerlabs.platform.registry.business.model.enumerations.Language;
import com.konkerlabs.platform.registry.business.model.enumerations.TimeZone;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.UserService;
import com.konkerlabs.platform.registry.security.TenantUserDetailsService;
import com.konkerlabs.platform.registry.web.converters.utils.ConverterUtils;
import com.konkerlabs.platform.registry.web.forms.UserForm;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;


@Controller()
@Scope("request")
@RequestMapping("/me")
public class UserController implements ApplicationContextAware {


    @Autowired
    private TenantUserDetailsService tenantUserDetailsService;
    @Autowired
    private UserService userService;
    @Autowired
    private ConverterUtils converterUtils;

    private Tenant tenant;
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


    public UserController(TenantUserDetailsService tenantUserDetailsService,
                          UserService userService, Tenant tenant, User user) {
        this.tenantUserDetailsService = tenantUserDetailsService;
        this.tenant = tenant;
        this.user = user;
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    public ModelAndView userPage(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        ModelAndView mv = new ModelAndView("users/form")
                .addObject("user", new UserForm().fillFrom(user))
                .addObject("action", "/me")
                .addObject("dateformats", DateFormat.values())
                .addObject("languages", Language.values())
                .addObject("timezones", TimeZone.values());

        return mv;
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public ModelAndView save(UserForm userForm,
                             RedirectAttributes redirectAttributes,
                             Locale locale) {

        User fromForm = userForm.toModel();
        fromForm.setEmail(this.user.getEmail());
        ServiceResponse<User> serviceResponse =
                userService.save(fromForm,
                        userForm.getOldPassword(),
                        userForm.getNewPassword(),
                        userForm.getNewPasswordConfirmation());

        if (serviceResponse.getStatus().equals(ServiceResponse.Status.OK)) {

            redirectAttributes.addFlashAttribute("message",
                    applicationContext.getMessage(Messages.USER_UPDATED_SUCCESSFULLY.getCode(),
                            null, locale)
            );
            return new ModelAndView("redirect:/me");
        } else {
            List<String> messages = serviceResponse.getResponseMessages()
                    .entrySet().stream()
                    .map(message -> applicationContext.getMessage(message.getKey(), message.getValue(), locale))
                    .collect(Collectors.toList());
            redirectAttributes.addFlashAttribute("errors", messages);
        }
        return new ModelAndView("redirect:/me");
    }


    /*public LocaleResolver getLocaleResolver(HttpServletRequest request, HttpServletResponse response, User loggedUser) {
        LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
        localeResolver.setLocale(request, response,
                new Locale(
                        loggedUser.getLanguage().name(),
                        loggedUser.getZoneId().name())
        );
        return localeResolver;
    }*/
}
