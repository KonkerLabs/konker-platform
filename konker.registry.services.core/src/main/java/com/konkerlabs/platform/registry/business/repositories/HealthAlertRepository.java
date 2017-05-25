package com.konkerlabs.platform.registry.business.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.konkerlabs.platform.registry.business.model.HealthAlert;

public interface HealthAlertRepository extends MongoRepository<HealthAlert, String> {

	@Query("{ 'tenant.id' : ?0, 'application.name' : ?1, 'solved' : false }")
	List<HealthAlert> findAllByTenantIdAndApplicationName(String tenantId, String applicationName);
	
	@Query("{ 'tenant.id' : ?0, 'application.name' : ?1, 'deviceGuid' : ?2, 'solved' : false }")
	List<HealthAlert> findAllByTenantIdApplicationNameAndDeviceGuid(String tenantId, String applicationName, String deviceGuid);
    
    @Query("{ 'tenant.id' : ?0, 'application.name' : ?1, 'guid' : ?2 }")
    HealthAlert findByTenantIdApplicationNameAndGuid(String tenantId, String applicationName, String healthAlertGuid);

}
