package com.konkerlabs.platform.registry.repositories;

import com.konkerlabs.platform.registry.model.Tenant;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TenantRepository extends MongoRepository<Tenant, String> {
}
