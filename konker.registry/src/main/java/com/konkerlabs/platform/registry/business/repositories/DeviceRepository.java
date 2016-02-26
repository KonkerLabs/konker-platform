package com.konkerlabs.platform.registry.business.repositories;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Tenant;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;

public interface DeviceRepository extends MongoRepository<Device,String> {

    @Query("{ 'tenant.id' : ?0 }")
    List<Device> findAllByTenant(String tenantId);
    @Query("{ 'tenant.id' : ?0, 'deviceId' : ?1 }")
    Device findByTenantIdAndDeviceId(String tenantId, String deviceId);
    @Query("{ 'tenant.id' : ?0, 'id' : ?1 }")
    Device findByTenantAndId(String tenantId, String deviceId);
    @Query("{ 'apiKey' : ?0 }")
    Device findByApiKey(String apiKey);
    //TODO This method must be extinguished when event rule specialized URI gets available
    @Query("{ 'deviceId' : ?0 }")
    Device findByDeviceId(String deviceId);
}