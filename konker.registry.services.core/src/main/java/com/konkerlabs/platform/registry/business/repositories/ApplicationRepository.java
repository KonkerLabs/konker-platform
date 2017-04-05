package com.konkerlabs.platform.registry.business.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.konkerlabs.platform.registry.business.model.Application;

public interface ApplicationRepository extends MongoRepository<Application,String> {

    @Query("{ 'tenant.id' : ?0 }")
    List<Application> findAllByTenant(String tenantId);
    
    @Query("{ 'tenant.id' : ?0, '_id' : ?1  }")
    Application findByTenantAndName(String tenantId, String name);
   
}