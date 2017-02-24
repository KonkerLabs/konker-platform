package com.konkerlabs.platform.registry.api.config.oauth;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;

@Configuration
@EnableResourceServer
public class ResourceServer extends ResourceServerConfigurerAdapter {

	@Override
	public void configure(HttpSecurity http) throws Exception {
		// @formatter:off
		http
			.authorizeRequests()
				.antMatchers("/oauth/token").permitAll()
				.antMatchers("/").access("#oauth2.hasScope('read')")
				.anyRequest().authenticated();
			// Just for laughs, apply OAuth protection to only 2 resources
//			.requestMatcher(new OrRequestMatcher(
//				new AntPathRequestMatcher("/"), 
//				new AntPathRequestMatcher("/v1/devices")
//			))
//			.authorizeRequests()
//			.anyRequest().access("#oauth2.hasScope('read')");
		// @formatter:on
	}

	@Override
	public void configure(ResourceServerSecurityConfigurer resources)
			throws Exception {
		resources.resourceId("sparklr");
	}

}
