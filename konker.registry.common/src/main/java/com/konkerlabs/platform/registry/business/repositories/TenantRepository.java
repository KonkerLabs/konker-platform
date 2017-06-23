package com.konkerlabs.platform.registry.business.repositories;

import com.konkerlabs.platform.registry.business.model.Tenant;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface TenantRepository extends MongoRepository<Tenant, String> {

    @Query("{ 'name' : ?0 }")
    Tenant findByName(String name);

    @Query("{ 'domainName' : ?0 }")
    Tenant findByDomainName(String domainName);

}
