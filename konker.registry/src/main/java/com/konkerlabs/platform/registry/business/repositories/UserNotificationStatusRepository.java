package com.konkerlabs.platform.registry.business.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.konkerlabs.platform.registry.business.model.UserNotificationStatus;

public interface UserNotificationStatusRepository extends MongoRepository<UserNotificationStatus, String> {

}
