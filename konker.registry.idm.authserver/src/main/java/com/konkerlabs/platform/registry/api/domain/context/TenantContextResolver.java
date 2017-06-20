package com.konkerlabs.platform.registry.api.domain.context;

import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.User;
import org.springframework.beans.factory.SmartFactoryBean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component("tenant")
public class TenantContextResolver implements SmartFactoryBean<Tenant> {

    @Override
    public Tenant getObject() throws Exception {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return User.class.cast(userDetails).getTenant();
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
