package com.konkerlabs.platform.registry.business.repositories;

import com.konkerlabs.platform.registry.business.model.Device;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;

public interface DeviceRepository extends MongoRepository<Device,String> {

    // FIXME: deviceID should be unique within tenant, not globally
    @Query("{ 'deviceId' : ?0 }")
    Device findByDeviceId(String deviceId);

}