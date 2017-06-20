package com.konkerlabs.platform.registry.api.domain.context;

import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.repositories.OauthClientDetails;
import org.springframework.beans.factory.SmartFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("tenant")
public class TenantContextResolver implements SmartFactoryBean<Tenant> {

    @Autowired
    private OauthClientDetails oauthClientDetails;

    @Override
    public Tenant getObject() throws Exception {
        return oauthClientDetails.getTenant();
    }

    @Override
    public Class<?> getObjectType() {
        return Tenant.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    @Override
    public boolean isPrototype() {
        return true;
    }

    @Override
    public boolean isEagerInit() {
        return false;
    }
}
