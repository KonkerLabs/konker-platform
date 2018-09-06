package com.konkerlabs.platform.registry.idm.security;

import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CustomTokenEnhancer implements TokenEnhancer {

	@Override
	public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
		Map<String, Object> additionalInfo = new HashMap<>();
		((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInfo);
		((DefaultOAuth2AccessToken) accessToken).setExpiration(tokenExpiresOn());
		((DefaultOAuth2AccessToken) accessToken).setValue(generateTokenValue());
		return accessToken;
	}

	private String generateTokenValue() {
	    StringBuilder sb = new StringBuilder(UUID.randomUUID().toString());
	    for (int i = 0; i < 2; i++) {
	        sb.append(UUID.randomUUID().toString());
        }
        return sb.toString().replaceAll("-", "");
	}

	private Date tokenExpiresOn() {
		return Date.from(LocalDateTime
				.now()
				.plusYears(10L)
				.atZone(ZoneId.systemDefault())
				.toInstant());
	}

}
