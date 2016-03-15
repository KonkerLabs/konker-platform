package com.konkerlabs.platform.registry.business.repositories.outgoing;

import com.konkerlabs.platform.registry.business.model.outgoing.Rest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.net.URI;
import java.util.List;

public interface RestRepository extends MongoRepository<Rest, String>{

    @Query("{ 'uri' : ?0 }")
    Rest findByURI(URI uri);
    @Query("{ 'tenant.id' : ?0 }")
    List<Rest> getAllByTenant(String tenantId);
}
