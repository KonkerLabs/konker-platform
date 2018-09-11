package com.konkerlabs.platform.registry.data.core.services.publishers;

import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.repositories.DeviceModelRepository;
import com.konkerlabs.platform.registry.business.services.LocationTreeUtils;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.LocationSearchService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.data.core.integration.gateway.RabbitGateway;
import com.konkerlabs.platform.registry.data.core.services.publishers.api.EventPublisher;
import com.konkerlabs.platform.registry.data.core.services.api.DeviceLogEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service(DeviceModelLocation.URI_SCHEME)
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class EventPublisherModelLocation implements EventPublisher {

    private static final String EVENT_DROPPED = "Outgoing event has been dropped: [URI: {0}] - [Message: {1}]";

    private static final Logger LOGGER = LoggerFactory.getLogger(EventPublisherModelLocation.class);

    public static final String DEVICE_MQTT_CHANNEL = "channel";

    private RabbitGateway rabbitGateway;
    private DeviceRegisterService deviceRegisterService;
    private DeviceLogEventService deviceLogEventService;
    private DeviceModelRepository deviceModelRepository;
    private LocationSearchService locationSearchService;
    private EventPublisherDevice eventPublisherDevice;

    @Autowired
    public EventPublisherModelLocation(RabbitGateway rabbitGateway) {
        this.rabbitGateway = rabbitGateway;
    }

    @Autowired
    public void setDeviceRegisterService(DeviceRegisterService deviceRegisterService) {
        this.deviceRegisterService = deviceRegisterService;
    }

    @Autowired
    public void setDeviceLogEventService(DeviceLogEventService deviceLogEventService) {
        this.deviceLogEventService = deviceLogEventService;
    }

    @Autowired
    public void setDeviceModelRepository(DeviceModelRepository deviceModelRepository) {
        this.deviceModelRepository = deviceModelRepository;
    }

    @Autowired
    public void setLocationSearchService(LocationSearchService locationSearchService) {
        this.locationSearchService = locationSearchService;
    }

    @Autowired
    public void setEventPublisherDevice(EventPublisherDevice eventPublisherDevice) {
        this.eventPublisherDevice = eventPublisherDevice;
    }

    @Override
    public void send(Event outgoingEvent, URI destinationUri, Map<String, String> data, Tenant tenant, Application application)
            throws Exception {
        Optional.ofNullable(outgoingEvent)
                .orElseThrow(() -> new IllegalArgumentException("Event cannot be null"));
        Optional.ofNullable(destinationUri)
                .filter(uri -> !uri.toString().isEmpty())
                .orElseThrow(() -> new IllegalArgumentException("Destination URI cannot be null or empty"));
        Optional.ofNullable(data)
                .orElseThrow(() -> new IllegalArgumentException("Data cannot be null"));
        Optional.ofNullable(data.get(DEVICE_MQTT_CHANNEL))
                .filter(s -> !s.isEmpty())
                .orElseThrow(() -> new IllegalStateException("A valid MQTT channel is required"));
        Optional.ofNullable(tenant)
                .orElseThrow(() -> new IllegalArgumentException("Tenant cannot be null"));
        Optional.ofNullable(application)
                .orElseThrow(() -> new IllegalArgumentException("Application cannot be null"));


        String uriPath = destinationUri.getPath();
        if (uriPath.startsWith("/")) {
            uriPath = uriPath.substring(1);
        }

        String guids[] = uriPath.split("/");
        if (guids.length < 2) {
            LOGGER.warn("Invalid model location URI: {}", uriPath);
            return;
        }

        String deviceModelGuid = guids[0];
        String locationGuid = guids[1];

        DeviceModel deviceModel = deviceModelRepository.findByTenantIdApplicationNameAndGuid(tenant.getId(), application.getName(), deviceModelGuid);
        Optional.ofNullable(deviceModel)
                .orElseThrow(() -> new IllegalArgumentException("Device model cannot be null"));

        ServiceResponse<Location> locationService = locationSearchService.findByGuid(tenant, application, locationGuid);
        if (!locationService.isOk()) {
            Optional.ofNullable(deviceModel)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid location"));
        }

        Location location = locationService.getResult();
        locationService = locationSearchService.findByName(tenant, application, location.getName(), true);
        if (!locationService.isOk()) {
            Optional.ofNullable(deviceModel)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid location tree"));
        }

        List<Location> nodes = LocationTreeUtils.getNodesList(locationService.getResult());

        ServiceResponse<List<Device>> devicesResponse = deviceRegisterService.findAll(tenant, application);
        if (devicesResponse.isOk()) {
            List<Device> devices = devicesResponse.getResult();
            devices.parallelStream().forEach((outgoingDevice) -> {
                if (matchesModelLocation(outgoingDevice, deviceModel, nodes) &&
                        !isIncomingDevice(outgoingEvent, outgoingDevice)) {
                    if (outgoingDevice.isActive()) {
                        eventPublisherDevice.sendMessage(outgoingEvent, data, outgoingDevice);
                    }
                }
            });
        }

    }

    private boolean isIncomingDevice(Event outgoingEvent, Device outgoingDevice) {
        return outgoingEvent.getIncoming().getDeviceGuid().equals(outgoingDevice.getGuid());
    }

    private boolean matchesModelLocation(Device outgoingDevice, DeviceModel deviceModel, List<Location> nodes) {

        if (outgoingDevice.getDeviceModel() == null) {
            return false;
        }

        if (!outgoingDevice.getDeviceModel().getName().equals(deviceModel.getName())) {
            return false;
        }

        for (Location locations: nodes) {
            String deviceLocation = outgoingDevice.getLocation().getName();
            if (locations.getName().equals(outgoingDevice.getLocation().getName())) {
                return true;
            }
        }

        return false;

    }

}
