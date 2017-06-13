package com.konkerlabs.platform.registry.business.repositories;

import com.konkerlabs.platform.registry.business.model.DeviceCustomData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface DeviceCustomDataRepository extends MongoRepository<DeviceCustomData, String> {

    @Query("{ 'tenant.id' : ?0, 'application.name' : ?1, 'device.id' : ?2 }")
    DeviceCustomData findByTenantIdApplicationNameAndDeviceId(String tenantId, String applicationName, String deviceId);

}
