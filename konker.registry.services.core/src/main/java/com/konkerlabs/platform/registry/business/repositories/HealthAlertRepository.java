package com.konkerlabs.platform.registry.business.repositories;

import java.util.List;

import com.konkerlabs.platform.registry.business.model.AlertTrigger;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.konkerlabs.platform.registry.business.model.HealthAlert;

public interface HealthAlertRepository extends MongoRepository<HealthAlert, String> {

	@Query("{ 'tenant.id' : ?0, 'application.name' : ?1, 'solved' : false }")
	List<HealthAlert> findAllByTenantIdAndApplicationName(String tenantId, String applicationName);
	
	@Query("{ 'tenant.id' : ?0, 'application.name' : ?1, 'device.id' : ?2, 'solved' : false }")
	List<HealthAlert> findAllByTenantIdApplicationNameAndDeviceId(String tenantId, String applicationName, String deviceId);
    
    @Query("{ 'tenant.id' : ?0, 'application.name' : ?1, 'guid' : ?2 }")
    HealthAlert findByTenantIdApplicationNameAndGuid(String tenantId, String applicationName, String healthAlertGuid);

    @Query("{ 'tenant.id' : ?0, 'application.name' : ?1, 'alertTrigger.id' : ?2, 'solved' : false }")
    List<HealthAlert> findAllByTenantIdApplicationNameAndTriggerId(String tenantId, String applicationName, String triggerId);

    @Query("{ 'tenant.id' : ?0, 'application.name' : ?1, 'alertTrigger.id' : ?2, 'alertId' : ?3, 'solved' : false }")
    HealthAlert findByTenantIdApplicationNameTriggerAndAlertId(String tenantId, String applicationName, String alertTriggerId, String alertId);

}
