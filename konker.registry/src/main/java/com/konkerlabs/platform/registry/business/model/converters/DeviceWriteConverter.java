package com.konkerlabs.platform.registry.business.model.converters;

import com.konkerlabs.platform.registry.business.model.Device;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class DeviceWriteConverter implements Converter<Device,DBObject> {
    @Override
    public DBObject convert(Device device) {
        DBObject dbo = new BasicDBObject();
        dbo.put("tenant",device.getTenant());
        dbo.put("deviceId",device.getDeviceId());
        dbo.put("name",device.getName());
        dbo.put("description",device.getDescription());
        dbo.put("registrationDate",device.getRegistrationDate().toEpochMilli());
        dbo.put("events",device.getEvents());
        return dbo;
    }
}
