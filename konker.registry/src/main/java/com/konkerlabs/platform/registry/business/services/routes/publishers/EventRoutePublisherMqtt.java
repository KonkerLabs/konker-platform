package com.konkerlabs.platform.registry.business.services.routes.publishers;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.EventRoute;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.routes.api.EventRoutePublisher;
import com.konkerlabs.platform.registry.integration.gateways.MqttMessageGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.Optional;

@Service("device")
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class EventRoutePublisherMqtt implements EventRoutePublisher {

    private static final String EVENT_DROPPED = "Outgoing event has been dropped: [URI: {0}] - [Message: {1}]";

    private static final Logger LOGGER = LoggerFactory.getLogger(EventRoutePublisherMqtt.class);

    private static final String MQTT_OUTGOING_TOPIC_TEMPLATE = "iot/{0}/{1}";

    private MqttMessageGateway mqttMessageGateway;
    private DeviceRegisterService deviceRegisterService;

    @Autowired
    public EventRoutePublisherMqtt(MqttMessageGateway mqttMessageGateway,
                                   DeviceRegisterService deviceRegisterService) {
        this.mqttMessageGateway = mqttMessageGateway;
        this.deviceRegisterService = deviceRegisterService;
    }

    @Override
    public void send(Event outgoingEvent, EventRoute.RuleActor outgoingRuleActor) {

        Device outgoingDevice = deviceRegisterService.findByTenantDomainNameAndDeviceId(
            outgoingRuleActor.getUri().getAuthority(),
            outgoingRuleActor.getUri().getPath().replaceAll("/","")
        );

        Optional.ofNullable(outgoingDevice)
            .orElseThrow(() -> new IllegalArgumentException(
                MessageFormat.format("Device is unknown : {0}",outgoingRuleActor.getUri().getPath())
            ));

        if (outgoingDevice.isActive()) {
            String destinationTopic = MessageFormat.format(MQTT_OUTGOING_TOPIC_TEMPLATE,
                    outgoingRuleActor.getUri().getPath().replaceAll("/",""), outgoingRuleActor.getData().get("channel"));
            mqttMessageGateway.send(outgoingEvent.getPayload(), destinationTopic);
        } else {
            LOGGER.debug(
                MessageFormat.format(EVENT_DROPPED,outgoingRuleActor.getUri(),outgoingEvent.getPayload())
            );
        }
    }
}
