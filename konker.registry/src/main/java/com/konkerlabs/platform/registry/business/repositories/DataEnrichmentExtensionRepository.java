package com.konkerlabs.platform.registry.business.repositories;

import java.net.URI;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.konkerlabs.platform.registry.business.model.DataEnrichmentExtension;

public interface DataEnrichmentExtensionRepository extends MongoRepository<DataEnrichmentExtension, String> {
    @Query("{ 'incoming.uri' : ?0 }")
    List<DataEnrichmentExtension> findByIncomingURI(URI uri);

    @Query("{ 'tenant.id' : ?0, 'name' : ?1 }")
    DataEnrichmentExtension findByTenantIdAndName(String tenantId, String name);

}