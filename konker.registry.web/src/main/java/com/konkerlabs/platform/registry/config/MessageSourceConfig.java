package com.konkerlabs.platform.registry.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

@Configuration
public class MessageSourceConfig {
	
	@Bean(name = "messageSource")
    public MessageSource getMessageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.addBasenames(
                "/WEB-INF/i18n/dateformats",
                "/WEB-INF/i18n/destinations",
                "/WEB-INF/i18n/devices",
                "/WEB-INF/i18n/email",
                "/WEB-INF/i18n/error",
                "/WEB-INF/i18n/global",
                "/WEB-INF/i18n/languages",
                "/WEB-INF/i18n/loglevels",
                "/WEB-INF/i18n/menu",
                "/WEB-INF/i18n/routes",
                "/WEB-INF/i18n/timezones",
                "/WEB-INF/i18n/tokens",
                "/WEB-INF/i18n/transformations",
                "/WEB-INF/i18n/usernotifications",
                "/WEB-INF/i18n/users",
                "/WEB-INF/i18n/visualization",
                "classpath:/messages/alert-triggers",
                "classpath:/messages/applications",
                "classpath:/messages/device-model",
                "classpath:/messages/devices",
                "classpath:/messages/rest-destination",
                "classpath:/messages/routes",
                "classpath:/messages/transformations",
                "classpath:/messages/rest-destination",
                "classpath:/messages/users",
                "classpath:/messages/tenants",
                "classpath:/messages/applications",
                "classpath:/messages/device-model",
                "classpath:/messages/health-alert",
                "classpath:/mail/MailMessages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

}
