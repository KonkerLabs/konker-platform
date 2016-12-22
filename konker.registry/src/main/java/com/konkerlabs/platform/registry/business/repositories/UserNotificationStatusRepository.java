package com.konkerlabs.platform.registry.business.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.konkerlabs.platform.registry.business.model.UserNotificationStatus;

public interface UserNotificationStatusRepository extends MongoRepository<UserNotificationStatus, String> {

    @Query("{'destination' : ?0}")
    public UserNotificationStatus getByDestination(String destination);
}
