package com.konkerlabs.platform.registry.api.config;

import java.util.Arrays;
import java.util.Map;

import org.springframework.boot.autoconfigure.web.DefaultErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.context.request.RequestAttributes;

@Configuration
public class WebMvcConfig  {

    @Bean(name = "messageSource")
    public MessageSource getMessageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.addBasenames("classpath:/messages/devices");
        messageSource.addBasenames("classpath:/messages/routes");
        messageSource.addBasenames("classpath:/messages/transformations");
        messageSource.addBasenames("classpath:/messages/rest-destination");
        messageSource.setDefaultEncoding("UTF-8");

        return messageSource;
    }

    @Bean
    public ErrorAttributes errorAttributes() {
        return new DefaultErrorAttributes() {

            @Override
            public Map<String, Object> getErrorAttributes(RequestAttributes requestAttributes,
                    boolean includeStackTrace) {
                Map<String, Object> errorAttributes = super.getErrorAttributes(requestAttributes, includeStackTrace);
                errorAttributes.remove("path");
                errorAttributes.remove("exception");
                errorAttributes.remove("error");
                errorAttributes.put("code", errorAttributes.get("status"));
                errorAttributes.put("status", "error");

                Object message = errorAttributes.get("message");
                errorAttributes.put("messages", Arrays.asList(message));
                errorAttributes.remove("message");

                return errorAttributes;
            }

        };
    }

}
