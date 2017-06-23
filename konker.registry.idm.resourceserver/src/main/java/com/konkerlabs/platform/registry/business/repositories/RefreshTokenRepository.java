package com.konkerlabs.platform.registry.business.repositories;

import com.konkerlabs.platform.registry.business.model.RefreshToken;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {}
