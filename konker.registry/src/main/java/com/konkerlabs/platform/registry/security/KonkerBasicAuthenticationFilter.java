package com.konkerlabs.platform.registry.security;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

public class KonkerBasicAuthenticationFilter extends BasicAuthenticationFilter {

	public KonkerBasicAuthenticationFilter(AuthenticationManager authenticationManager) {
		super(authenticationManager);
	}
	
	@Override
	protected void onUnsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException failed) throws IOException {
		super.onUnsuccessfulAuthentication(request, response, failed);
	}


}
