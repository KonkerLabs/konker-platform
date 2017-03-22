package com.konkerlabs.platform.registry.business.repositories.events;


import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.DeviceRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.repositories.events.api.BaseEventRepositoryImpl;
import com.konkerlabs.platform.utilities.parsers.json.JsonParsingService;
import com.mongodb.util.JSON;
import lombok.Builder;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cassandra.core.Ordering;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.context.ApplicationContext;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository("cassandraEvents")
public class EventRepositoryCassandraImpl extends BaseEventRepositoryImpl {

    @Autowired
    private JsonParsingService jsonParsingService;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired(required = false)
    private CassandraTemplate cassandraTemplate;
    @Autowired(required = false)
    private CassandraOperations cassandraOperations;
    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    public static final String INCOMMINGTABLE = "incoming_events";
    public static final String OUTGOINGTABLE = "outgoing_events";
    private static final Logger LOG = LoggerFactory.getLogger(EventRepositoryCassandraImpl.class);

    @Override
    protected Event doSave(Tenant tenant, Event event, Type type) throws BusinessException {
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
                    deviceRepository.findByTenantAndGuid(existingTenant.getId(), event.getOutgoing().getDeviceGuid())
            ).orElseThrow(() -> new BusinessException(Validations.OUTGOING_DEVICE_ID_DOES_NOT_EXIST.getCode()));
        }

        event.getIncoming().setTenantDomain(tenant.getDomainName());
        cassandraOperations.insert(
                type.equals(Type.INCOMING)
                        ? CassandraIncommingEvent
                        .builder()
                        .tenantDomain(event.getIncoming().getTenantDomain())
                        .deviceGuid(event.getIncoming().getDeviceGuid())
                        .deviceId(event.getIncoming().getDeviceId())
                        .channel(event.getIncoming().getChannel())
                        .timestamp(event.getTimestamp())
                        .payload(event.getPayload())
                        .build()
                        : CassandraOutgoingEvent
                        .builder()
                        .tenantDomain(event.getIncoming().getTenantDomain())
                        .deviceGuid(event.getIncoming().getDeviceGuid())
                        .deviceId(event.getIncoming().getDeviceId())
                        .channel(event.getIncoming().getChannel())
                        .timestamp(event.getTimestamp())
                        .payload(event.getPayload())
                        .incomming(JSON.serialize(event.getIncoming()))
                        .build()


        );
        return event;
    }

    @Override
    protected List<Event> doFindBy(Tenant tenant,
                                   String deviceGuid,
                                   String channel,
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

        Select queryIncomming = QueryBuilder.select().from(INCOMMINGTABLE);
        queryIncomming.where(QueryBuilder.eq("tenant_domain", tenant.getDomainName()));
        queryIncomming.where(QueryBuilder.eq("device_guid", deviceGuid));
        Optional.ofNullable(channel).ifPresent(value -> {
            queryIncomming.where(QueryBuilder.eq("channel", value));
        });

        Optional.ofNullable(startInstant).ifPresent(value -> {
            queryIncomming.where(QueryBuilder.gte("timestamp", value));
        });
        Optional.ofNullable(endInstant).ifPresent(value -> {
            queryIncomming.where(QueryBuilder.lte("timestamp", value));
        });


        Select queryOutgoing = QueryBuilder.select().from(OUTGOINGTABLE);
        queryOutgoing.where(QueryBuilder.eq("tenant_domain", tenant.getDomainName()));
        queryOutgoing.where(QueryBuilder.eq("device_guid", deviceGuid));
        Optional.ofNullable(channel).ifPresent(value -> {
            queryOutgoing.where(QueryBuilder.eq("channel", value));
        });

        Optional.ofNullable(startInstant).ifPresent(value -> {
            queryOutgoing.where(QueryBuilder.gte("timestamp", value));
        });
        Optional.ofNullable(endInstant).ifPresent(value -> {
            queryOutgoing.where(QueryBuilder.lte("timestamp", value));
        });

        List<Event> events = new ArrayList<>();
        if(type.equals(Type.INCOMING)){
            List<CassandraIncommingEvent> incommingEvents =
                    cassandraOperations.select(queryIncomming, CassandraIncommingEvent.class);
            events.addAll(incommingEvents.stream().map(incoming -> {
                return Event.builder().incoming(
                        Event.EventActor.builder()
                                .deviceGuid(incoming.getDeviceGuid())
                                .tenantDomain(incoming.getTenantDomain())
                                .channel(incoming.getChannel())
                                .deviceId(incoming.getDeviceId())
                                .build())
                        .outgoing(null)
                        .payload(incoming.getPayload())
                        .timestamp(incoming.getTimestamp())
                        .build();
            }).collect(Collectors.toList()));

        }
        if(type.equals(Type.OUTGOING)){
            List<CassandraOutgoingEvent> outgoingEvents =
                    cassandraOperations.select(queryOutgoing, CassandraOutgoingEvent.class);

            events.addAll(outgoingEvents.stream().map(outgoing -> {
                return Event.builder().outgoing(
                        Event.EventActor.builder()
                                .deviceGuid(outgoing.getDeviceGuid())
                                .tenantDomain(outgoing.getTenantDomain())
                                .channel(outgoing.getChannel())
                                .deviceId(outgoing.getDeviceId())
                                .build())
                        .incoming(Optional.ofNullable(outgoing.getIncomming())
                                .map(incoming -> {
                                    try {
                                        Map<String, Object> incomingMap = jsonParsingService.toMap(outgoing.getIncomming());
                                        return Event.EventActor.builder()
                                                .deviceGuid(Optional.ofNullable(incomingMap.get("deviceGuid")).isPresent() ? incomingMap.get("deviceGuid").toString() : null)
                                                .tenantDomain(Optional.ofNullable(incomingMap.get("tenantDomain")).isPresent() ? incomingMap.get("tenantDomain").toString() : null)
                                                .channel(Optional.ofNullable(incomingMap.get("channel")).isPresent() ? incomingMap.get("channel").toString() : null)
                                                .deviceId(Optional.ofNullable(incomingMap.get("deviceId")).isPresent() ? incomingMap.get("deviceId").toString() : null)
                                                .build();
                                    } catch (JsonProcessingException e) {
                                        LOG.error("Error transforming incoming message : " + outgoing.getIncomming(), e);
                                    }
                                    return null;
                                }).orElse(null))
                        .payload(outgoing.getPayload())
                        .timestamp(outgoing.getTimestamp())
                        .build();
            }).collect(Collectors.toList()));
        }

        return events;
    }

    @Override
    protected void doRemoveBy(Tenant tenant, String deviceGuid, Type type) throws Exception {
        Optional.ofNullable(tenant)
                .filter(tenant1 -> Optional.ofNullable(tenant1.getDomainName()).filter(s -> !s.isEmpty()).isPresent())
                .orElseThrow(() -> new IllegalArgumentException("Tenant cannot be null"));
        Optional.ofNullable(deviceGuid).filter(s -> !s.isEmpty())
                .orElseThrow(() -> new IllegalArgumentException("Device ID cannot be null or empty"));

        Delete query = QueryBuilder.delete()
                .from(type.equals(Type.INCOMING) ? INCOMMINGTABLE : OUTGOINGTABLE)
                .where(QueryBuilder.eq("tenant_domain", tenant.getDomainName()))
                .and(QueryBuilder.eq("device_guid", deviceGuid))
                .ifExists();

        cassandraOperations.delete(query);
    }

}

