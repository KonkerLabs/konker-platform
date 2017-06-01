package com.konkerlabs.platform.registry.idm.web;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.idm.config.OAuthClientDetailsService;
import com.konkerlabs.platform.registry.idm.domain.repository.AccessToken;
import com.konkerlabs.platform.registry.idm.domain.repository.OauthClientDetails;
import com.konkerlabs.platform.registry.idm.web.form.AccessTokenRegistrationForm;
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
@RequestMapping("/account/{applicationId}/tokens")
public class AccountTokensController implements ApplicationContextAware {

    @Autowired
    private OAuthClientDetailsService oAuthClientDetailsService;
    private ApplicationContext applicationContext;
    @Autowired
    private Tenant tenant;


    @PreAuthorize("hasAuthority('LIST_OAUTHTOKENS_ROOT')")
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView tokensAsRoot(@PathVariable("applicationId") String applicationId) {
        ServiceResponse<List<AccessToken>> tokenList =
                oAuthClientDetailsService.loadAllTokens(
                        Application.builder().name(
                                applicationId
                        ).build());

        ModelAndView view = new ModelAndView(
                String.format("/tokens/index", applicationId))
                .addObject("applicationId", tenant.getDomainName())
                .addObject("allTokens", tokenList.getResult());
        if (tokenList.isOk()) {
            view.addObject("tokens", tokenList.getResult());
        }

        return view;
    }

    @PreAuthorize("hasAuthority('SHOW_OAUTHTOKENS')")
    @RequestMapping(value = "/show", method = RequestMethod.GET)
    public ModelAndView showClient(
            @PathVariable("applicationId") String applicationId,
            @PathVariable("clientId") String clientId
    ) {

        ServiceResponse<OauthClientDetails> client =
                oAuthClientDetailsService.loadClientById(tenant, clientId);

        ModelAndView view = new ModelAndView("clients/form")
                .addObject("oauthClient", client.getResult())
                .addObject("applicationId", tenant.getDomainName())
                .addObject("action",
                        MessageFormat.format("/account/{0}/clients/saveClient", tenant.getDomainName()));
        return view;
    }


    @PreAuthorize("hasAuthority('SHOW_OAUTHTOKENS_ROOT')")
    @RequestMapping(value = "/{tokenId}/show", method = RequestMethod.GET)
    public ModelAndView showTokenAsRoot(
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


    @PreAuthorize("hasAuthority('REMOVE_OAUTHTOKENS')")
    @RequestMapping(value = "/{tokenId}/deleteToken", method = RequestMethod.DELETE)
    public ModelAndView removeToken(
            @PathVariable("applicationId") String applicationId,
            @PathVariable("tokenId") String clientId,
            @ModelAttribute("tokenForm") AccessTokenRegistrationForm form,
            RedirectAttributes redirectAttributes, Locale locale) {

        return doRemoveToken(
                () -> oAuthClientDetailsService.deleteTokenAsRoot(clientId),
                Application.builder().name(applicationId).build(),
                form, locale,
                redirectAttributes, "");
    }

    @PreAuthorize("hasAuthority('REMOVE_OAUTHTOKENS_ROOT')")
    @RequestMapping(value = "/{tokenId}/deleteTokenAll", method = RequestMethod.DELETE)
    public ModelAndView removeTokenAsRoot(
            @PathVariable("applicationId") String applicationId,
            @PathVariable("tokenId") String clientId,
            @ModelAttribute("tokenForm") AccessTokenRegistrationForm form,
            RedirectAttributes redirectAttributes, Locale locale) {

        return doRemoveToken(
                () -> oAuthClientDetailsService.deleteToken(
                        tenant,
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

    private ModelAndView doRemoveToken(Supplier<ServiceResponse<AccessToken>> responseSupplier,
                                       Application application,
                                       AccessTokenRegistrationForm registrationForm, Locale locale,
                                       RedirectAttributes redirectAttributes, String action) {

        ServiceResponse<AccessToken> serviceResponse = responseSupplier.get();

        if (serviceResponse.getStatus().equals(ServiceResponse.Status.OK)) {
            redirectAttributes.addFlashAttribute("message",
                    applicationContext.getMessage(OAuthClientDetailsService.Messages.TOKEN_REMOVED_SUCCESSFULLY.getCode(),
                            null, locale));
            return new ModelAndView(MessageFormat.format("redirect:/account/{0}/tokens/",
                    application.getName(),
                    serviceResponse.getResult().getTokenId()));
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
