package com.konkerlabs.platform.registry.idm.web.converters;

import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.idm.domain.context.UserContextResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

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
