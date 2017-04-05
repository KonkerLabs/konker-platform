package com.konkerlabs.platform.registry.business.repositories;

import com.konkerlabs.platform.registry.business.model.Transformation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface TransformationRepository extends MongoRepository<Transformation,String> {
    @Query("{ 'tenant.id' : ?0 }")
    List<Transformation> findAllByTenantId(String tenantId);

    @Query("{ 'application.id' : ?0")
    List<Transformation> findAllByApplicationId(String id);

    @Query("{ 'tenant.id' : ?0, 'application.id' : ?1, 'name' : ?2 }")
    List<Transformation> findByName(String tenantId, String applicationId, String name);

    @Query("{ 'tenant.id' : ?0, 'guid' : ?1 }")
    Transformation findByTenantIdAndTransformationGuid(String id, String guid);
    
    @Query("{  'guid' : ?0 }")
    Transformation findByGuid(String guid);
}
