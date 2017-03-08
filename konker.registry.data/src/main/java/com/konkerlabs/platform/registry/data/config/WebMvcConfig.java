package com.konkerlabs.platform.registry.data.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

@Configuration
public class WebMvcConfig  {

    @Bean(name = "messageSource")
    public MessageSource getMessageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.addBasenames("classpath:/messages/devices");
        messageSource.addBasenames("classpath:/messages/routes");
        messageSource.addBasenames("classpath:/messages/transformations");
        messageSource.setDefaultEncoding("UTF-8");

        return messageSource;
    }

}
