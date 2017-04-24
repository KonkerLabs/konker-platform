package com.konkerlabs.platform.registry.business.repositories;

import com.konkerlabs.platform.registry.business.model.RestDestination;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface RestDestinationRepository extends MongoRepository<RestDestination, String>{

    @Query("{ 'tenant.id' : ?0, 'application.name' : ?1, 'guid' : ?2 }")
    RestDestination getByTenantAndGUID(String tenantId, String applicationName, String id);

    @Query("{ 'tenant.id' : ?0, 'application.name' : ?1, 'name' : ?2 }")
    RestDestination getByTenantAndName(String tenantId, String applicationName, String name);

    @Query("{ 'tenant.id' : ?0, 'application.name' : ?1 }")
    List<RestDestination> findAllByTenant(String tenantId, String applicationName);

}
