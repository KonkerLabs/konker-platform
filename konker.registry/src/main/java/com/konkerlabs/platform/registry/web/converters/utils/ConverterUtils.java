package com.konkerlabs.platform.registry.web.converters.utils;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.LocaleResolver;

import com.konkerlabs.platform.registry.security.UserContextResolver;

@Component
public class ConverterUtils {

	@Autowired
	private HttpServletRequest request;
	@Autowired
	private ApplicationContext appContext;
	@Autowired
	private UserContextResolver userContextResolver;

	private static final String GLOBAL_DATETIME_FORMAT_PATTERN = "datetime.format.pattern";

	public Locale getCurrentLocale() {
		LocaleResolver resolver = (LocaleResolver) request.getAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE);
		return resolver.resolveLocale(request);
	}

	public String getDateTimeFormatPattern() {
		return appContext.getMessage(GLOBAL_DATETIME_FORMAT_PATTERN, null, getCurrentLocale());
	}

	public String getUserZoneID() {
		return userContextResolver.getObject().getZoneId();
	}
}
