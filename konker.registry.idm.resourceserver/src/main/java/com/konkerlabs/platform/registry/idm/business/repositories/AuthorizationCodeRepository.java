package com.konkerlabs.platform.registry.idm.business.repositories;

import com.konkerlabs.platform.registry.idm.business.model.AuthorizationCode;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface AuthorizationCodeRepository
        extends MongoRepository<AuthorizationCode, String> {}
