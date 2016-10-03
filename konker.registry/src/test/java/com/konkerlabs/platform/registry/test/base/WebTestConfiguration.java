package com.konkerlabs.platform.registry.test.base;

import static org.mockito.Mockito.mock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.security.UserContextResolver;

@Configuration
public class WebTestConfiguration {

	@Bean
	public Tenant tenant() {
		return Tenant.builder().name("konker").domainName("konker").id("id").build();
	}

	@Bean
	public User user() {
		return User.builder().email("user@domain.com").tenant(tenant()).build();
	}

	@Bean
	public UserContextResolver userContextResolver() {
		return mock(UserContextResolver.class);
	}
	
	

}
