package com.konkerlabs.platform.registry.business.repositories;

import com.konkerlabs.platform.registry.business.model.RestDestination;
import com.konkerlabs.platform.registry.business.model.SmsDestination;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface SmsDestinationRepository extends MongoRepository<SmsDestination, String> {

    @Query("{ 'tenant.id' : ?0, 'guid' : ?1 }")
    SmsDestination getByTenantAndGUID(String tenantId, String id);

    @Query("{ 'tenant.id' : ?0, 'name' : ?1 }")
    SmsDestination getByTenantAndName(String tenantId, String name);

    @Query("{ 'tenant.id' : ?0 }")
    List<SmsDestination> findAllByTenant(String tenantId);

}
