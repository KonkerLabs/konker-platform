package com.konkerlabs.platform.registry.business.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.konkerlabs.platform.registry.business.model.DeviceModel;

public interface DeviceModelRepository extends MongoRepository<DeviceModel, String> {

	@Query("{ 'tenant.id' : ?0, 'application.name' : ?1 }")
	List<DeviceModel> findAllByTenantIdAndApplicationName(String tenantId, String applicationName);

    @Query("{ 'tenant.id' : ?0, 'application.name' : ?1 }")
    Page<DeviceModel> findAllByTenantIdAndApplicationName(String tenantId, String applicationName, Pageable pageRequest);

    @Query("{ 'tenant.id' : ?0, 'application.name' : ?1, 'name' : ?2 }")
    DeviceModel findByTenantIdApplicationNameAndName(String tenantId, String applicationName, String name);

    @Query("{ 'tenant.id' : ?0, 'application.name' : ?1, 'guid' : ?2 }")
    DeviceModel findByTenantIdApplicationNameAndGuid(String tenantId, String applicationName, String guid);

    @Query("{ 'tenant.id' : ?0, 'application.name' : ?1, 'defaultModel' : ?2 }")
	DeviceModel findDefault(String tenantId, String applicationName, boolean defaultModel);

}
