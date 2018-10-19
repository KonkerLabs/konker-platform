package com.konkerlabs.platform.registry.idm.web.controllers;

import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.idm.services.OAuth2AccessTokenService;
import com.konkerlabs.platform.registry.idm.services.OAuthClientDetailsService;
import com.konkerlabs.platform.registry.idm.web.form.AccessTokenRegistrationForm;
import com.konkerlabs.platform.registry.idm.web.form.OauthClientRegistrationForm;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
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
@RequestMapping("/idm/{applicationId}/clients")
public class AccountClientsController implements ApplicationContextAware {

    @Autowired
    private OAuthClientDetailsService oAuthClientDetailsService;
    @Autowired
    private OAuth2AccessTokenService oAuth2AccessTokenService;

    private ApplicationContext applicationContext;
    @Autowired
    private Tenant tenant;
    @Autowired
    private User user;
    @Autowired
    private ApplicationService applicationService;

    @PreAuthorize("hasAuthority('LIST_OAUTH')")
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView index(@PathVariable("applicationId") String applicationId) {
        Application application =
                applicationService.getByApplicationName(tenant, applicationId).getResult();

        ServiceResponse<List<OauthClientDetails>> clientList =
                oAuthClientDetailsService.loadClientsByTenant(
                        tenant, application);

        ServiceResponse<List<Application>> allApplications =
                applicationService.findAll(tenant);

        ModelAndView view = new ModelAndView(
                String.format("idm/clients/index", applicationId))
                .addObject("applicationId", tenant.getDomainName())
                .addObject("allClients", clientList.getResult())
                .addObject("allApplications", allApplications.getResult());
        if (clientList.isOk()) {
            view.addObject("clients", clientList.getResult());
        }

        return view;
    }

    @PreAuthorize("hasAuthority('CREATE_OAUTH')")
    @RequestMapping(value = "/new", method = RequestMethod.GET)
    public ModelAndView newClient(
            @PathVariable("applicationId") String applicationId
    ) {
        ModelAndView view = new ModelAndView("idm/clients/form")
                .addObject("oauthClient", new OauthClientRegistrationForm().toModel())
                .addObject("applicationId", tenant.getDomainName())
                .addObject("action",
                        MessageFormat.format("/idm/{0}/clients/saveClient", tenant.getDomainName()));
        return view;
    }


    @PreAuthorize("hasAuthority('CREATE_OAUTH')")
    @RequestMapping(value = "/saveClient", method = RequestMethod.POST)
    public ModelAndView saveClient(
            @PathVariable("applicationId") String applicationId,
            @ModelAttribute("oauthClient") OauthClientRegistrationForm form,
            RedirectAttributes redirectAttributes, Locale locale
    ) {
        Application application =
                applicationService.getByApplicationName(tenant, applicationId).getResult();

        OauthClientDetails clientDetails = form.toModel();
        clientDetails.setClientId(String.format("application://%s/%s", application.getName(), clientDetails.getName()));

        return doSaveClient(
                () -> oAuthClientDetailsService.saveClient(
                        tenant,
                        application,
                        clientDetails),
                application,
                form, locale,
                redirectAttributes, "");
    }

    @PreAuthorize("hasAuthority('REMOVE_OAUTH')")
    @RequestMapping(value = "/{clientSecret}/deleteClient", method = RequestMethod.GET)
    public ModelAndView removeClient(
            @PathVariable("applicationId") String applicationId,
            @PathVariable("clientSecret") String clientSecret,
            @ModelAttribute("clientForm") OauthClientRegistrationForm form,
            RedirectAttributes redirectAttributes, Locale locale) {
        Application application =
                applicationService.getByApplicationName(tenant, applicationId).getResult();

        ServiceResponse<OauthClientDetails> oAuthServiceResponse = oAuthClientDetailsService.loadApplicationAndClientSecret(tenant, application, clientSecret);
        OauthClientDetails oauthClient = oAuthServiceResponse.getResult();

        return doRemoveClient(
                () -> oAuthClientDetailsService.deleteClient(
                        tenant,
                        oauthClient.getClientId()),
                application,
                form, locale,
                redirectAttributes, "");
    }

    @PreAuthorize("hasAuthority('REMOVE_OAUTH')")
    @RequestMapping(value = "/{clientSecret}/edit", method = RequestMethod.GET)
    public ModelAndView editClient(
            @PathVariable("applicationId") String applicationId,
            @PathVariable("clientSecret") String clientSecret,
            @ModelAttribute("clientForm") OauthClientRegistrationForm form,
            RedirectAttributes redirectAttributes, Locale locale) {
        Application application =
                applicationService.getByApplicationName(tenant, applicationId).getResult();

        ServiceResponse<OauthClientDetails> oAuthServiceResponse = oAuthClientDetailsService.loadApplicationAndClientSecret(tenant, application, clientSecret);
        OauthClientDetails oauthClient = oAuthServiceResponse.getResult();

        ServiceResponse<List<AccessToken>> tokenList =
                oAuthClientDetailsService.loadClientId(oauthClient);

        ModelAndView view = new ModelAndView("idm/clients/show")
                .addObject("oauthClient", oauthClient)
                .addObject("allTokens", tokenList.getResult())
                .addObject("applicationId", application)
                .addObject("action",
                        MessageFormat.format("/idm/{0}/clients/saveClient", tenant.getDomainName()));
        return view;
    }

