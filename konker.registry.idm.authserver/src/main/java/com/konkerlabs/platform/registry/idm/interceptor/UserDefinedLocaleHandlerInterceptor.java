package com.konkerlabs.platform.registry.idm.interceptor;

import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;


public class UserDefinedLocaleHandlerInterceptor extends HandlerInterceptorAdapter {

    private Logger LOG = LoggerFactory.getLogger(UserDefinedLocaleHandlerInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication()).isPresent() &&
                SecurityContextHolder.getContext().getAuthentication().getPrincipal()
                instanceof User) {
            try {
                User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                if (Optional.ofNullable(request.getSession()).isPresent()
                        && Optional.ofNullable(user).isPresent()) {
                    if (!Optional.ofNullable(request.getSession()
                            .getAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME)).isPresent() ||
                            !request.getSession().getAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME)
                                    .equals(user.getLanguage().getLocale())) {

                        request.getSession().setAttribute(
                                SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME,
                                user.getLanguage().getLocale());
                    }
                }
            } catch (ClassCastException e){
                User invalidUSer = User.builder().name("INVALID USER").tenant(Tenant.builder().name("INVALID TENANT").build()).build();
				LOG.error("Invalid user type on session", 
                		invalidUSer.toURI(),
                		invalidUSer.getTenant().getLogLevel(),
                		e);
            }

        }

        return super.preHandle(request, response, handler);
    }
}
