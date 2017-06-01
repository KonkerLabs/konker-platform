package com.konkerlabs.platform.registry.idm.web;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.idm.config.OAuthClientDetailsService;
import com.konkerlabs.platform.registry.idm.domain.repository.AccessToken;
import com.konkerlabs.platform.registry.idm.domain.repository.OauthClientDetails;
import com.konkerlabs.platform.registry.idm.web.form.OauthClientRegistrationForm;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/account/{applicationId}/clients")
public class AccountClientsController implements ApplicationContextAware {

    @Autowired
    private OAuthClientDetailsService oAuthClientDetailsService;
    private ApplicationContext applicationContext;
    @Autowired
    private Tenant tenant;


    @PreAuthorize("hasAuthority('LIST_OAUTHCLIENTS_ROOT')")
    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public ModelAndView indexAsRoot(@PathVariable("applicationId") String applicationId) {
        ServiceResponse<List<OauthClientDetails>> clientList =
                oAuthClientDetailsService.loadAllClients(
                        Application.builder().name(
                                applicationId
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

    @PreAuthorize("hasAuthority('LIST_OAUTHCLIENTS')")
    @RequestMapping(value = "/", method = RequestMethod.GET)
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

    @PreAuthorize("hasAuthority('ADD_OAUTHCLIENTS')")
    @RequestMapping(value = "/new", method = RequestMethod.GET)
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


    @PreAuthorize("hasAuthority('SHOW_OAUTHCLIENTS_ROOT')")
    @RequestMapping(value = "/showAll", method = RequestMethod.GET)
    public ModelAndView showClientsAsRoot(
            @PathVariable("applicationId") String applicationId,
            @PathVariable("tokenId") String tokenId
    ) {

        ServiceResponse<AccessToken> token =
                oAuthClientDetailsService.loadTokenByIdAsRoot(tokenId);

        ModelAndView view = new ModelAndView("tokens/form")
                .addObject("oauthToken", token.getResult())
                .addObject("applicationId", tenant.getDomainName());
        return view;
    }


    @PreAuthorize("hasAuthority('ADD_OAUTHCLIENTS')")
    @RequestMapping(value = "/saveClient", method = RequestMethod.POST)
    public ModelAndView saveClient(
            @PathVariable("applicationId") String applicationId,
            @ModelAttribute("oauthClient") OauthClientRegistrationForm form,
            RedirectAttributes redirectAttributes, Locale locale
    ) {

        return doSaveClient(
                () -> oAuthClientDetailsService.saveClient(
                        tenant,
                        Application.builder().name(applicationId).build(),
                        form.toModel()),
                Application.builder().name(applicationId).build(),
                form, locale,
                redirectAttributes, "");
    }

    @PreAuthorize("hasAuthority('ADD_OAUTHCLIENTS_ROOT')")
    @RequestMapping(value = "/saveClientAll", method = RequestMethod.POST)
    public ModelAndView saveClientAsRoot(
            @PathVariable("applicationId") String applicationId,
            @PathVariable("tenantDomainName") String tenantDomainName,
            @ModelAttribute("oauthClient") OauthClientRegistrationForm form,
            RedirectAttributes redirectAttributes, Locale locale
    ) {

        return doSaveClient(
                () -> oAuthClientDetailsService.saveClientAsRoot(
                        tenantDomainName,
                        Application.builder().name(applicationId).build(),
                        form.toModel()),
                Application.builder().name(applicationId).build(),
                form, locale,
                redirectAttributes, "");
    }

    @PreAuthorize("hasAuthority('REMOVE_OAUTHCLIENTS')")
    @RequestMapping(value = "/{clientId}/deleteClient", method = RequestMethod.GET)
    public ModelAndView removeClient(
            @PathVariable("applicationId") String applicationId,
            @PathVariable("clientId") String clientId,
            @ModelAttribute("clientForm") OauthClientRegistrationForm form,
            RedirectAttributes redirectAttributes, Locale locale) {


        return doRemoveClient(
                () -> oAuthClientDetailsService.deleteClient(
                        tenant,
                        clientId),
                Application.builder().name(applicationId).build(),
                form, locale,
                redirectAttributes, "");
    }

    @PreAuthorize("hasAuthority('REMOVE_OAUTHCLIENTS_ROOT')")
    @RequestMapping(value = "/{clientId}/deleteClientAll", method = RequestMethod.DELETE)
    public ModelAndView removeClientAsRoot(
            @PathVariable("applicationId") String applicationId,
            @PathVariable("clientId") String clientId,
            @ModelAttribute("clientForm") OauthClientRegistrationForm form,
            RedirectAttributes redirectAttributes, Locale locale) {

        ServiceResponse<OauthClientDetails> deletionResult =
                oAuthClientDetailsService.deleteClientAsRoot(clientId);

        return doRemoveClient(
                () -> oAuthClientDetailsService.deleteClientAsRoot(
                        clientId),
                Application.builder().name(applicationId).build(),
                form, locale,
                redirectAttributes, "");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private ModelAndView doRemoveClient(Supplier<ServiceResponse<OauthClientDetails>> responseSupplier,
                                        Application application,
                                        OauthClientRegistrationForm registrationForm, Locale locale,
                                        RedirectAttributes redirectAttributes, String action) {

        ServiceResponse<OauthClientDetails> serviceResponse = responseSupplier.get();

        if (serviceResponse.getStatus().equals(ServiceResponse.Status.OK)) {
            redirectAttributes.addFlashAttribute("message",
                    applicationContext.getMessage(OAuthClientDetailsService.Messages.CLIENT_REMOVED_SUCCESSFULLY.getCode(),
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


    private ModelAndView doSaveClient(Supplier<ServiceResponse<OauthClientDetails>> responseSupplier,
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
