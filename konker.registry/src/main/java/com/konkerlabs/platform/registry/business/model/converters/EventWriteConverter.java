package com.konkerlabs.platform.registry.business.model.converters;

import com.konkerlabs.platform.registry.business.model.Event;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class EventWriteConverter implements Converter<Event,DBObject> {
    @Override
    public DBObject convert(Event event) {
        DBObject dbo = new BasicDBObject();
        dbo.put("timestamp",event.getTimestamp().toEpochMilli());
        dbo.put("payload",event.getPayload());
        return dbo;
    }
}
