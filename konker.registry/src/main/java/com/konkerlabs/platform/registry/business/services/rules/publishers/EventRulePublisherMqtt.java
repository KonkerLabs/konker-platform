package com.konkerlabs.platform.registry.business.services.rules.publishers;

import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.EventRule;
import com.konkerlabs.platform.registry.business.services.rules.api.EventRulePublisher;
import com.konkerlabs.platform.registry.integration.gateways.MqttMessageGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;

@Service("eventRulePublisherMqtt")
@Qualifier("eventRulePublisherMqtt")
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class EventRulePublisherMqtt implements EventRulePublisher {

    private MqttMessageGateway mqttMessageGateway;

    @Autowired
    public EventRulePublisherMqtt(MqttMessageGateway mqttMessageGateway) {
        this.mqttMessageGateway = mqttMessageGateway;
    }

    @Override
    public void send(Event outgoingEvent, EventRule.RuleActor outgoingRuleActor) {
        String destinationTopic = MessageFormat.format("konker/device/{0}/{1}",
                outgoingRuleActor.getUri().getPath(), outgoingRuleActor.getData().get("channel"));
        mqttMessageGateway.send(outgoingEvent.getPayload(), destinationTopic);
    }
}
