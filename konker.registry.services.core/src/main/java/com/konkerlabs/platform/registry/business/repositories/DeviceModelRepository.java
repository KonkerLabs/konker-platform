package com.konkerlabs.platform.registry.business.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.konkerlabs.platform.registry.business.model.DeviceModel;

public interface DeviceModelRepository extends MongoRepository<DeviceModel, String> {

	@Query("{ 'tenant.id' : ?0, 'application.name' : ?1 }")
	List<DeviceModel> findAllByTenantIdAndApplicationName(String tenantId, String applicationName);
    
    @Query("{ 'tenant.id' : ?0, 'application.name' : ?1, 'name' : ?2 }")
    DeviceModel findByTenantIdApplicationNameAndName(String tenantId, String applicationName, String name);

}
