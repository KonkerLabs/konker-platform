package com.konkerlabs.platform.registry.security;

import com.konkerlabs.platform.registry.business.model.User;
import org.springframework.beans.factory.SmartFactoryBean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component("user")
public class UserContextResolver implements SmartFactoryBean<User> {

    @Override
    public User getObject(){
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return User.class.cast(userDetails);
    }

    @Override
    public Class<?> getObjectType() {
        return User.class;
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
