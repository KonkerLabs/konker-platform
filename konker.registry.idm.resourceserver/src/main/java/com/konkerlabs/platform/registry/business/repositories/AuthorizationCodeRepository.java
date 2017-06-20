package com.konkerlabs.platform.registry.business.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;


public interface AuthorizationCodeRepository
        extends MongoRepository<AuthorizationCode, String> {}
