package com.konkerlabs.platform.registry.idm.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

@Configuration
@EnableResourceServer
public class ResourceServer extends ResourceServerConfigurerAdapter {

	public static final String RESOURCE_ID = "registryidm";
	private static final String[] PUBLIC_RESOURCES = new String[]{
			"/oauth/**",
			"/bootstrap/**",
			"/konker/**",
			"/bootstrap/**",
			"/font-awesome/**",
			"/probe",
			"/health",
			"/info",
			"/error",
			"/configuration/ui",
			"/swagger-ui.html",
			"/swagger-resources",
			"/swagger-resources/configuration/ui",
			"/swagger-resources/configuration/context",
			"/web/docs",
			"/static/**",
			"/v2/web-docs",
			"/v2/api-docs",
			"/swagger-resources/configuration/security"
	};



	@Bean(name = "messageSource")
	public MessageSource getMessageSource() {
		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.addBasenames(
				"classpath:/i18n/dateformats",
				"classpath:/i18n/error",
				"classpath:/i18n/global",
				"classpath:/i18n/languages",
				"classpath:/i18n/menu",
				"classpath:/i18n/account",
				"classpath:/i18n/tokens");
		messageSource.setDefaultEncoding("UTF-8");
		return messageSource;
	}


	@Override
	public void configure(HttpSecurity http) throws Exception {
		http
			.authorizeRequests()
				.antMatchers(PUBLIC_RESOURCES).permitAll()
				.antMatchers("/").access("#oauth2.hasScope('read')")
				.anyRequest().authenticated();
	}

	@Override
	public void configure(ResourceServerSecurityConfigurer resources)
			throws Exception {
		resources.resourceId(RESOURCE_ID);
	}

	@Bean
	public LocaleResolver localeResolver() {
		final SessionLocaleResolver ret = new SessionLocaleResolver();
		return ret;
	}
}
