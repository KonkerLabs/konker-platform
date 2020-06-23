package org.konker.registry.cassandraetl.services;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.repositories.ApplicationRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.repositories.events.EventRepositoryCassandraImpl;
import com.konkerlabs.platform.registry.business.repositories.events.EventRepositoryMongoImpl;
import org.konker.registry.cassandraetl.config.EqualizeConfig;
import org.konker.registry.cassandraetl.repositories.EventRepositoryCassandraEtlImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class EqualizeCassandraTablesService {

    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private static final String INCOMING_EVENTS = "incoming_events";
    private static final String INCOMING_EVENTS_CHANNEL = "incoming_events_channel";
    private static final String INCOMING_EVENTS_DEVICE_GUID = "incoming_events_device_guid";
    private static final String INCOMING_EVENTS_DEVICE_GUID_CHANNEL = "incoming_events_device_guid_channel";
    private static final String INCOMING_EVENTS_LOCATION_GUID = "incoming_events_location_guid";
    private static final String INCOMING_EVENTS_LOCATION_GUID_CHANNEL = "incoming_events_location_guid_channel";
    private static final String INCOMING_EVENTS_LOCATION_GUID_DEVICE_GUID = "incoming_events_location_guid_device_guid";
    private static final String INCOMING_EVENTS_LOCATION_GUID_DEVICE_GUID_CHANNEL = "incoming_events_location_guid_deviceguid_channel";

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private EventRepositoryCassandraEtlImpl cassandraEventsRepository;

    @Autowired
    private EqualizeConfig equalizeConfig;

    Integer maxSize = 0;

    public void equilize() throws BusinessException {

        LOGGER.info("Starting...");

        int count = 0;

        Tenant tenant = tenantRepository.findByDomainName(equalizeConfig.getTenant());
        process(tenant,
                equalizeConfig.getApplication(),
                equalizeConfig.getDevice(),
                equalizeConfig.getChannel(),
                equalizeConfig.getLocation(),
                Instant.ofEpochMilli(equalizeConfig.getTimestampStart()),
                Instant.ofEpochMilli(equalizeConfig.getTimestampEnd()));

        LOGGER.info("Finished! Tenants processed: " + count);

    }

    private void process(Tenant tenant,
                         String applicationName,
                         String deviceGuid,
                         String channel,
                         String locationGuid,
                         Instant startInstant,
                         Instant endInstant) throws BusinessException {

        Integer limit = 30000;
        Application application = applicationRepository.findByTenantAndName(tenant.getId(), applicationName);

        LOGGER.info("Tenant {} Application {}", tenant.getName(), application.getName());

        List<Event> incomingEvents = cassandraEventsRepository.findIncomingBy(
                tenant,
                application,
                null,
                null,
                null,
                startInstant,
                endInstant,
                false,
                limit);
        LOGGER.info("\tIncoming events: {}", incomingEvents.size());

        List<Event> incomingEventsChannel = cassandraEventsRepository.findIncomingBy(
                tenant,
                application,
                null,
                null,
                channel,
                startInstant,
                endInstant,
                false,
                limit);
        LOGGER.info("\tIncoming events channel: {}", incomingEventsChannel.size());

        List<Event> incomingEventsDeviceGuid = cassandraEventsRepository.findIncomingBy(
                tenant,
                application,
                deviceGuid,
                null,
                null,
                startInstant,
                endInstant,
                false,
                limit);
        LOGGER.info("\tIncoming events device guid: {}", incomingEventsDeviceGuid.size());

        List<Event> incomingEventsDeviceGuidChannel = cassandraEventsRepository.findIncomingBy(
                tenant,
                application,
                deviceGuid,
                null,
                channel,
                startInstant,
                endInstant,
                false,
                limit);
        LOGGER.info("\tIncoming events device guid channel: {}", incomingEventsDeviceGuidChannel.size());

        List<Event> incomingEventsLocationGuid = cassandraEventsRepository.findIncomingBy(
                tenant,
                application,
                null,
                locationGuid,
                null,
                startInstant,
                endInstant,
                false,
                limit);
        LOGGER.info("\tIncoming events location guid: {}", incomingEventsLocationGuid.size());

        List<Event> incomingEventsLocationGuidChannel = cassandraEventsRepository.findIncomingBy(
                tenant,
                application,
                null,
                locationGuid,
                channel,
                startInstant,
                endInstant,
                false,
                limit);
        LOGGER.info("\tIncoming events location guid channel: {}", incomingEventsLocationGuidChannel.size());

        List<Event> incomingEventsLocationGuidDeviceGuid = cassandraEventsRepository.findIncomingBy(
                tenant,
                application,
                deviceGuid,
                locationGuid,
                null,
                startInstant,
                endInstant,
                false,
                limit);
        LOGGER.info("\tIncoming events location guid device guid: {}", incomingEventsLocationGuidDeviceGuid.size());

        List<Event> incomingEventsLocationGuidDeviceGuidChannel = cassandraEventsRepository.findIncomingBy(
                tenant,
                application,
                deviceGuid,
                locationGuid,
                channel,
                startInstant,
                endInstant,
                false,
                limit);
        LOGGER.info("\tIncoming events location guid device guid channel: {}", incomingEventsLocationGuidDeviceGuidChannel.size());

        Map<String, List<Event>> mapEvents = new HashMap<>();
        mapEvents.put(INCOMING_EVENTS, incomingEvents);
        mapEvents.put(INCOMING_EVENTS_CHANNEL, incomingEventsChannel);
        mapEvents.put(INCOMING_EVENTS_DEVICE_GUID, incomingEventsDeviceGuid);
        mapEvents.put(INCOMING_EVENTS_DEVICE_GUID_CHANNEL, incomingEventsDeviceGuidChannel);
        mapEvents.put(INCOMING_EVENTS_LOCATION_GUID, incomingEventsLocationGuid);
        mapEvents.put(INCOMING_EVENTS_LOCATION_GUID_CHANNEL, incomingEventsLocationGuidChannel);
        mapEvents.put(INCOMING_EVENTS_LOCATION_GUID_DEVICE_GUID, incomingEventsLocationGuidDeviceGuid);
        mapEvents.put(INCOMING_EVENTS_LOCATION_GUID_DEVICE_GUID_CHANNEL, incomingEventsLocationGuidDeviceGuidChannel);

        mapEvents.entrySet().stream()
                .forEach(mapEvent -> {
                    if (mapEvent.getValue().size() > maxSize) {
                        maxSize = mapEvent.getValue().size();
                    }
                });

        Set<Map.Entry<String, List<Event>>> emptyTables = mapEvents.entrySet().stream()
                .filter(item -> item.getValue().size() == 0)
                .collect(Collectors.toSet());

        List<Event> events = mapEvents.entrySet().stream()
                .filter(item -> item.getValue().size() == maxSize)
                .map(item -> item.getValue())
                .collect(Collectors.toList()).get(0);

        saveEventsInEmptyTables(tenant, application, events, emptyTables);

    }

    private void saveEventsInEmptyTables(Tenant tenant,
                                         Application application,
                                         List<Event> events,
                                         Set<Map.Entry<String,
                                                 List<Event>>> emptyTables) {
        emptyTables.stream()
                .forEach(emptyTable -> {
                    String table = emptyTable.getKey();
                    events.stream()
                            .forEach(event -> {
                                cassandraEventsRepository.saveEvent(tenant, application, event, table);
                            });
                });

    }

}
