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

import java.text.MessageFormat;
import java.util.List;


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

        ModelAndView view = new ModelAndView("clients/index");
        if (clientList.isOk()) {
            view.addObject("clients", clientList.getResult());
        }

        return view;
    }

    @RequestMapping(value = "/{applicationId}/clients/new", method = RequestMethod.GET)
    public ModelAndView newClient() {
        ModelAndView view = new ModelAndView("clients/form")
                .addObject("device", new OauthClientRegistrationForm())
                .addObject("action",
                        MessageFormat.format("/account/{0}/clients/saveClient", tenant.getDomainName()));
        return view;
    }

    @RequestMapping(value = "/{applicationId}/clients/saveClient", method = RequestMethod.GET)
    public ModelAndView saveClient(
            @PathVariable("applicationId") String applicationId,
            @ModelAttribute("deviceForm") OauthClientRegistrationForm form
    ) {
        ServiceResponse<OauthClientDetails> result =
                oAuthClientDetailsService.saveClient(
                        tenant, Application.builder().name(applicationId).build());

        ModelAndView view = new ModelAndView("clients/form")
                .addObject(result);
        return view;
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

}
