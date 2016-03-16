package com.konkerlabs.platform.registry.business.repositories.outgoing;

import com.konkerlabs.platform.registry.business.model.destinations.RestDestination;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.net.URI;
import java.util.List;

public interface RestRepository extends MongoRepository<RestDestination, String>{

    @Query("{ 'uri' : ?0 }")
    RestDestination findByURI(URI uri);
    @Query("{ 'tenant.id' : ?0 }")
    List<RestDestination> getAllByTenant(String tenantId);
}
