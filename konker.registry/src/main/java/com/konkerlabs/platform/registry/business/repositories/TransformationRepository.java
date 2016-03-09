package com.konkerlabs.platform.registry.business.repositories;

import com.konkerlabs.platform.registry.business.model.Transformation;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TransformationRepository extends MongoRepository<Transformation,String> {
}
