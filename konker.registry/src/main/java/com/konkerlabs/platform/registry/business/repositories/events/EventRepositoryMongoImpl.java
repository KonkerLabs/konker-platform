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
import org.springframework.data.mongodb.core.query.Update;
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

    private enum Type {
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

        Type(String actorFieldName, String collectionName) {
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
        doSave(tenant,event, Type.INCOMING);
    }

    @Override
    public void saveOutgoing(Tenant tenant, Event event) throws BusinessException {
        doSave(tenant,event, Type.OUTGOING);
    }

    private void doSave(Tenant tenant, Event event, Type type) throws BusinessException {
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

        if (type.equals(Type.OUTGOING)) {
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
        toSave.put(Type.INCOMING.getActorFieldName(), incoming);
        toSave.put("payload", event.getPayload());

        if (type.equals(Type.OUTGOING)) {
            DBObject outgoing = new BasicDBObject();
            outgoing.put("deviceGuid",event.getOutgoing().getDeviceGuid());
            outgoing.put("tenantDomain",event.getOutgoing().getTenantDomain());
            outgoing.put("channel",event.getOutgoing().getChannel());

            toSave.put(Type.OUTGOING.getActorFieldName(), outgoing);
        }

        mongoTemplate.save(toSave, type.getCollectionName());
    }

    @Override
    public List<Event> findIncomingBy(Tenant tenant,
                                      String deviceGuid,
                                      Instant startInstant,
                                      Instant endInstant,
                                      boolean ascending,
                                      Integer limit) throws BusinessException {
        return doFindBy(tenant,deviceGuid,startInstant,endInstant,ascending,limit, Type.INCOMING, false);
    }

    @Override
    public List<Event> findOutgoingBy(Tenant tenant,
                                      String deviceGuid,
                                      Instant startInstant,
                                      Instant endInstant,
                                      boolean ascending,
                                      Integer limit) throws BusinessException {
        return doFindBy(tenant,deviceGuid,startInstant,endInstant,ascending,limit, Type.OUTGOING, false);
    }

    @Override
    public void removeBy(Tenant tenant, String deviceGuid) throws BusinessException {
        try {
            doRemoveBy(tenant, deviceGuid, Type.INCOMING);
            doRemoveBy(tenant, deviceGuid, Type.OUTGOING);
        } catch (Exception e){
            throw new BusinessException(e.getMessage(), e);
        }
    }

    private List<Event> doFindBy(Tenant tenant,
                                 String deviceGuid,
                                 Instant startInstant,
                                 Instant endInstant,
                                 boolean ascending,
                                 Integer limit,
                                 Type type,
                                 boolean isDeleted) throws BusinessException {

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
            Criteria.where(MessageFormat.format("{0}.{1}", type.getActorFieldName(),"deviceGuid"))
            .is(deviceGuid)
        );

        Optional.ofNullable(startInstant).ifPresent(instant -> criterias.add(Criteria.where("ts").gte(instant.toEpochMilli())));
        Optional.ofNullable(endInstant).ifPresent(instant -> criterias.add(Criteria.where("ts").lte(instant.toEpochMilli())));
        Optional.ofNullable(isDeleted)
                .ifPresent(deleted -> criterias.add(Criteria.where("deleted").exists(deleted)));

        Query query = Query.query(
                Criteria.where(
                        MessageFormat.format("{0}.{1}", type.getActorFieldName(),"tenantDomain")
                        ).is(tenant.getDomainName())
                        .andOperator(criterias.toArray(new Criteria[criterias.size()])));

        Optional.ofNullable(limit).filter(integer -> integer > 0).ifPresent(integer -> query.limit(integer));

        Sort.Direction sort = ascending ? Sort.Direction.ASC : Sort.Direction.DESC;

        List<DBObject> result = mongoTemplate.find(query.with(new Sort(new Sort.Order(sort, "ts"))),
                DBObject.class,
                type.getCollectionName());

        return result.stream().map(dbObject -> {
            dbObject.removeField("_id");
            dbObject.removeField("_class");
            return dbObject;
        }).map(dbObject -> Event.builder()
                .incoming(
                        ((Supplier<Event.EventActor>) () -> {
                            DBObject incoming = ((DBObject) dbObject.get(type.getActorFieldName()));
                            return Event.EventActor.builder()
                                    .deviceGuid(incoming.get("deviceGuid").toString())
                                    .tenantDomain(incoming.get("tenantDomain").toString())
                                    .channel(incoming.get("channel").toString()).build();
                        }).get()
                )
                .outgoing(
                    ((Supplier<Event.EventActor>) () -> {
                        return Optional.ofNullable((DBObject)dbObject.get(type.getActorFieldName()))
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

    /**
     * Remove events from device in logical way
     * @param tenant
     * @param deviceGuid
     * @param type
     * @throws Exception
     */
    private void doRemoveBy(Tenant tenant, String deviceGuid, Type type) throws Exception {
        Optional.ofNullable(tenant)
                .filter(tenant1 -> Optional.ofNullable(tenant1.getDomainName()).filter(s -> !s.isEmpty()).isPresent())
                .orElseThrow(() -> new IllegalArgumentException("Tenant cannot be null"));
        Optional.ofNullable(deviceGuid).filter(s -> !s.isEmpty())
                .orElseThrow(() -> new IllegalArgumentException("Device ID cannot be null or empty"));

        List<Criteria> criterias = new ArrayList<>();

        criterias.add(
                Criteria.where(MessageFormat.format("{0}.{1}", type.getActorFieldName(),"deviceGuid"))
                        .is(deviceGuid)
        );
        Query query = Query.query(
                Criteria.where(
                        MessageFormat.format("{0}.{1}", type.getActorFieldName(),"tenantDomain")
                ).is(tenant.getDomainName())
                        .andOperator(criterias.toArray(new Criteria[criterias.size()])));

        Update update = new Update();
        update.set("deleted", true);
        mongoTemplate.updateMulti(query, update, DBObject.class, type.getCollectionName());
    }
}
