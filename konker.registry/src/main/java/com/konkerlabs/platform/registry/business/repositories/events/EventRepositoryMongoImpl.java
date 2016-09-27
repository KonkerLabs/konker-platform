package com.konkerlabs.platform.registry.business.repositories.events;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.DeviceRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
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

import java.text.MessageFormat;
import java.time.Instant;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Repository("mongoEvents")
public class EventRepositoryMongoImpl implements EventRepository {

    public static final String EVENTS_INCOMING_COLLECTION_NAME = "incomingEvents";
    public static final String EVENTS_OUTGOING_COLLECTION_NAME = "outgoingEvents";

    private enum Direction {
        INCOMING("incoming",EVENTS_INCOMING_COLLECTION_NAME),
        OUTGOING("outgoing",EVENTS_OUTGOING_COLLECTION_NAME);

        private String actorFieldName;
        private String collectionName;

        public String getActorFieldName() {
            return actorFieldName;
        }

        public String getCollectionName() {
            return collectionName;
        }

        Direction(String actorFieldName, String collectionName) {
            this.actorFieldName = actorFieldName;
            this.collectionName = collectionName;
        }
    }

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
    public void saveIncoming(Tenant tenant, Event event) throws BusinessException {
        doSave(tenant,event,Direction.INCOMING);
    }

    @Override
    public void saveOutgoing(Tenant tenant, Event event) throws BusinessException {
        doSave(tenant,event,Direction.OUTGOING);
    }

    private void doSave(Tenant tenant, Event event, Direction direction) throws BusinessException {
        Optional.ofNullable(tenant)
                .filter(tenant1 -> Optional.ofNullable(tenant1.getDomainName()).filter(s -> !s.isEmpty()).isPresent())
                .orElseThrow(() -> new BusinessException(CommonValidations.TENANT_NULL.getCode()));
        Optional.ofNullable(tenantRepository.findByDomainName(tenant.getDomainName()))
                .orElseThrow(() -> new BusinessException(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()));
        Optional.ofNullable(event)
                .orElseThrow(() -> new BusinessException(CommonValidations.RECORD_NULL.getCode()));
        Optional.ofNullable(event.getIncoming())
                .orElseThrow(() -> new BusinessException(Validations.EVENT_INCOMING_NULL.getCode()));
        Optional.ofNullable(event.getIncoming().getDeviceGuid()).filter(s -> !s.isEmpty())
                .orElseThrow(() -> new BusinessException(Validations.INCOMING_DEVICE_GUID_NULL.getCode()));
        Optional.ofNullable(event.getIncoming().getChannel()).filter(s -> !s.isEmpty())
                .orElseThrow(() -> new BusinessException(Validations.EVENT_INCOMING_CHANNEL_NULL.getCode()));

        Tenant existingTenant = tenantRepository.findByDomainName(tenant.getDomainName());

        Optional.ofNullable(
                deviceRepository.findByTenantAndGuid(existingTenant.getId(),event.getIncoming().getDeviceGuid())
        ).orElseThrow(() -> new BusinessException(Validations.INCOMING_DEVICE_ID_DOES_NOT_EXIST.getCode()));

        Optional.ofNullable(event.getTimestamp())
                .orElseThrow(() -> new BusinessException(Validations.EVENT_TIMESTAMP_NULL.getCode()));

        if (direction.equals(Direction.OUTGOING)) {
            Optional.ofNullable(event.getOutgoing())
                    .orElseThrow(() -> new BusinessException(Validations.EVENT_OUTGOING_NULL.getCode()));
            Optional.ofNullable(event.getOutgoing().getDeviceGuid()).filter(s -> !s.isEmpty())
                    .orElseThrow(() -> new BusinessException(Validations.OUTGOING_DEVICE_GUID_NULL.getCode()));
            Optional.ofNullable(event.getOutgoing().getChannel()).filter(s -> !s.isEmpty())
                    .orElseThrow(() -> new BusinessException(Validations.EVENT_INCOMING_CHANNEL_NULL.getCode()));

            Optional.ofNullable(
                    deviceRepository.findByTenantAndGuid(existingTenant.getId(),event.getIncoming().getDeviceGuid())
            ).orElseThrow(() -> new BusinessException(Validations.OUTGOING_DEVICE_ID_DOES_NOT_EXIST.getCode()));
        }

        event.getIncoming().setTenantDomain(tenant.getDomainName());

        DBObject incoming = new BasicDBObject();
        incoming.put("deviceGuid",event.getIncoming().getDeviceGuid());
        incoming.put("tenantDomain",event.getIncoming().getTenantDomain());
        incoming.put("channel",event.getIncoming().getChannel());

        DBObject toSave = new BasicDBObject();

        toSave.removeField("ts");
        toSave.put("ts", event.getTimestamp().toEpochMilli());
        toSave.put(Direction.INCOMING.getActorFieldName(), incoming);
        toSave.put("payload", event.getPayload());

        if (direction.equals(Direction.OUTGOING)) {
            DBObject outgoing = new BasicDBObject();
            outgoing.put("deviceGuid",event.getOutgoing().getDeviceGuid());
            outgoing.put("tenantDomain",event.getOutgoing().getTenantDomain());
            outgoing.put("channel",event.getOutgoing().getChannel());

            toSave.put(Direction.OUTGOING.getActorFieldName(), outgoing);
        }

        mongoTemplate.save(toSave, direction.getCollectionName());
    }

