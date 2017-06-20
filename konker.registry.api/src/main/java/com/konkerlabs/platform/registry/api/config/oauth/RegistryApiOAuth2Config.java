package com.konkerlabs.platform.registry.api.config.oauth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;

/*@Configuration
@EnableAuthorizationServer
@Import({MethodSecurityConfig.class})*/
public class RegistryApiOAuth2Config extends AuthorizationServerConfigurerAdapter {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RegistryApiOAuth2Config.class);

	/*@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	@Qualifier("oauth2ClientDetails")
	private ClientDetailsService clientDetailsService;

	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints)
			throws Exception {
		TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
		tokenEnhancerChain.setTokenEnhancers(
				Arrays.asList(
						tokenEnhancer(), 
						accessTokenConverter()
					));
		
		
		endpoints.authenticationManager(authenticationManager)
				.tokenStore(tokenStore())
				.tokenEnhancer(tokenEnhancerChain)
				.allowedTokenEndpointRequestMethods(HttpMethod.GET, HttpMethod.POST);
	}
	
	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		clients.withClientDetails(clientDetailsService);
	}

	@Override
	public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
		security.passwordEncoder(new PasswordEncoder() {
			
			@Override
			public boolean matches(CharSequence rawPassword, String encodedPassword) {
				try {
					return new PasswordManager().validatePassword(rawPassword.toString(), encodedPassword);
				} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
					LOGGER.error(e.getMessage(), e);
					return false;
				}
			}
			
			@Override
			public String encode(CharSequence rawPassword) {
				try {
					return new PasswordManager().createHash(rawPassword.toString());
				} catch (SecurityException e) {
					LOGGER.error(e.getMessage(), e);
					return "";
				}
			}
		});
		security
				.realm("konker")
				.allowFormAuthenticationForClients();
	}
	
	@Bean
	public JwtAccessTokenConverter accessTokenConverter() {
		JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
		converter.setSigningKey("apikonkeroauth");
		return converter;
	}

	@Bean
	public TokenStore tokenStore() {
		return new JwtTokenStore(accessTokenConverter());
	}
	
	@Bean
	@Primary
	public DefaultTokenServices tokenServices() {
		DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
		defaultTokenServices.setTokenStore(tokenStore());
		defaultTokenServices.setSupportRefreshToken(true);
		return defaultTokenServices;
	}
	
	@Bean
	public TokenEnhancer tokenEnhancer() {
		return new CustomTokenEnhancer();
	}*/
	
}
