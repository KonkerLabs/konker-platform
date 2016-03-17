package com.konkerlabs.platform.registry.business.repositories;

import com.konkerlabs.platform.registry.business.model.SmsDestination;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SmsDestinationRepository extends MongoRepository<SmsDestination, String> {
}
