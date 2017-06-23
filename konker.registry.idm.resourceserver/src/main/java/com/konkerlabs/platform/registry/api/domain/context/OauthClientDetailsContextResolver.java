package com.konkerlabs.platform.registry.api.domain.context;

import com.konkerlabs.platform.registry.business.model.OauthClientDetails;
import com.konkerlabs.platform.registry.idm.config.OAuthClientDetailsService;
import org.springframework.beans.factory.SmartFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("oauthClientDetails")
public class OauthClientDetailsContextResolver implements SmartFactoryBean<OauthClientDetails> {

    @Autowired
    private OAuthClientDetailsService oAuthClientDetailsService;

    @Override
    public boolean isPrototype() {
        return false;
    }

    @Override
    public boolean isEagerInit() {
        return false;
    }

    @Override
    public OauthClientDetails getObject() throws Exception {
        try {
            String principal =
                    (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            OauthClientDetails clientDetails =
                    oAuthClientDetailsService.loadClientByIdAsRoot(principal)
                            .getResult();
            return clientDetails;
        } catch (Exception e){}

        return null;
    }

    @Override
    public Class<?> getObjectType() {
        return OauthClientDetails.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

}
