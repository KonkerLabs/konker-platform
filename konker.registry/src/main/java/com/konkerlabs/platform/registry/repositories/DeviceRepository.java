package com.konkerlabs.platform.registry.repositories;

import com.konkerlabs.platform.registry.model.Device;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DeviceRepository extends MongoRepository<Device,String> {
}
