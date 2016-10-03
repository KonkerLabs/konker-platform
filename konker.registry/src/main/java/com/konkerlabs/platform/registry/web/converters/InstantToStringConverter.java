package com.konkerlabs.platform.registry.web.converters;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.LocaleResolver;

import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.security.UserContextResolver;

@Component
public class InstantToStringConverter implements Converter<Instant, String> {

	private static final String GLOBAL_DATETIME_FORMAT_PATTERN = "datetime.format.pattern";

	@Autowired
	private ApplicationContext appContext;
	@Autowired
	private UserContextResolver userContextResolver;
	@Autowired
	private HttpServletRequest request;

	@Override
	public String convert(Instant source) {
		ZonedDateTime zonedTime = source.atZone(ZoneId.of(getUser().getZoneId()));
		Locale locale = getCurrentLocale();
		return zonedTime.format(DateTimeFormatter.ofPattern(getDateTimeFormatPattern(locale), locale));
	}

	private Locale getCurrentLocale() {
		LocaleResolver resolver = (LocaleResolver) request.getAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE);
		return resolver.resolveLocale(request);
	}

	private String getDateTimeFormatPattern(Locale locale) {
		return appContext.getMessage(GLOBAL_DATETIME_FORMAT_PATTERN, null, locale);
	}

	private User getUser() {
		return userContextResolver.getObject();
	}

}
