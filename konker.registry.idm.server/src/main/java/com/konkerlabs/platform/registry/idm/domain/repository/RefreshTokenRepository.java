package com.konkerlabs.platform.registry.idm.domain.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by andre on 19/05/17.
 */
public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {}
