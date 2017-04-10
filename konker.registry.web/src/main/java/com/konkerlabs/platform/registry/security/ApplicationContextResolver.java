package com.konkerlabs.platform.registry.security;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.User;
import org.springframework.beans.factory.SmartFactoryBean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component("application")
public class ApplicationContextResolver implements SmartFactoryBean<Application> {

    @Override
    public Application getObject() throws Exception {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Tenant tenant =  User.class.cast(userDetails).getTenant();
        // Return tenant default application
        return Application.builder().name(tenant.getDomainName()).build();
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
