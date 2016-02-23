package com.konkerlabs.platform.registry.business.repositories;

import com.konkerlabs.platform.registry.business.model.DataEnrichmentExtension;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.net.URI;
import java.util.List;

public interface DataEnchrichmentExtensionRepository extends MongoRepository<DataEnrichmentExtension,String> {
    @Query("{ 'incoming.uri' : ?0 }")
    List<DataEnrichmentExtension> findByIncomingURI(URI uri);
}