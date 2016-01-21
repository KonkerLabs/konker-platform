package com.konkerlabs.platform.registry.business.repositories;

import com.konkerlabs.platform.registry.business.model.Device;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;

public interface DeviceRepository extends MongoRepository<Device,String> {

    @Query("{ 'deviceId' : ?0 }")
    List<Device> findByDeviceId(String deviceId);

}
