package com.konkerlabs.platform.registry.web.converters;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.konkerlabs.platform.registry.business.model.User;

@Component
public class InstantToStringConverter implements Converter<Instant, String> {

	private static final String GLOBAL_DATETIME_FORMAT_PATTERN = "datetime.format.pattern";

	@Autowired
	private ApplicationContext appContext;

	@Override
	public String convert(Instant source) {
		ZonedDateTime zonedTime = source.atZone(ZoneId.of(getUser().getZoneId()));
		Locale locale = getCurrentLocale();
		return zonedTime.format(DateTimeFormatter.ofPattern(getDateTimeFormatPattern(locale), locale));
	}

	private Locale getCurrentLocale() {
		return LocaleContextHolder.getLocale();
	}

	private String getDateTimeFormatPattern(Locale locale) {
		return appContext.getMessage(GLOBAL_DATETIME_FORMAT_PATTERN, null, locale);
	}

	private User getUser() {
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return User.class.cast(userDetails);
	}

}
