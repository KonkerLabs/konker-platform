package com.konkerlabs.platform.registry.business.repositories;

import com.konkerlabs.platform.registry.business.model.EventRoute;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.net.URI;
import java.util.List;

public interface EventRouteRepository extends MongoRepository<EventRoute, String> {

	@Query("{ 'incoming.uri' : ?0 }")
	List<EventRoute> findByIncomingUri(URI routeActorUri);

	@Query("{ 'outgoing.uri' : ?0 }")
	List<EventRoute> findByOutgoingUri(URI routeActorUri);

	@Query("{ 'tenant.id' : ?0, 'application.name' : ?1 }")
	List<EventRoute> findAll(String tenantId, String applicationName);

	@Query("{ 'tenant.id' : ?0, 'application.name' : ?1, 'name' : ?2 }")
	EventRoute findByRouteName(String tenantId, String applicationName, String name);

	@Query("{ 'tenant.id' : ?0, 'application.name' : ?1, 'guid' : ?2 }")
	EventRoute findByGuid(String tenantId, String applicationName, String guid);

	@Query("{ 'tenant.id' : ?0, 'application.name' : ?1, 'transformation.id' :  ?2}")
	List<EventRoute> findByTransformationId(String tenantId, String applicationName, String transformationid);

}