package com.konkerlabs.platform.registry.business.repositories;

import com.konkerlabs.platform.registry.business.model.EventRouteCounter;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EventRouteCounterRepository extends MongoRepository<EventRouteCounter, String> {


}