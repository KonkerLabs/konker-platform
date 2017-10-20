package com.konkerlabs.platform.registry.test.base;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Collections;

import javax.mail.internet.MimeMessage;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import com.konkerlabs.platform.registry.config.EmailConfig;

@Configuration
public class SpringMailTestConfiguration {

    @Bean
    public JavaMailSender javaMailSender() {
    	JavaMailSenderImpl mock = mock(JavaMailSenderImpl.class);
		when(mock.createMimeMessage()).thenReturn(mock(MimeMessage.class));
		
    	return mock;
    }
    
    @Bean
    public SpringTemplateEngine templateEngine() {
    	SpringTemplateEngine mock = spy(SpringTemplateEngine.class);
    	
    	final ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
		templateResolver.setOrder(2);
		templateResolver.setResolvablePatterns(Collections.singleton("html/*"));
		templateResolver.setPrefix("/mail/");
		templateResolver.setSuffix(".html");
		templateResolver.setTemplateMode(TemplateMode.HTML);
		templateResolver.setCharacterEncoding("UTF-8");
		templateResolver.setCacheable(false);
    	mock.addTemplateResolver(templateResolver);
    	
    	final ClassLoaderTemplateResolver templateResolverTxt = new ClassLoaderTemplateResolver();
		templateResolverTxt.setOrder(1);
		templateResolverTxt.setResolvablePatterns(Collections.singleton("text/*"));
		templateResolverTxt.setPrefix("/mail/");
		templateResolverTxt.setSuffix(".txt");
		templateResolverTxt.setTemplateMode(TemplateMode.TEXT);
		templateResolverTxt.setCharacterEncoding("UTF-8");
		templateResolverTxt.setCacheable(false);
		mock.addTemplateResolver(templateResolverTxt);
    	
		return mock;
    }
    
    @Bean
    public EmailConfig emailConfig() {
    	return new EmailConfig();
    }
}
