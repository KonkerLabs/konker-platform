package com.konkerlabs.platform.registry.idm.web;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.idm.config.OAuthClientDetailsService;
import com.konkerlabs.platform.registry.idm.domain.repository.OauthClientDetails;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@Scope("request")
@RestController
@RequestMapping("/account/api/{applicationId}")
public class AccountRestController implements ApplicationContextAware {


    @Autowired
    private OAuthClientDetailsService oAuthClientDetailsService;
    private ApplicationContext applicationContext;
    @Autowired
    private Tenant tenant;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ResponseEntity<List<OauthClientDetails>> listClients(@PathVariable("applicationId") String applicationId){
        ServiceResponse<List<OauthClientDetails>> clientList =
                oAuthClientDetailsService.loadClientsByTenant(
                        tenant, Application.builder().name(applicationId).build());

        if(clientList.isOk()){
            return ResponseEntity
                    .ok().body(clientList.getResult());
        }

        return ResponseEntity.notFound().build();
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public ResponseEntity<OauthClientDetails> create(@PathVariable("applicationId") String applicationId){
        ServiceResponse<OauthClientDetails> result =
                oAuthClientDetailsService.saveClient(tenant,
                        Application.builder().name(applicationId).build());
        if(result.isOk()){
            return ResponseEntity
                    .created(URI.create("/"))
                    .body(result.getResult());
        }

        return ResponseEntity.badRequest().build();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