    @PreAuthorize("hasAuthority('REMOVE_OAUTH')")
    @RequestMapping(value = "/{clientSecret}/createToken", method = RequestMethod.GET)
    public ModelAndView editClient(
            @PathVariable("applicationId") String applicationId,
            @PathVariable("clientSecret") String clientSecret,
            RedirectAttributes redirectAttributes, Locale locale) {
        Application application =
                applicationService.getByApplicationName(tenant, applicationId).getResult();

        ServiceResponse<OauthClientDetails> oAuthServiceResponse = oAuthClientDetailsService.loadApplicationAndClientSecret(tenant, application, clientSecret);
        OauthClientDetails oauthClient = oAuthServiceResponse.getResult();

        ServiceResponse<OAuth2AccessToken> tokenServiceResponse =
                oAuth2AccessTokenService.getAccessToken(tenant, application, oauthClient);

        ModelAndView view = new ModelAndView("idm/clients/new-token")
                .addObject("oauthClient", oauthClient)
                .addObject("token", tokenServiceResponse.getResult().getValue())
                .addObject("applicationId", application)
                .addObject("action",
                        MessageFormat.format("/idm/{0}/clients/saveClient", tenant.getDomainName()));
        return view;
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
            return new ModelAndView(MessageFormat.format("redirect:/idm/{0}/clients/",
                    application.getName()));
        } else {
            List<String> messages = serviceResponse.getResponseMessages()
                    .entrySet().stream()
                    .map(message -> applicationContext.getMessage(message.getKey(), message.getValue(), locale))
                    .collect(Collectors.toList());
            return new ModelAndView("idm/clients/form").addObject("errors", messages)
                    .addObject("oauthClient", new OauthClientRegistrationForm().toModel())
                    .addObject("applicationId", tenant.getDomainName())
                    .addObject("action",
                            MessageFormat.format("/idm/{0}/clients/saveClient", tenant.getDomainName()))
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
            return new ModelAndView(MessageFormat.format("redirect:/idm/{0}/clients/",
                    application.getName(),
                    serviceResponse.getResult().getClientId()));
        } else {
            List<String> messages = serviceResponse.getResponseMessages()
                    .entrySet().stream()
                    .map(message -> applicationContext.getMessage(message.getKey(), message.getValue(), locale))
                    .collect(Collectors.toList());
            return new ModelAndView("idm/clients/form").addObject("errors", messages)
                    .addObject("oauthClient", new OauthClientRegistrationForm().toModel())
                    .addObject("applicationId", tenant.getDomainName())
                    .addObject("action",
                            MessageFormat.format("/idm/{0}/clients/saveClient", tenant.getDomainName()))
                    .addObject("method", action);
        }

    }

    @PreAuthorize("hasAuthority('REMOVE_OAUTH')")
    @RequestMapping(value = "/{tokenId}/deleteToken", method = RequestMethod.GET)
    public ModelAndView removeToken(
            @PathVariable("applicationId") String applicationId,
            @PathVariable("tokenId") String clientId,
            @ModelAttribute("tokenForm") AccessTokenRegistrationForm form,
            RedirectAttributes redirectAttributes, Locale locale) {
        Application application =
                applicationService.getByApplicationName(tenant, applicationId).getResult();

        return doRemoveToken(
                () -> oAuthClientDetailsService.deleteTokenAsRoot(clientId),
                application,
                form, locale,
                redirectAttributes, "");
    }

    private ModelAndView doRemoveToken(Supplier<ServiceResponse<AccessToken>> responseSupplier,
                                       Application application,
                                       AccessTokenRegistrationForm registrationForm, Locale locale,
                                       RedirectAttributes redirectAttributes, String action) {

        ServiceResponse<AccessToken> serviceResponse = responseSupplier.get();

        if (serviceResponse.getStatus().equals(ServiceResponse.Status.OK)) {
            redirectAttributes.addFlashAttribute("message",
                    applicationContext.getMessage(OAuthClientDetailsService.Messages.TOKEN_REMOVED_SUCCESSFULLY.getCode(),
                            null, locale));
            return new ModelAndView(MessageFormat.format("redirect:/idm/{0}/clients/",
                    application.getName()));
        } else {
            List<String> messages = serviceResponse.getResponseMessages()
                    .entrySet().stream()
                    .map(message -> applicationContext.getMessage(message.getKey(), message.getValue(), locale))
                    .collect(Collectors.toList());
            return new ModelAndView("idm/clients/form").addObject("errors", messages)
                    .addObject("oauthClient", new OauthClientRegistrationForm().toModel())
                    .addObject("applicationId", tenant.getDomainName())
                    .addObject("action",
                            MessageFormat.format("/idm/{0}/clients/", application.getName()))
                    .addObject("method", action);
        }
    }

}
