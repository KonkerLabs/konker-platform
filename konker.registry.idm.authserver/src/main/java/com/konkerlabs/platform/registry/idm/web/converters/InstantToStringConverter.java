package com.konkerlabs.platform.registry.idm.web.converters;

import com.konkerlabs.platform.registry.idm.web.converters.utils.ConverterUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class InstantToStringConverter implements Converter<Instant, String> {

    @Autowired
    private ConverterUtils utils;

    @Override
    public String convert(Instant source) {
        ZonedDateTime zonedTime = source.atZone(ZoneId.of(utils.getUserZoneID()));
        return zonedTime
                .format(DateTimeFormatter.ofPattern(
                        utils.getDateTimeFormatPattern(),
                        utils.getCurrentLocale()
                        )
                );
    }

}
