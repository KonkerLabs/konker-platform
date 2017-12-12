package com.konkerlabs.platform.registry.test.base;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

@Configuration
public class MessageSouceTestConfiguration {

	@Bean(name = "messageSource")
    public MessageSource getMessageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.addBasenames("classpath:/messages/alert-triggers");
        messageSource.addBasenames("classpath:/messages/applications");
        messageSource.addBasenames("classpath:/messages/device-model");
        messageSource.addBasenames("classpath:/messages/devices");
        messageSource.addBasenames("classpath:/messages/devices-config");
        messageSource.addBasenames("classpath:/messages/gateways");
        messageSource.addBasenames("classpath:/messages/locations");
        messageSource.addBasenames("classpath:/messages/rest-destination");
        messageSource.addBasenames("classpath:/messages/routes");
        messageSource.addBasenames("classpath:/messages/transformations");
        messageSource.addBasenames("classpath:/messages/users");
        messageSource.addBasenames("classpath:/messages/tenants");
        messageSource.addBasenames("classpath:/messages/health-alert");
        messageSource.addBasenames("classpath:/mail/MailMessages");
        messageSource.setDefaultEncoding("UTF-8");

        return messageSource;
    }
}
