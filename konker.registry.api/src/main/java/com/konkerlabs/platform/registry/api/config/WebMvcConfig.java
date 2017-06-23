package com.konkerlabs.platform.registry.api.config;

import java.util.Arrays;
import java.util.Map;

import org.springframework.boot.autoconfigure.web.DefaultErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.konkerlabs.platform.registry.api.web.interceptor.ResquestResponseInterceptor;

@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter {

    @Bean(name = "messageSource")
    public MessageSource getMessageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.addBasenames("classpath:/messages/alert-triggers");
        messageSource.addBasenames("classpath:/messages/applications");
        messageSource.addBasenames("classpath:/messages/device-model");
        messageSource.addBasenames("classpath:/messages/devices");
        messageSource.addBasenames("classpath:/messages/devices-config");
        messageSource.addBasenames("classpath:/messages/locations");
        messageSource.addBasenames("classpath:/messages/rest-destination");
        messageSource.addBasenames("classpath:/messages/routes");
        messageSource.addBasenames("classpath:/messages/transformations");
        messageSource.addBasenames("classpath:/messages/users");
        messageSource.addBasenames("classpath:/messages/health-alert");
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
                errorAttributes.remove("exceptions");
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
        
    @Bean
    public ResquestResponseInterceptor resquestResponseInterceptor() {
    	return new ResquestResponseInterceptor();
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
    	registry.addInterceptor(resquestResponseInterceptor()).addPathPatterns("/applications/*", "/users/*");
    }

}
