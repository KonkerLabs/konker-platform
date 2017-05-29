package com.konkerlabs.platform.registry.idm.domain.repository;

import org.springframework.data.mongodb.repository.MongoRepository;


public interface AuthorizationCodeRepository
        extends MongoRepository<AuthorizationCode, String> {}
