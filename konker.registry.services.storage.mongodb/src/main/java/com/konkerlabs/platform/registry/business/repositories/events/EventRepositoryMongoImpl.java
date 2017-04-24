package com.konkerlabs.platform.registry.business.repositories.events;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.DeviceRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.repositories.events.api.BaseEventRepositoryImpl;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Repository("mongoEvents")
public class EventRepositoryMongoImpl extends BaseEventRepositoryImpl {

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private DeviceRepository deviceRepository;

    @Override
    protected Event doSave(Tenant tenant, Application application, Event event, Type type) throws BusinessException {
        Optional.ofNullable(tenantRepository.findByDomainName(tenant.getDomainName()))
                .orElseThrow(() -> new BusinessException(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()));

        Tenant existingTenant = tenantRepository.findByDomainName(tenant.getDomainName());

        Optional.ofNullable(
                deviceRepository.findByTenantAndGuid(existingTenant.getId(), event.getIncoming().getDeviceGuid())
        ).orElseThrow(() -> new BusinessException(Validations.INCOMING_DEVICE_ID_DOES_NOT_EXIST.getCode()));

        Optional.ofNullable(event.getTimestamp())
                .orElseThrow(() -> new BusinessException(Validations.EVENT_TIMESTAMP_NULL.getCode()));

        if (type.equals(Type.OUTGOING)) {
            Optional.ofNullable(event.getOutgoing())
                    .orElseThrow(() -> new BusinessException(Validations.EVENT_OUTGOING_NULL.getCode()));
            Optional.ofNullable(event.getOutgoing().getDeviceGuid()).filter(s -> !s.isEmpty())
                    .orElseThrow(() -> new BusinessException(Validations.OUTGOING_DEVICE_GUID_NULL.getCode()));
            Optional.ofNullable(event.getOutgoing().getChannel()).filter(s -> !s.isEmpty())
                    .orElseThrow(() -> new BusinessException(Validations.EVENT_OUTGOING_CHANNEL_NULL.getCode()));

            Optional.ofNullable(
                    deviceRepository.findByTenantAndGuid(existingTenant.getId(),event.getOutgoing().getDeviceGuid())
            ).orElseThrow(() -> new BusinessException(Validations.OUTGOING_DEVICE_ID_DOES_NOT_EXIST.getCode()));
        }

        event.getIncoming().setTenantDomain(tenant.getDomainName());

        DBObject incoming = new BasicDBObject();
        incoming.put("deviceGuid",event.getIncoming().getDeviceGuid());
        incoming.put("tenantDomain",event.getIncoming().getTenantDomain());
        incoming.put("applicationName",event.getIncoming().getApplicationName());
        incoming.put("channel",event.getIncoming().getChannel());
        incoming.put("deviceId", event.getIncoming().getDeviceId());

        DBObject toSave = new BasicDBObject();

        toSave.removeField("ts");
        toSave.put("ts", event.getTimestamp().toEpochMilli());
        toSave.put(Type.INCOMING.getActorFieldName(), incoming);
        toSave.put("payload", event.getPayload());

        if (type.equals(Type.OUTGOING)) {
            DBObject outgoing = new BasicDBObject();
            outgoing.put("deviceGuid",event.getOutgoing().getDeviceGuid());
            outgoing.put("tenantDomain",event.getOutgoing().getTenantDomain());
            outgoing.put("applicationName",event.getOutgoing().getApplicationName());
            outgoing.put("channel",event.getOutgoing().getChannel());
            outgoing.put("deviceId", event.getOutgoing().getDeviceId());

            toSave.put(Type.OUTGOING.getActorFieldName(), outgoing);
        }

        mongoTemplate.save(toSave, type.getCollectionName());

        return event;
    }

    protected List<Event> doFindBy(Tenant tenant,
                                 Application application,
                                 String deviceGuid,
                                 String channel,
                                 Instant startInstant,
                                 Instant endInstant,
                                 boolean ascending,
                                 Integer limit,
                                 Type type,
                                 boolean isDeleted) throws BusinessException {

        List<Criteria> criterias = new ArrayList<>();

        Optional.ofNullable(deviceGuid).ifPresent(instant -> criterias.add(Criteria.where(MessageFormat.format("{0}.{1}", type.getActorFieldName(),"deviceGuid")).is(deviceGuid)));
        Optional.ofNullable(startInstant).ifPresent(instant -> criterias.add(Criteria.where("ts").gt(instant.toEpochMilli())));
        Optional.ofNullable(endInstant).ifPresent(instant -> criterias.add(Criteria.where("ts").lte(instant.toEpochMilli())));
        Optional.ofNullable(isDeleted)
                .ifPresent(deleted -> criterias.add(Criteria.where("deleted").exists(deleted)));
        Optional.ofNullable(channel)
                .ifPresent(ch -> {
                    criterias.add(
                            Criteria.where(MessageFormat.format("{0}.{1}", type.getActorFieldName(),"channel"))
                                    .is(ch)
                    );
                });

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
                            return Optional.ofNullable((DBObject) dbObject.get(Type.INCOMING.getActorFieldName()))
                                    .map(dbObject1 -> {
                                        return Event.EventActor.builder()
                                                .deviceGuid(Optional.ofNullable(dbObject1.get("deviceGuid")).isPresent() ? dbObject1.get("deviceGuid").toString() : null)
                                                .tenantDomain(Optional.ofNullable(dbObject1.get("tenantDomain")).isPresent() ? dbObject1.get("tenantDomain").toString() : null)
                                                .applicationName(Optional.ofNullable(dbObject1.get("applicationName")).isPresent() ? dbObject1.get("applicationName").toString() : null)
                                                .channel(Optional.ofNullable(dbObject1.get("channel")).isPresent() ? dbObject1.get("channel").toString() : null)
                                                .deviceId(Optional.ofNullable(dbObject1.get("deviceId")).isPresent() ? dbObject1.get("deviceId").toString() : null)
                                                .build();
                                    }).orElse(null);
                        }).get()
                )
                .outgoing(
                    ((Supplier<Event.EventActor>) () -> {
                        return Optional.ofNullable((DBObject)dbObject.get(Type.OUTGOING.getActorFieldName()))
                            .map(dbObject1 -> {
                                return Event.EventActor.builder()
                                        .deviceGuid(Optional.ofNullable(dbObject1.get("deviceGuid")).isPresent() ? dbObject1.get("deviceGuid").toString() : null)
                                        .tenantDomain(Optional.ofNullable(dbObject1.get("tenantDomain")).isPresent() ? dbObject1.get("tenantDomain").toString() : null)
                                        .applicationName(Optional.ofNullable(dbObject1.get("applicationName")).isPresent() ? dbObject1.get("applicationName").toString() : null)
                                        .channel(Optional.ofNullable(dbObject1.get("channel")).isPresent() ? dbObject1.get("channel").toString() : null)
                                        .deviceId(Optional.ofNullable(dbObject1.get("deviceId")).isPresent() ? dbObject1.get("deviceId").toString() : null)
                                        .build();
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
     * @param application
     * @param deviceGuid
     * @param type
     * @throws Exception
     */
    protected void doRemoveBy(Tenant tenant, Application application, String deviceGuid, Type type) throws Exception {

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
