package com.konkerlabs.platform.registry.data.services.publishers;

import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.repositories.DeviceModelRepository;
import com.konkerlabs.platform.registry.business.services.LocationTreeUtils;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.LocationSearchService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.data.services.api.DeviceLogEventService;
import com.konkerlabs.platform.registry.data.services.publishers.api.EventPublisher;
import com.konkerlabs.platform.registry.integration.gateways.RabbitGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.text.MessageFormat;
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

    @Autowired
    public EventPublisherModelLocation(RabbitGateway rabbitGateway,
                                       DeviceRegisterService deviceRegisterService) {
        this.rabbitGateway = rabbitGateway;
        this.deviceRegisterService = deviceRegisterService;
    }

    @Autowired
    public void setDeviceLogEventService(DeviceLogEventService deviceLogEventService) {
        this.deviceLogEventService = deviceLogEventService;
    }

    @Override
    public void send(Event outgoingEvent, URI destinationUri, Map<String, String> data, Tenant tenant, Application application) {
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

        String deviceModelGuid = "";
        String locationGuid = null;

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
            devicesResponse.getResult().parallelStream().forEach((outgoingDevice) -> {
                if (matches(outgoingDevice, deviceModel, nodes))
                sendMesage(outgoingEvent, data, outgoingDevice);
            });
        }

    }

    private boolean matches(Device outgoingDevice, DeviceModel deviceModel, List<Location> nodes) {

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

    private void sendMesage(Event outgoingEvent, Map<String, String> data, Device outgoingDevice) {

        if (outgoingDevice.isActive()) {
            outgoingEvent.setOutgoing(
                    Event.EventActor.builder()
                            .deviceGuid(outgoingDevice.getGuid())
                            .channel(data.get(DEVICE_MQTT_CHANNEL))
                            .tenantDomain(outgoingDevice.getTenant().getDomainName())
                            .applicationName(outgoingDevice.getApplication().getName())
                            .deviceId(outgoingDevice.getDeviceId())
                            .build()
            );

            rabbitGateway.sendEvent(outgoingDevice.getApiKey(), data.get(DEVICE_MQTT_CHANNEL), outgoingEvent.getPayload());

            ServiceResponse<Event> response = deviceLogEventService.logOutgoingEvent(outgoingDevice, outgoingEvent);

            if (!response.isOk())
                LOGGER.error("Failed to forward event to its destination",
                        response.getResponseMessages(),
                        outgoingDevice.toURI(),
                        outgoingDevice.getLogLevel());
        }

    }

}
