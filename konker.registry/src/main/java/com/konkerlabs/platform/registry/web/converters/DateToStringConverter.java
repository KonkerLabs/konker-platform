package com.konkerlabs.platform.registry.web.converters;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.security.UserContextResolver;

@Component
public class DateToStringConverter implements Converter<Date, String> {

	@Autowired
	private UserContextResolver userContextResolver;

	@Override
	public String convert(Date source) {

		User currentUser = userContextResolver.getObject();
		DateFormat df = new SimpleDateFormat(currentUser.getDateFormat().getId() + " HH:mm:ss");
		df.setTimeZone(TimeZone.getTimeZone(currentUser.getZoneId().getId()));

		return df.format(source);

	}

}
