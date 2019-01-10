package com.konkerlabs.platform.registry.business.repositories;

import com.konkerlabs.platform.registry.business.model.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends MongoRepository<Application,String> {

    @Query("{ 'tenant.id' : ?0 }")
    List<Application> findAllByTenant(String tenantId);

    @Query("{ 'tenant.id' : ?0 }")
    Page<Application> findAllByTenant(String tenantId, Pageable pageRequest);
    
    @Query("{ 'tenant.id' : ?0, '_id' : ?1  }")
    Application findByTenantAndName(String tenantId, String name);

}
