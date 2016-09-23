package com.konkerlabs.platform.registry.business.repositories.events;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.DeviceRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.utilities.parsers.json.JsonParsingService;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository("mongoEvents")
public class EventRepositoryMongoImpl implements EventRepository {

    public static final String EVENTS_COLLECTION_NAME = "deviceEvents";

    @Autowired
    private JsonParsingService jsonParsingService;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private DeviceRepository deviceRepository;

    @Override
    public void push(Tenant tenant, Event event) throws BusinessException {

        Optional.ofNullable(tenant)
                .filter(tenant1 -> Optional.ofNullable(tenant1.getDomainName()).filter(s -> !s.isEmpty()).isPresent())
                .orElseThrow(() -> new IllegalArgumentException("Tenant cannot be null"));
        Optional.ofNullable(tenantRepository.findByDomainName(tenant.getDomainName()))
                .orElseThrow(() -> new BusinessException("Tenant does not exists"));
        Optional.ofNullable(event)
                .orElseThrow(() -> new IllegalArgumentException("Event cannot be null"));
        Optional.ofNullable(event.getDeviceId()).filter(s -> !s.isEmpty())
                .orElseThrow(() -> new BusinessException("Device ID cannot be null or empty"));

        Tenant existingTenant = tenantRepository.findByDomainName(tenant.getDomainName());

        Optional.ofNullable(deviceRepository.findByTenantIdAndDeviceId(existingTenant.getId(),event.getDeviceId()))
                .orElseThrow(() -> new BusinessException("Device does not exists"));
        Optional.ofNullable(event.getTimestamp())
                .orElseThrow(() -> new IllegalStateException("Event timestamp cannot be null"));

        DBObject toSave = (DBObject) JSON.parse(event.getPayload());

        toSave.removeField("ts");
        toSave.put("ts", event.getTimestamp().toEpochMilli());
        toSave.put("deviceId", event.getDeviceId());
        toSave.put("tenantDomain", tenant.getDomainName());

        mongoTemplate.save(toSave, EVENTS_COLLECTION_NAME);
    }

    @Override
    public List<Event> findBy(Tenant tenant, String deviceId, Long startingEpochMillis, Long endEpochMillis) {

        Optional.ofNullable(tenant)
                .filter(tenant1 -> Optional.ofNullable(tenant1.getDomainName()).filter(s -> !s.isEmpty()).isPresent())
                .orElseThrow(() -> new IllegalArgumentException("Tenant cannot be null"));
        Optional.ofNullable(deviceId).filter(s -> !s.isEmpty())
                .orElseThrow(() -> new IllegalArgumentException("Device ID cannot be null or empty"));
        Optional.ofNullable(startingEpochMillis)
                .orElseThrow(() -> new IllegalArgumentException("Starting offset cannot be null"));

        List<Criteria> criterias = new ArrayList<>();

        criterias.add(Criteria.where("deviceId").is(deviceId));
        criterias.add(Criteria.where("ts").gte(startingEpochMillis));

        Optional.ofNullable(endEpochMillis).ifPresent(aLong -> criterias.add(Criteria.where("ts").lte(aLong)));

        Query query = Query.query(
            Criteria.where("tenantDomain").is(tenant.getDomainName())
            .andOperator(criterias.toArray(new Criteria[criterias.size()])));

        List<DBObject> result = mongoTemplate.find(query.with(new Sort(new Sort.Order(Sort.Direction.ASC, "ts"))),
                DBObject.class,
                EVENTS_COLLECTION_NAME);

        return result.stream().map(dbObject -> {
            dbObject.removeField("_id");
            dbObject.removeField("_class");
            return dbObject;
        }).map(dbObject -> Event.builder()
                .deviceId(deviceId)
                .payload(JSON.serialize(dbObject))
                .timestamp(Instant.ofEpochMilli((Long) dbObject.get("ts")))
                .build())
        .collect(Collectors.toList());
    }
}
