package com.konkerlabs.platform.registry.business.repositories;

import com.konkerlabs.platform.registry.business.model.EventRoute;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.net.URI;
import java.util.List;

public interface EventRouteRepository extends MongoRepository<EventRoute,String> {
    @Query("{ 'incoming.uri' : ?0 }")
    List<EventRoute> findByIncomingURI(URI uri);
    @Query("{ 'tenant.id' : ?0 }")
    List<EventRoute> findAllByTenant(String tenantId);
    @Query("{ 'tenant.id' : ?0, 'id' : ?1 }")
    EventRoute findByTenantIdAndRouteName(String tenantId, String name);
}