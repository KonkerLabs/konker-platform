package com.konkerlabs.platform.registry.business.repositories;

import com.konkerlabs.platform.registry.business.model.Transformation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface TransformationRepository extends MongoRepository<Transformation,String> {
    @Query("{ 'tenant.id' : ?0 }")
    List<Transformation> findAllByTenantId(String tenantId);

    @Query("{ 'tenant.id' : ?0, 'name' : ?1 }")
    List<Transformation> findByName(String tenantId, String name);

    @Query("{ 'tenant.id' : ?0, 'id' : ?1 }")
    Transformation findByTenantIdAndTransformationId(String id, String id1);
}
