package com.konkerlabs.platform.registry.business.repositories;

import com.konkerlabs.platform.registry.business.model.LoginAudit;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginAuditRepository extends MongoRepository<LoginAudit, String> {
}
