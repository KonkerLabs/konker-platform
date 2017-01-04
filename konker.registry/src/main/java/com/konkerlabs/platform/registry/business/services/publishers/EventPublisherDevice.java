package com.konkerlabs.platform.registry.business.services.publishers;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.publishers.api.EventPublisher;
import com.konkerlabs.platform.registry.integration.gateways.MqttMessageGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Optional;

@Service("device")
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class EventPublisherDevice implements EventPublisher {

    private static final String EVENT_DROPPED = "Outgoing event has been dropped: [URI: {0}] - [Message: {1}]";

    private static final Logger LOGGER = LoggerFactory.getLogger(EventPublisherDevice.class);

    private static final String MQTT_OUTGOING_TOPIC_TEMPLATE = "sub/{0}/{1}";

    public static final String DEVICE_MQTT_CHANNEL = "channel";

    private MqttMessageGateway mqttMessageGateway;
    private DeviceRegisterService deviceRegisterService;
    private DeviceEventService deviceEventService;

    @Autowired
    public EventPublisherDevice(MqttMessageGateway mqttMessageGateway,
                                DeviceRegisterService deviceRegisterService) {
        this.mqttMessageGateway = mqttMessageGateway;
        this.deviceRegisterService = deviceRegisterService;
    }

    @Autowired
    public void setDeviceEventService(DeviceEventService deviceEventService) {
        this.deviceEventService = deviceEventService;
    }

    @Override
    public void send(Event outgoingEvent, URI destinationUri, Map<String, String> data, Tenant tenant) {
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

        Device outgoingDevice = deviceRegisterService.findByTenantDomainNameAndDeviceGuid(
                destinationUri.getAuthority(),
                destinationUri.getPath().replaceAll("/", "")
        );

        Optional.ofNullable(outgoingDevice)
                .orElseThrow(() -> new IllegalArgumentException(
                        MessageFormat.format("Device is unknown : {0}", destinationUri.getPath())
                ));

        if (outgoingDevice.isActive()) {
            outgoingEvent.setOutgoing(
                    Event.EventActor.builder()
                            .deviceGuid(outgoingDevice.getGuid())
                            .channel(data.get(DEVICE_MQTT_CHANNEL))
                            .tenantDomain(outgoingDevice.getTenant().getDomainName())
                            .deviceId(outgoingDevice.getDeviceId())
                            .build()
            );
            String destinationTopic = MessageFormat.format(MQTT_OUTGOING_TOPIC_TEMPLATE,
                    outgoingDevice.getApiKey(), data.get(DEVICE_MQTT_CHANNEL));
            mqttMessageGateway.send(outgoingEvent.getPayload(), destinationTopic);
            ServiceResponse<Event> response = deviceEventService.logOutgoingEvent(outgoingDevice, outgoingEvent);

            if (!response.isOk())
                LOGGER.error("Failed to forward event to its destination",
                        response.getResponseMessages(),
                        outgoingEvent.getOutgoing().toURI());
        } else {
            LOGGER.debug(
                    MessageFormat.format(EVENT_DROPPED, destinationUri, outgoingEvent.getPayload()),
                    outgoingDevice.toURI()
            );
        }
    }
}
