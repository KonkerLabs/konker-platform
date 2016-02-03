package com.konkerlabs.platform.registry.business.repositories;

import com.konkerlabs.platform.registry.business.model.EventRule;
import com.sun.org.apache.xpath.internal.operations.String;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.net.URI;

public interface EventRuleRepository extends MongoRepository<EventRule,String> {

    @Query("{ 'incoming.uri' : ?0 }")
    EventRule findByIncomingURI(URI uri);

}