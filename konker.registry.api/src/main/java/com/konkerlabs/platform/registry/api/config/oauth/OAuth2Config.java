package com.konkerlabs.platform.registry.api.config.oauth;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.builders.InMemoryClientDetailsServiceBuilder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;

import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.UserService;
import com.konkerlabs.platform.security.exceptions.SecurityException;
import com.konkerlabs.platform.security.managers.PasswordManager;

@Configuration
@EnableAuthorizationServer
public class OAuth2Config extends AuthorizationServerConfigurerAdapter {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(OAuth2Config.class);

	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	private UserService userService;

	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints)
			throws Exception {
		endpoints.authenticationManager(authenticationManager);
	}
	
	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		ServiceResponse<List<User>> findByEmail = userService.findAll();
		List<User> users = findByEmail.getResult();
		
		InMemoryClientDetailsServiceBuilder inMemory = clients.inMemory();
		
		users.forEach(user -> {
			inMemory
				.withClient(user.getEmail())
				.secret(user.getPassword())
				.authorizedGrantTypes("client_credentials", "password")
				.authorities(getAuthorities(user))
				.scopes("read")
				.resourceIds(ResourceServer.RESOURCE_ID)
				.accessTokenValiditySeconds(0);
		});
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
	}
	
	private String getAuthorities(User user) {
		StringBuffer sb = new StringBuffer();
		
		user.getAuthorities().forEach(auth -> sb.append(auth.getAuthority() + ","));
		sb.deleteCharAt(sb.lastIndexOf(","));
		
		return sb.toString();
	}
}
