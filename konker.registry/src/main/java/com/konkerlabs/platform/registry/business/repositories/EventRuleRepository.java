package com.konkerlabs.platform.registry.business.repositories;

import com.konkerlabs.platform.registry.business.model.EventRule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.net.URI;
import java.util.List;

public interface EventRuleRepository extends MongoRepository<EventRule,String> {
    @Query("{ 'incoming.uri' : ?0 }")
    List<EventRule> findByIncomingURI(URI uri);
}