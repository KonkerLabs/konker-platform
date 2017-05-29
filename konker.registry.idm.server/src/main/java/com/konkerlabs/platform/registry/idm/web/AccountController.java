package com.konkerlabs.platform.registry.idm.web;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.idm.config.OAuthClientDetailsService;
import com.konkerlabs.platform.registry.idm.domain.repository.OauthClientDetails;
import com.konkerlabs.platform.registry.idm.web.form.OauthClientRegistrationForm;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.stream.Collectors;


@Scope("request")
@Controller
@RequestMapping("/account")
public class AccountController implements ApplicationContextAware {

    @Autowired
    private OAuthClientDetailsService oAuthClientDetailsService;
    private ApplicationContext applicationContext;
    @Autowired
    private Tenant tenant;

    @RequestMapping(value = "/{applicationId}/clients/", method = RequestMethod.GET)
    public ModelAndView index(@PathVariable("applicationId") String applicationId) {
        ServiceResponse<List<OauthClientDetails>> clientList =
                oAuthClientDetailsService.loadClientsByTenant(
                        tenant, Application.builder().name(
                                tenant.getDomainName()
                        ).build());

        ModelAndView view = new ModelAndView(
                String.format("/clients/index", applicationId))
                .addObject("applicationId", tenant.getDomainName())
                .addObject("allClients", clientList.getResult());
        if (clientList.isOk()) {
            view.addObject("clients", clientList.getResult());
        }

        return view;
    }

    @RequestMapping(value = "/{applicationId}/clients/new", method = RequestMethod.GET)
    public ModelAndView newClient(
            @PathVariable("applicationId") String applicationId
    ) {
        ModelAndView view = new ModelAndView("clients/form")
                .addObject("oauthClient", new OauthClientRegistrationForm().toModel())
                .addObject("applicationId", tenant.getDomainName())
                .addObject("action",
                        MessageFormat.format("/account/{0}/clients/saveClient", tenant.getDomainName()));
        return view;
    }

    @RequestMapping(value = "/{applicationId}/clients/{clientId}/show", method = RequestMethod.GET)
    public ModelAndView showClient(
            @PathVariable("applicationId") String applicationId,
            @PathVariable("clientId") String clientId
    ) {

        ServiceResponse<OauthClientDetails> client =
                oAuthClientDetailsService.loadById(clientId);

        ModelAndView view = new ModelAndView("clients/form")
                .addObject("oauthClient", client.getResult())
                .addObject("applicationId", tenant.getDomainName())
                .addObject("action",
                        MessageFormat.format("/account/{0}/clients/saveClient", tenant.getDomainName()));
        return view;
    }

    @RequestMapping(value = "/{applicationId}/clients/saveClient", method = RequestMethod.POST)
    public ModelAndView saveClient(
            @PathVariable("applicationId") String applicationId,
            @ModelAttribute("oauthClient") OauthClientRegistrationForm form,
            RedirectAttributes redirectAttributes, Locale locale
    ) {

        return doSave(
                () -> oAuthClientDetailsService.saveClient(
                        tenant,
                        Application.builder().name(applicationId).build(),
                        form.toModel()),
                Application.builder().name(applicationId).build(),
                form, locale,
                redirectAttributes, "");
    }

    @RequestMapping(value = "/{applicationId}/clients/{clientId}/remove", method = RequestMethod.GET)
    public ModelAndView removeClient(
            @PathVariable("applicationId") String applicationId,
            @PathVariable("clientId") String clientId,
            @ModelAttribute("clientForm") OauthClientRegistrationForm form) {


        ModelAndView view = new ModelAndView("clients/form");
        return view;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private ModelAndView doSave(Supplier<ServiceResponse<OauthClientDetails>> responseSupplier,
                                Application application,
                                OauthClientRegistrationForm registrationForm, Locale locale,
                                RedirectAttributes redirectAttributes, String action) {

        ServiceResponse<OauthClientDetails> serviceResponse = responseSupplier.get();

        if (serviceResponse.getStatus().equals(ServiceResponse.Status.OK)) {
            redirectAttributes.addFlashAttribute("message",
                    applicationContext.getMessage(OAuthClientDetailsService.Messages.CLIENT_REGISTERED_SUCCESSFULLY.getCode(),
                            null, locale));
            return new ModelAndView(MessageFormat.format("redirect:/account/{0}/clients/",
                    application.getName(),
                    serviceResponse.getResult().getClientId()));
        } else {
            List<String> messages = serviceResponse.getResponseMessages()
                    .entrySet().stream()
                    .map(message -> applicationContext.getMessage(message.getKey(), message.getValue(), locale))
                    .collect(Collectors.toList());
            return new ModelAndView("clients/form").addObject("errors", messages)
                    .addObject("oauthClient", new OauthClientRegistrationForm().toModel())
                    .addObject("applicationId", tenant.getDomainName())
                    .addObject("action",
                            MessageFormat.format("/account/{0}/clients/saveClient", tenant.getDomainName()))
                    .addObject("method", action);
        }

    }

}
