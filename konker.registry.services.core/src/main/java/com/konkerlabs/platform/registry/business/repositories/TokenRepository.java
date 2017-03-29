package com.konkerlabs.platform.registry.business.repositories;

import com.konkerlabs.platform.registry.business.model.Token;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

/**
 * Created by Felipe on 27/12/16.
 */
public interface TokenRepository extends MongoRepository<Token, String> {
    @Query("{ 'userEmail' : ?0, 'purpose' : ?1, 'isExpired' : false }")
    Token findByUserEmail(String userEmail, String purposeName);
}
