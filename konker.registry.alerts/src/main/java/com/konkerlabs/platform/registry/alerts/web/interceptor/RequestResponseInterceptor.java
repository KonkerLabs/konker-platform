package com.konkerlabs.platform.registry.alerts.web.interceptor;

import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.UserService;
import org.eclipse.jetty.server.HttpOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.MessageFormat;

public class RequestResponseInterceptor extends HandlerInterceptorAdapter {

	private Logger LOGGER = LoggerFactory.getLogger(RequestResponseInterceptor.class);

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
			LOGGER.info(MessageFormat.format("{0} {1} {2} {3}",
                    serviceResponse.getResult().getTenant().getDomainName(),
					url,
                    response.getStatus(),
					out.getWritten()));
		}

	}

}
