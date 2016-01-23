package com.konkerlabs.platform.registry.business.model.converters;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.mongodb.DBObject;
import org.springframework.core.convert.converter.Converter;

import java.time.Instant;
import java.util.List;

public class DeviceReadConverter implements Converter<DBObject,Device> {
    @Override
    public Device convert(DBObject dbObject) {
        return Device.builder()
                .tenant((Tenant) dbObject.get("tenant"))
                .deviceId(dbObject.get("deviceId").toString())
                .name(dbObject.get("name").toString())
                .description(dbObject.get("description").toString())
                .registrationDate(Instant.ofEpochMilli(Long.valueOf(dbObject.get("registrationDate").toString())))
                .events((List<Event>) dbObject.get("events"))
                .build();
    }
}
