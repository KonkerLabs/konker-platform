package com.konkerlabs.platform.registry.interceptor;

import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.security.UserContextResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;
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
                LOG.error("Invalid user type on session", e);
            }

        }

        return super.preHandle(request, response, handler);
    }
}
