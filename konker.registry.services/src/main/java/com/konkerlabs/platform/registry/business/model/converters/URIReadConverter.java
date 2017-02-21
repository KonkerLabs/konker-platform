package com.konkerlabs.platform.registry.business.model.converters;


import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.net.URI;
import java.net.URISyntaxException;

@ReadingConverter
public class URIReadConverter implements Converter<String,URI> {
    @Override
    public URI convert(String source) {
        try {
            return new URI(source);
        } catch (URISyntaxException e) {
            return null;
        }
    }
}
