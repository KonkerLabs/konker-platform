package com.konkerlabs.platform.registry.api.config.oauth;

import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CustomTokenEnhancer implements TokenEnhancer {

	@Override
	public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
		Map<String, Object> additionalInfo = new HashMap<>();
		((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInfo);
		((DefaultOAuth2AccessToken) accessToken).setExpiration(tokenExpiresOn());
		return accessToken;
	}

	private Date tokenExpiresOn() {
		return Date.from(LocalDateTime
				.now()
				.plusYears(10L)
				.atZone(ZoneId.systemDefault())
				.toInstant());
	}

}
