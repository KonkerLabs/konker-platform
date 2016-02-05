package com.konkerlabs.platform.registry.business.services.rules.api;

import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.EventRule;

public interface EventRulePublisher {

    void send(Event outgoingEvent, EventRule.RuleActor outgoingRuleActor);
}
