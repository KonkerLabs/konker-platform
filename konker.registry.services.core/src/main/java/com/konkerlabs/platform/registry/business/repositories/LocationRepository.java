package com.konkerlabs.platform.registry.business.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.konkerlabs.platform.registry.business.model.Location;

public interface LocationRepository extends MongoRepository<Location,String> {

    @Query("{ 'tenant.id' : ?0, 'application.name' : ?1 }")
	List<Location> findAllByTenantIdAndApplicationName(String tenantId, String applicationName);

    @Query("{ 'tenant.id' : ?0, 'application.name' : ?1, 'guid' : ?2 }")
    Location findByTenantAndApplicationAndGuid(String tenantId, String applicationName, String guid);

    @Query("{ 'tenant.id' : ?0, 'application.name' : ?1, 'name' : ?2 }")
	Location findByTenantAndApplicationAndName(String tenantId, String applicationName, String name);

}