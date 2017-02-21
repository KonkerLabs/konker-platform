package com.konkerlabs.platform.registry.business.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.konkerlabs.platform.registry.business.model.LoginAudit;

@Repository
public interface LoginAuditRepository extends MongoRepository<LoginAudit, String> {
}
