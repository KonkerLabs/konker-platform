package com.konkerlabs.platform.registry.web.converters.utils;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;

import com.konkerlabs.platform.registry.security.UserContextResolver;

@Component
public class ConverterUtils {

	@Autowired
	private HttpServletRequest request;
	@Autowired
	private HttpServletResponse response;
	@Autowired
	private ApplicationContext appContext;
	@Autowired
	private UserContextResolver userContextResolver;
	@Autowired
	private LocaleResolver localeResolver;

	private static final String GLOBAL_DATETIME_FORMAT_PATTERN = "datetime.format.pattern";
	private static final String GLOBAL_TIME_FORMAT_PATTERN = "time.format.pattern";

	public Locale getCurrentLocale() {
		return localeResolver.resolveLocale(request);
	}

	/**
	 * Flush locale from session and update with user locale
	 */
	public void flushLocale(){
		localeResolver.setLocale(
				request,
				response,
				userContextResolver.getObject().getLanguage().getLocale());
	}

	public void setLocale(Locale locale){
		localeResolver.setLocale(request, response, locale);
	}


	public String getDateTimeFormatPattern() {
		String date = userContextResolver.getObject().getDateFormat().getId();
		String time = appContext.getMessage(GLOBAL_TIME_FORMAT_PATTERN, null, userContextResolver.getObject().getLanguage().getLocale());
		return date + " " + time;
	}

	public String getUserZoneID() {
		return userContextResolver.getObject().getZoneId().getId();
	}
}
