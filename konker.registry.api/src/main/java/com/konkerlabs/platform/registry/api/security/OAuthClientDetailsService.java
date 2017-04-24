package com.konkerlabs.platform.registry.api.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.stereotype.Service;

import com.konkerlabs.platform.registry.business.repositories.UserRepository;

@Service("oauth2ClientDetails")
public class OAuthClientDetailsService implements ClientDetailsService {

	@Autowired
	private UserRepository userRepository;
	
	@Override
	public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
		return (ClientDetails) userRepository.findOne(clientId);
	}

}
