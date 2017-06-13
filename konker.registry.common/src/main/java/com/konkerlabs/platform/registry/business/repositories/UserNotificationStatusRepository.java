package com.konkerlabs.platform.registry.business.repositories;

import com.konkerlabs.platform.registry.business.model.UserNotificationStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface UserNotificationStatusRepository extends MongoRepository<UserNotificationStatus, String> {

    @Query("{'destination' : ?0}")
    public UserNotificationStatus getByDestination(String destination);
}
