package com.konkerlabs.platform.registry.business.repositories;

import com.konkerlabs.platform.registry.business.model.DataEnrichmentExtension;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.net.URI;
import java.util.List;

public interface DataEnrichmentExtensionRepository extends MongoRepository<DataEnrichmentExtension, String> {

    @Query("{ 'tenant.id' : ?0, 'name' : ?1 }")
    DataEnrichmentExtension findByTenantIdAndName(String tenantId, String name);

    @Query("{ 'tenant.id' : ?0, 'guid' : ?1 }")
    DataEnrichmentExtension findByTenantIdAndGUID(String tenantId, String id);

    @Query("{ 'tenant.id' : ?0 }")
    List<DataEnrichmentExtension> findAllByTenantId(String tenantId);

    @Query("{ 'tenant.id' : ?0, 'incoming' : ?1, 'containerKey' : ?2 }")
    List<DataEnrichmentExtension> findByTenantIdAndIncomingAndContainerKey(String tenantId, URI incoming, String containerKey);

    @Query("{ 'tenant.id' : ?0, 'incoming' : ?1 }")
    List<DataEnrichmentExtension> findByTenantIdAndIncoming(String tenantId, URI incoming);
}