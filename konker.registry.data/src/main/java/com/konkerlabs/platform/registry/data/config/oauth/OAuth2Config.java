package com.konkerlabs.platform.registry.data.config.oauth;

import com.konkerlabs.platform.registry.idm.services.MongoTokenStore;
import com.konkerlabs.platform.security.exceptions.SecurityException;
import com.konkerlabs.platform.security.managers.PasswordManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

@Configuration
@EnableAuthorizationServer
@Import({MethodSecurityConfig.class})
public class OAuth2Config extends AuthorizationServerConfigurerAdapter {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(OAuth2Config.class);

	@Autowired
	private MongoTokenStore mongoTokenStore;

	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	@Qualifier("oauth2ClientDetails")
	private ClientDetailsService clientDetailsService;

	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints)
			throws Exception {
		endpoints.authenticationManager(authenticationManager)
				.tokenStore(mongoTokenStore)
				.allowedTokenEndpointRequestMethods(HttpMethod.GET, HttpMethod.POST);
	}
	
	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		clients.withClientDetails(clientDetailsService);
	}

	@Bean
	@Primary
	public DefaultTokenServices tokenServices() {
		DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
		defaultTokenServices.setTokenStore(mongoTokenStore);
		defaultTokenServices.setSupportRefreshToken(true);
		return defaultTokenServices;
	}
	
}
