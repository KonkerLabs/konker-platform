package com.konkerlabs.platform.registry.business.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.konkerlabs.platform.registry.business.model.AlertTrigger;

public interface AlertTriggerRepository  extends MongoRepository<AlertTrigger, String> {

    @Query("{ 'tenant.id' : ?0, 'application.name' : ?1 }")
    List<AlertTrigger> listByTenantIdAndApplicationName(String tenantId, String applicationName);

    @Query("{ 'tenant.id' : ?0, 'application.name' : ?1, 'guid' : ?2 }")
    AlertTrigger findByTenantIdAndApplicationNameAndGuid(String tenantId, String applicationName, String guid);

    @Query("{ 'tenant.id' : ?0, 'application.name' : ?1, 'name' : ?2 }")
    AlertTrigger findByTenantIdAndApplicationNameAndName(String id, String name, String triggerName);

}
