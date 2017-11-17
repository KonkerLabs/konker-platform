package com.konkerlabs.platform.registry.idm.business.repositories;

import com.konkerlabs.platform.registry.idm.business.model.RefreshToken;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {}
