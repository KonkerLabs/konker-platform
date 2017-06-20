package com.konkerlabs.platform.registry.api.config.oauth;

/*@Configuration
@EnableResourceServer*/
public class RegistryApiResourceServer {

	/*public static final String RESOURCE_ID = "registryapi";
	private static final String[] PUBLIC_RESOURCES = new String[]{
			"/oauth/token",
			"/configuration/ui",
			"/swagger-ui.html",
			"/swagger-resources",
			"/swagger-resources/configuration/ui",
			"/swagger-resources/configuration/context",
			"/web/docs",
			"/static*//**",
			"/v2/web-docs"
	};

	*//*@Override*//*
	public void configure(HttpSecurity http) throws Exception {
		http
			.authorizeRequests()
				.antMatchers(PUBLIC_RESOURCES).permitAll()
				.antMatchers("/").access("#oauth2.hasScope('read')")
				.anyRequest().authenticated();
	}

	*//*@Override*//*
	public void configure(ResourceServerSecurityConfigurer resources)
			throws Exception {
		resources.resourceId(RESOURCE_ID);
	}*/

}
