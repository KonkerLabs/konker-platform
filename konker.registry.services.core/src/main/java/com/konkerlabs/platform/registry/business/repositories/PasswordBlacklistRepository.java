package com.konkerlabs.platform.registry.business.repositories;

import com.konkerlabs.platform.registry.business.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface PasswordBlacklistRepository
        extends MongoRepository<User.PasswordBlacklist, String> {

    @Query("{ 'text': 0 }")
    List<User.PasswordBlacklist> findMatches(String text);
}
