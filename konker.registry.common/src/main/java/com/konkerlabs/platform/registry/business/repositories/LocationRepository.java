package com.konkerlabs.platform.registry.business.repositories;

import com.konkerlabs.platform.registry.business.model.Location;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface LocationRepository extends MongoRepository<Location,String> {

    @Query("{ 'tenant.id' : ?0, 'application.name' : ?1 }")
	List<Location> findAllByTenantIdAndApplicationName(String tenantId, String applicationName);

    @Query("{ 'tenant.id' : ?0, 'application.name' : ?1, 'parent' : { '$exists' : false } }")
    Location findRootLocationByTenantAndApplication(String tenantId, String applicationName);

    @Query("{ 'tenant.id' : ?0, 'application.name' : ?1, 'guid' : ?2 }")
    Location findByTenantAndApplicationAndGuid(String tenantId, String applicationName, String guid);

    @Query("{ 'tenant.id' : ?0, 'application.name' : ?1, 'name' : ?2 }")
	Location findByTenantAndApplicationAndName(String tenantId, String applicationName, String name);

    @Query("{ 'tenant.id' : ?0, 'application.name' : ?1, 'parent.id' : ?2 }")
    List<Location> findChildrensByParentId(String tenantId, String applicationName, String parentId);

}