    @Override
    public List<Event> findIncomingBy(Tenant tenant,
                                      String deviceGuid,
                                      Instant startInstant,
                                      Instant endInstant,
                                      Integer limit) throws BusinessException {
        return doFindBy(tenant,deviceGuid,startInstant,endInstant,limit,Direction.INCOMING);
    }

    @Override
    public List<Event> findOutgoingBy(Tenant tenant,
                                      String deviceGuid,
                                      Instant startInstant,
                                      Instant endInstant,
                                      Integer limit) throws BusinessException {
        return doFindBy(tenant,deviceGuid,startInstant,endInstant,limit,Direction.OUTGOING);
    }

    private List<Event> doFindBy(Tenant tenant,
                                 String deviceGuid,
                                 Instant startInstant,
                                 Instant endInstant,
                                 Integer limit,
                                 Direction direction) throws BusinessException {

        Optional.ofNullable(tenant)
                .filter(tenant1 -> Optional.ofNullable(tenant1.getDomainName()).filter(s -> !s.isEmpty()).isPresent())
                .orElseThrow(() -> new IllegalArgumentException("Tenant cannot be null"));
        Optional.ofNullable(deviceGuid).filter(s -> !s.isEmpty())
                .orElseThrow(() -> new IllegalArgumentException("Device ID cannot be null or empty"));

        if (!Optional.ofNullable(startInstant).isPresent() &&
                !Optional.ofNullable(limit).isPresent())
            throw new IllegalArgumentException("Limit cannot be null when start instant isn't provided");

        List<Criteria> criterias = new ArrayList<>();

        criterias.add(
            Criteria.where(MessageFormat.format("{0}.{1}",direction.getActorFieldName(),"deviceGuid"))
            .is(deviceGuid)
        );

        Optional.ofNullable(startInstant).ifPresent(instant -> criterias.add(Criteria.where("ts").gte(instant.toEpochMilli())));
        Optional.ofNullable(endInstant).ifPresent(instant -> criterias.add(Criteria.where("ts").lte(instant.toEpochMilli())));

        Query query = Query.query(
                Criteria.where(
                        MessageFormat.format("{0}.{1}",direction.getActorFieldName(),"tenantDomain")
                        ).is(tenant.getDomainName())
                        .andOperator(criterias.toArray(new Criteria[criterias.size()])));

        Optional.ofNullable(limit).filter(integer -> integer > 0).ifPresent(integer -> query.limit(integer));

        List<DBObject> result = mongoTemplate.find(query.with(new Sort(new Sort.Order(Sort.Direction.DESC, "ts"))),
                DBObject.class,
                direction.getCollectionName());

        return result.stream().map(dbObject -> {
            dbObject.removeField("_id");
            dbObject.removeField("_class");
            return dbObject;
        }).map(dbObject -> Event.builder()
                .incoming(
                        ((Supplier<Event.EventActor>) () -> {
                            DBObject incoming = ((DBObject) dbObject.get(direction.getActorFieldName()));
                            return Event.EventActor.builder()
                                    .deviceGuid(incoming.get("deviceGuid").toString())
                                    .tenantDomain(incoming.get("tenantDomain").toString())
                                    .channel(incoming.get("channel").toString()).build();
                        }).get()
                )
                .outgoing(
                    ((Supplier<Event.EventActor>) () -> {
                        return Optional.ofNullable((DBObject)dbObject.get(direction.getActorFieldName()))
                            .map(dbObject1 -> {
                                return Event.EventActor.builder()
                                        .deviceGuid(dbObject1.get("deviceGuid").toString())
                                        .tenantDomain(dbObject1.get("tenantDomain").toString())
                                        .channel(dbObject1.get("channel").toString()).build();
                            })
                            .orElse(null);
                    }).get()
                )
                .payload(dbObject.get("payload").toString())
                .timestamp(Instant.ofEpochMilli((Long) dbObject.get("ts")))
                .build())
        .collect(Collectors.toList());

    }
}
