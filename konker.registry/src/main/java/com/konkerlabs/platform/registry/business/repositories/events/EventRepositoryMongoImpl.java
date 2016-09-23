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
import com.mongodb.BasicDBObject;
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
import java.util.Objects;
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

        Optional.ofNullable(deviceRepository.findByTenantAndId(existingTenant.getId(),event.getDeviceId()))
                .orElseThrow(() -> new BusinessException("Device does not exists"));
        Optional.ofNullable(event.getTimestamp())
                .orElseThrow(() -> new IllegalStateException("Event timestamp cannot be null"));

        DBObject toSave = new BasicDBObject();

        toSave.removeField("ts");
        toSave.put("ts", event.getTimestamp().toEpochMilli());
        toSave.put("deviceId", event.getDeviceId());
        toSave.put("channel", event.getChannel());
        toSave.put("payload", event.getPayload());
        toSave.put("tenantDomain", tenant.getDomainName());

        mongoTemplate.save(toSave, EVENTS_COLLECTION_NAME);
    }

    public List<Event> findBy(Tenant tenant, String deviceId, Instant startInstant, Instant endInstant, Integer limit) {

        Optional.ofNullable(tenant)
                .filter(tenant1 -> Optional.ofNullable(tenant1.getDomainName()).filter(s -> !s.isEmpty()).isPresent())
                .orElseThrow(() -> new IllegalArgumentException("Tenant cannot be null"));
        Optional.ofNullable(deviceId).filter(s -> !s.isEmpty())
                .orElseThrow(() -> new IllegalArgumentException("Device ID cannot be null or empty"));

        if (!Optional.ofNullable(startInstant).isPresent() &&
            !Optional.ofNullable(limit).isPresent())
                throw new IllegalArgumentException("Limit cannot be null when start instant isn't provided");

        List<Criteria> criterias = new ArrayList<>();

        criterias.add(Criteria.where("deviceId").is(deviceId));

        Optional.ofNullable(startInstant).ifPresent(instant -> criterias.add(Criteria.where("ts").gte(instant.toEpochMilli())));
        Optional.ofNullable(endInstant).ifPresent(instant -> criterias.add(Criteria.where("ts").lte(instant.toEpochMilli())));

        Query query = Query.query(
            Criteria.where("tenantDomain").is(tenant.getDomainName())
            .andOperator(criterias.toArray(new Criteria[criterias.size()])));

        Optional.ofNullable(limit).filter(integer -> integer > 0).ifPresent(integer -> query.limit(integer));

        List<DBObject> result = mongoTemplate.find(query.with(new Sort(new Sort.Order(Sort.Direction.DESC, "ts"))),
                DBObject.class,
                EVENTS_COLLECTION_NAME);

        return result.stream().map(dbObject -> {
            dbObject.removeField("_id");
            dbObject.removeField("_class");
            return dbObject;
        }).map(dbObject -> Event.builder()
                .deviceId(dbObject.get("deviceId").toString())
                .payload(dbObject.get("payload").toString())
                .timestamp(Instant.ofEpochMilli((Long) dbObject.get("ts")))
                .channel(dbObject.get("channel").toString())
                .build())
        .collect(Collectors.toList());
    }
}
