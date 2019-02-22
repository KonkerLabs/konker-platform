package com.konkerlabs.platform.registry.test.base;

import com.konkerlabs.platform.registry.config.EmailConfig;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.spring4.SpringTemplateEngine;

@Configuration
public class EmailTestConfig extends EmailConfig {

    @Bean
    public JavaMailSender javaMailSender() {
        return Mockito.mock(JavaMailSender.class);
    }

    @Bean
    public SpringTemplateEngine springTemplateEngine() {
        return Mockito.mock(SpringTemplateEngine.class);
    }
    
}
