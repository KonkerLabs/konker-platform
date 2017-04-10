package com.konkerlabs.platform.registry.business.repositories;

import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.Transformation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface TransformationRepository extends MongoRepository<Transformation,String> {
    @Query("{ 'tenant.id' : ?0 }")
    List<Transformation> findAllByTenantId(String tenantId);

    @Query("{ 'tenant.id' : ?0, 'application.name' : ?1 }")
    List<Transformation> findAllByApplicationId(String tenantId, String applicationId);

    @Query("{ 'tenant.id' : ?0, 'application.name' : ?1, 'name' : ?2 }")
    List<Transformation> findByName(String tenantId, String applicationId, String name);

    @Query("{ 'tenant.id' : ?0, 'guid' : ?1, 'application.name' : ?2 }")
    Transformation findByTenantIdApplicationIdAndTransformationGuid(String id, String guid, String applicationId);
    
    @Query("{  'guid' : ?0 }")
    Transformation findByGuid(String guid);
}
