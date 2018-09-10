package com.konkerlabs.platform.registry.data.core.config.oauth;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;

@Configuration
@EnableResourceServer
public class ResourceServer extends ResourceServerConfigurerAdapter {

	@Override
	public void configure(HttpSecurity http) throws Exception {

		http
            .requestMatchers().antMatchers("/gateway/**")
            .and()
			.authorizeRequests().antMatchers("/gateway/**")
                .access("#oauth2.hasScope('read')")
				.anyRequest().authenticated();

	}

}
