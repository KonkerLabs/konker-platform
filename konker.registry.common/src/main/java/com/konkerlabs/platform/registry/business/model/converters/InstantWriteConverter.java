package com.konkerlabs.platform.registry.business.model.converters;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import java.time.Instant;

@WritingConverter
public class InstantWriteConverter implements Converter<Instant,Long> {
    @Override
    public Long convert(Instant source) {
        return source.toEpochMilli();
    }
}
