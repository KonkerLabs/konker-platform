package com.konkerlabs.platform.registry.business.repositories;

import com.konkerlabs.platform.registry.business.model.Device;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface DeviceRepository extends MongoRepository<Device,String> {

    @Query("{ 'tenant.id' : ?0 }")
    List<Device> findAllByTenant(String tenantId);
    @Query("{ 'tenant.id' : ?0, 'deviceId' : ?1 }")
    Device findByTenantIdAndDeviceId(String tenantId, String deviceId);
    @Query("{ 'tenant.id' : ?0, 'guid' : ?1 }")
    Device findByTenantAndGuid(String tenantId, String deviceGuid);
    @Query("{ 'apiKey' : ?0 }")
    Device findByApiKey(String apiKey);
    @Query("{ 'tenant.id' : ?0, 'application.name' : ?1 }")
	List<Device> findAllByTenantIdAndApplicationName(String tenantId, String applicationName);
    @Query("{ 'tenant.id' : ?0, 'application.name' : ?1, 'guid' : ?2 }")
	Device findByTenantAndApplicationAndGuid(String tenantId, String applicationName, String guid);
}