@Data
@Builder
@Table(value = EventRepositoryCassandraImpl.INCOMMINGTABLE)
class CassandraIncommingEvent {
    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED, name = "device_guid", ordinal = 0)
    private String deviceGuid;
    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, name = "tenant_domain", ordinal = 1)
    private String tenantDomain;
    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, name = "channel", ordinal = 2)
    private String channel;
    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, name = "timestamp", ordering = Ordering.ASCENDING, ordinal = 3)
    private Instant timestamp;
    @Column("device_id")
    private String deviceId;
    @Column("deleted")
    private Boolean deleted;
    @Column("payload")
    private String payload;
}

@Data
@Builder
@Table(value = EventRepositoryCassandraImpl.OUTGOINGTABLE)
class CassandraOutgoingEvent {
    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED, name = "device_guid", ordinal = 0)
    private String deviceGuid;
    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, name = "tenant_domain", ordinal = 1)
    private String tenantDomain;
    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, name = "channel", ordinal = 2)
    private String channel;
    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, ordering = Ordering.ASCENDING, ordinal = 3)
    private Instant timestamp;
    @Column("device_id")
    private String deviceId;
    @Column("deleted")
    private Boolean deleted;
    @Column("payload")
    private String payload;
    @Column("incomming")
    private String incomming;
}
