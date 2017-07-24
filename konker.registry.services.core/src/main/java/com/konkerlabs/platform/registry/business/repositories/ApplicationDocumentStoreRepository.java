package com.konkerlabs.platform.registry.business.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.konkerlabs.platform.registry.business.model.ApplicationDocumentStore;

public interface ApplicationDocumentStoreRepository extends MongoRepository<ApplicationDocumentStore, String> {

    @Query("{ 'tenant.id' : ?0, 'application.name' : ?1, 'document' : ?2, 'key' : ?3 }")
    ApplicationDocumentStore findUniqueByTenantIdApplicationName(String id, String name, String document, String key);

}
