package com.konkerlabs.platform.registry.business.model.converters;

import com.konkerlabs.platform.registry.business.model.Event;
import com.mongodb.DBObject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.time.Instant;

@ReadingConverter
public class EventReadConverter implements Converter<DBObject,Event> {
    @Override
    public Event convert(DBObject dbObject) {
        return Event.builder()
            .timestamp(Instant.ofEpochMilli(Long.valueOf(dbObject.get("timestamp").toString())))
            .payload(dbObject.get("payload").toString())
            .build();
    }
}
