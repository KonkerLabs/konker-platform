package com.konkerlabs.platform.registry.api.domain.context;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.repositories.OauthClientDetails;
import org.springframework.beans.factory.SmartFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("application")
public class ApplicationContextResolver implements SmartFactoryBean<Application> {

    @Autowired
    private OauthClientDetails oauthClientDetails;

    @Override
    public Application getObject() throws Exception {
        return oauthClientDetails.getApplication();
    }

    @Override
    public Class<?> getObjectType() {
        return Application.class;
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
