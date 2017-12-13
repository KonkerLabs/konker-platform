package com.konkerlabs.platform.registry.data.services.publishers;

import java.net.URI;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.data.services.api.DeviceLogEventService;
import com.konkerlabs.platform.registry.data.services.publishers.api.EventPublisher;
import com.konkerlabs.platform.registry.integration.gateways.RabbitGateway;

@Service("device")
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class EventPublisherDevice implements EventPublisher {

    private static final String EVENT_DROPPED = "Outgoing event has been dropped: [URI: {0}] - [Message: {1}]";

    private static final Logger LOGGER = LoggerFactory.getLogger(EventPublisherDevice.class);

    public static final String DEVICE_MQTT_CHANNEL = "channel";

    private RabbitGateway rabbitGateway;
    private DeviceRegisterService deviceRegisterService;
    private DeviceLogEventService deviceLogEventService;

    @Autowired
    public EventPublisherDevice(RabbitGateway rabbitGateway,
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

        Device outgoingDevice = deviceRegisterService.findByTenantDomainNameAndDeviceGuid(
                destinationUri.getAuthority(),
                destinationUri.getPath().replaceAll("/", "")
        );

        Optional.ofNullable(outgoingDevice)
                .orElseThrow(() -> new IllegalArgumentException(
                        MessageFormat.format("Device is unknown : {0}", destinationUri.getPath())
                ));

        if (outgoingDevice.isActive()) {
            sendMessage(outgoingEvent, data, outgoingDevice);
        } else {
            LOGGER.debug(
                    MessageFormat.format(EVENT_DROPPED, destinationUri, outgoingEvent.getPayload()),
                    outgoingDevice.toURI(),
                    outgoingDevice.getLogLevel()
            );
        }
    }

    public void sendMessage(Event outgoingEvent, Map<String, String> data, Device outgoingDevice) {

        outgoingEvent = outgoingEvent.clone(); // sets the new outgoing to a new event instance
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
