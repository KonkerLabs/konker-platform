package com.konkerlabs.platform.registry.data.core.security;

import java.io.IOException;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class KonkerParamsAuthenticationFilter extends OncePerRequestFilter {

    private AuthenticationManager authenticationManager;

    public KonkerParamsAuthenticationFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    protected void doFilterInternal(javax.servlet.http.HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        // is already authenticated?
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            chain.doFilter(request, response);
            return;
        }

        Map<String, String[]> parms = request.getParameterMap();

        try {

            final String DEVICE_KEY_PARAM = "deviceKey";
            final String DEVICE_SECRET_PARAM = "deviceSecret";

            if (parms.containsKey(DEVICE_KEY_PARAM) && parms.containsKey(DEVICE_SECRET_PARAM)) {

                String apiKey = parms.get(DEVICE_KEY_PARAM)[0];
                String password = parms.get(DEVICE_SECRET_PARAM)[0];

                UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(apiKey,
                        password);
                Authentication authResult = this.authenticationManager.authenticate(authRequest);

                SecurityContextHolder.getContext().setAuthentication(authResult);

            }

        } catch (AuthenticationException failed) {
            SecurityContextHolder.clearContext();
        }

        chain.doFilter(request, response);

    }

}
