package com.konkerlabs.platform.registry.business.repositories;

import com.konkerlabs.platform.registry.business.model.EventRouteCounter;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;

public interface EventRouteCounterRepository extends MongoRepository<EventRouteCounter, String> {


    @Query("{  'tenant.id' : ?0, 'application.name' : ?1, 'eventRoute.id' : ?2, 'creationDate' : { '$gte' : ?3, '$lte' : ?4 }}")
    EventRouteCounter getByEventRouteAndCreationDate(String tenantId,
                                                     String applicationName,
                                                     String eventRouteId,
                                                     Instant startDate,
                                                     Instant endDate);

}