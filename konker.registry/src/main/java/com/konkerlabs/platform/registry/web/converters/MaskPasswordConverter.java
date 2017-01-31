package com.konkerlabs.platform.registry.web.converters;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class MaskPasswordConverter implements Converter<String, String> {

	@Override
	public String convert(String source) {

		if (source == null) {
			return null;
		} else {
			return StringUtils.leftPad("", source.length(), '*');
		}

	}

}
