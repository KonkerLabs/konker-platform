package com.konkerlabs.platform.registry.business.services;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.model.LoginAudit.LoginAuditEvent;
import com.konkerlabs.platform.registry.business.services.api.LoginAuditService;

public class KonkerDaoAuthenticationProvider extends DaoAuthenticationProvider {

    private LoginAuditService loginAuditService;

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails,
            UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {

        User user = null;
        Tenant tenant = null;
        if (User.class.isInstance(userDetails)) {
            user = (User) userDetails;
            tenant = user.getTenant();
        }

        try {
            super.additionalAuthenticationChecks(userDetails, authentication);
            if (user != null)
                loginAuditService.register(tenant, user, LoginAuditEvent.LOGIN);

        } catch (BadCredentialsException e) {
            if (user != null)
                loginAuditService.register(tenant, user, LoginAuditEvent.WRONG_PASSWD);
            
            // rethrows BadCredentialsException
            throw e;
        }

    }

    public void setLoginAuditService(LoginAuditService loginAuditService) {
        this.loginAuditService = loginAuditService;
    }

}
