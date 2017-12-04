package com.konkerlabs.platform.registry.alerts.repositories;

import com.konkerlabs.platform.registry.business.model.AlertTrigger;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface AlertTriggerRepository  extends MongoRepository<AlertTrigger, String> {

    @Query("{ 'tenant.id' : ?0, 'application.name' : ?1 }")
    List<AlertTrigger> listByTenantIdAndApplicationName(String tenantId, String applicationName);

    @Query("{ 'tenant.id' : ?0, 'application.name' : ?1, 'guid' : ?2 }")
    AlertTrigger findByTenantIdAndApplicationNameAndGuid(String tenantId, String applicationName, String guid);

}
