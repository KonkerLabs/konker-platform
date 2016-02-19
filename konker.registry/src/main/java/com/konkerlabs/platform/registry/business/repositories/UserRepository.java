package com.konkerlabs.platform.registry.business.repositories;

import com.konkerlabs.platform.registry.business.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface UserRepository extends MongoRepository<User, String> {
}
