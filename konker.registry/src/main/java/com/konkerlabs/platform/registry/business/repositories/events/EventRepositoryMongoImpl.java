package com.konkerlabs.platform.registry.business.repositories.events;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.DeviceRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.utilities.parsers.json.JsonParsingService;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

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
                .orElseThrow(() -> new IllegalArgumentException(CommonValidations.TENANT_NULL.getCode()));
        Optional.ofNullable(tenantRepository.findByDomainName(tenant.getDomainName()))
                .orElseThrow(() -> new BusinessException(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()));
        Optional.ofNullable(event)
                .orElseThrow(() -> new IllegalArgumentException(CommonValidations.RECORD_NULL.getCode()));
        Optional.ofNullable(event.getDeviceGuid()).filter(s -> !s.isEmpty())
                .orElseThrow(() -> new BusinessException(DeviceRegisterService.Validations.DEVICE_GUID_NULL.getCode()));

        Tenant existingTenant = tenantRepository.findByDomainName(tenant.getDomainName());

        Optional.ofNullable(deviceRepository.findByTenantAndGuid(existingTenant.getId(),event.getDeviceGuid()))
                .orElseThrow(() -> new BusinessException(DeviceRegisterService.Validations.DEVICE_ID_DOES_NOT_EXIST.getCode()));
        Optional.ofNullable(event.getTimestamp())
                .orElseThrow(() -> new IllegalStateException(Validations.EVENT_TIMESTAMP_NULL.getCode()));

        DBObject toSave = new BasicDBObject();

        toSave.removeField("ts");
        toSave.put("ts", event.getTimestamp().toEpochMilli());
        toSave.put("deviceGuid", event.getDeviceGuid());
        toSave.put("channel", event.getChannel());
        toSave.put("payload", event.getPayload());
        toSave.put("tenantDomain", tenant.getDomainName());

        mongoTemplate.save(toSave, EVENTS_COLLECTION_NAME);
    }

    public List<Event> findBy(Tenant tenant, String deviceGuid, Instant startInstant, Instant endInstant, Integer limit) {

        Optional.ofNullable(tenant)
                .filter(tenant1 -> Optional.ofNullable(tenant1.getDomainName()).filter(s -> !s.isEmpty()).isPresent())
                .orElseThrow(() -> new IllegalArgumentException("Tenant cannot be null"));
        Optional.ofNullable(deviceGuid).filter(s -> !s.isEmpty())
                .orElseThrow(() -> new IllegalArgumentException("Device ID cannot be null or empty"));

        if (!Optional.ofNullable(startInstant).isPresent() &&
            !Optional.ofNullable(limit).isPresent())
                throw new IllegalArgumentException("Limit cannot be null when start instant isn't provided");

        List<Criteria> criterias = new ArrayList<>();

        criterias.add(Criteria.where("deviceGuid").is(deviceGuid));

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
                .deviceGuid(dbObject.get("deviceGuid").toString())
                .payload(dbObject.get("payload").toString())
                .timestamp(Instant.ofEpochMilli((Long) dbObject.get("ts")))
                .channel(dbObject.get("channel").toString())
                .build())
        .collect(Collectors.toList());
    }

	@Override
	public List<Event> findLastBy(Tenant tenant, String deviceGuid) {
		Optional.ofNullable(tenant)
			.filter(tenant1 -> Optional.ofNullable(tenant1.getDomainName()).filter(s -> !s.isEmpty()).isPresent())
			.orElseThrow(() -> new IllegalArgumentException("Tenant cannot be null"));
		Optional.ofNullable(deviceGuid).filter(s -> !s.isEmpty())
			.orElseThrow(() -> new IllegalArgumentException("Device ID cannot be null or empty"));
		
		List<Criteria> criterias = new ArrayList<>();

        criterias.add(Criteria.where("deviceGuid").is(deviceGuid));

        Query query = Query.query(
            Criteria.where("tenantDomain").is(tenant.getDomainName())
            .andOperator(criterias.toArray(new Criteria[criterias.size()]))).limit(1);

        List<DBObject> result = mongoTemplate.find(query.with(new Sort(new Sort.Order(Sort.Direction.DESC, "ts"))),
                DBObject.class,
                EVENTS_COLLECTION_NAME);

        return result.stream().map(dbObject -> {
            dbObject.removeField("_id");
            dbObject.removeField("_class");
            return dbObject;
        }).map(dbObject -> Event.builder()
                .deviceGuid(dbObject.get("deviceGuid").toString())
                .payload(dbObject.get("payload").toString())
                .timestamp(Instant.ofEpochMilli((Long) dbObject.get("ts")))
                .channel(dbObject.get("channel").toString())
                .build())
        .collect(Collectors.toList());
	}
}
