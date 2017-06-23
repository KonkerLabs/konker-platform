package com.konkerlabs.platform.registry.api.domain.context;

import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.model.OauthClientDetails;
import org.springframework.beans.factory.SmartFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("user")
public class UserContextResolver implements SmartFactoryBean<User> {

    @Autowired
    private OauthClientDetails oauthClientDetails;

    @Override
    public User getObject(){
        return oauthClientDetails.getParentUser();
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
