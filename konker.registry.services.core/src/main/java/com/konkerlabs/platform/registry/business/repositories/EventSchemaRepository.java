package com.konkerlabs.platform.registry.business.repositories;

import com.konkerlabs.platform.registry.business.model.EventSchema;
import com.konkerlabs.platform.registry.business.services.EventSchemaServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class EventSchemaRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Cacheable(value = "eventSchemaCache", keyGenerator = "customKeyGenerator")
	public EventSchema findByDeviceGuidChannel(String deviceGuid, String channel) {
        EventSchema existing = mongoTemplate.findOne(
                Query.query(Criteria.where("deviceGuid")
                        .is(deviceGuid).andOperator(Criteria.where("channel").is(channel))),
                EventSchema.class, EventSchemaServiceImpl.SchemaType.INCOMING.getCollectionName()
        );

        return existing;
    }

}