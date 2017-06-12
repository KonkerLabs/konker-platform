package com.konkerlabs.platform.registry.api.web.interceptor;

import java.io.DataOutputStream;
import java.text.MessageFormat;
import java.util.Optional;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.HttpOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import com.google.common.io.CountingOutputStream;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.UserService;

public class ResquestResponseInterceptor extends HandlerInterceptorAdapter {
	
	private Logger LOGGER = LoggerFactory.getLogger(ResquestResponseInterceptor.class);
	
	@Autowired
	private UserService userService;
	
	@Override
	public void afterCompletion(
			HttpServletRequest request, 
			HttpServletResponse response, 
			Object handler, 
			Exception ex) throws Exception {

		String emailUser = request.getUserPrincipal().getName();
		ServiceResponse<User> serviceResponse = userService.findByEmail(emailUser);
		String url = request.getRequestURL().toString();
		HttpOutput out = (HttpOutput) response.getOutputStream();
		
		if (serviceResponse.isOk()) {
			LOGGER.info(MessageFormat.format("{0} {1} {2}", 
					serviceResponse.getResult().getTenant().getDomainName(),
					url,
					out.getWritten()));
		}
		
	}

}
