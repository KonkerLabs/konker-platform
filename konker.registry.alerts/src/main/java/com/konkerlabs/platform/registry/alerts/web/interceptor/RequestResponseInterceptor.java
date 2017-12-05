package com.konkerlabs.platform.registry.alerts.web.interceptor;

import org.eclipse.jetty.server.HttpOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.MessageFormat;

public class RequestResponseInterceptor extends HandlerInterceptorAdapter {

    private Logger LOGGER = LoggerFactory.getLogger(RequestResponseInterceptor.class);

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex) throws Exception {

        String url = request.getRequestURL().toString();
        HttpOutput out = (HttpOutput) response.getOutputStream();

        LOGGER.info(MessageFormat.format("{0} {1} {2} {3}",
                "public-access",
                url,
                response.getStatus(),
                out.getWritten()));


    }

}
