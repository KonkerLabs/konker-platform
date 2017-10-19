package com.konkerlabs.platform.registry.business.repositories;

import com.konkerlabs.platform.registry.business.model.EventRoute;
import com.konkerlabs.platform.registry.business.model.Gateway;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.net.URI;
import java.util.List;

public interface GatewayRepository extends MongoRepository<Gateway, String> {

	@Query("{ 'tenant.id' : ?0, 'application.name' : ?1 }")
	List<Gateway> findAll(String tenantId, String applicationName);

	@Query("{ 'tenant.id' : ?0, 'application.name' : ?1, 'name' : ?2 }")
	Gateway findByName(String tenantId, String applicationName, String name);

	@Query("{ 'tenant.id' : ?0, 'application.name' : ?1, 'guid' : ?2 }")
	Gateway findByGuid(String tenantId, String applicationName, String guid);

}