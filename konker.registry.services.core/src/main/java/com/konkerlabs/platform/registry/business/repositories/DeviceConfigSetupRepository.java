package com.konkerlabs.platform.registry.business.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.konkerlabs.platform.registry.business.model.DeviceConfigSetup;

public interface DeviceConfigSetupRepository extends MongoRepository<DeviceConfigSetup, String> {

    @Query("{ 'tenant.id' : ?0, 'application.name' : ?1 }")
    List<DeviceConfigSetup> findAllByTenantIdAndApplicationName(String tenantId, String applicationName);

}
