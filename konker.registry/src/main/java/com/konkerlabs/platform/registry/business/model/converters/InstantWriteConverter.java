package com.konkerlabs.platform.registry.business.model.converters;

import org.springframework.core.convert.converter.Converter;

import java.time.Instant;

public class InstantWriteConverter implements Converter<Instant,Long> {
    @Override
    public Long convert(Instant source) {
        return source.toEpochMilli();
    }
}
