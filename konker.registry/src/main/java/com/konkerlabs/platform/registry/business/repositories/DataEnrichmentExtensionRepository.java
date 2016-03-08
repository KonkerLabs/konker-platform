package com.konkerlabs.platform.registry.business.repositories;

import java.net.URI;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.konkerlabs.platform.registry.business.model.DataEnrichmentExtension;

public interface DataEnrichmentExtensionRepository extends MongoRepository<DataEnrichmentExtension, String> {

    @Query("{ 'tenant.id' : ?0, 'name' : ?1 }")
    DataEnrichmentExtension findByTenantIdAndName(String tenantId, String name);

    @Query("{ 'tenant.id' : ?0 }")
    List<DataEnrichmentExtension> findAllByTenantId(String tenantId);

    @Query("{ 'tenant.id' : ?0, 'incoming' : ?1, 'containerKey' : ?2 }")
    List<DataEnrichmentExtension> findByTenantIdAndIncomingAndContainerKey(String tenantId, URI incoming, String containerKey);

    @Query("{ 'tenant.id' : ?0, 'incoming' : ?1 }")
    List<DataEnrichmentExtension> findByTenantIdAndIncoming(String tenantId, URI incoming);
}