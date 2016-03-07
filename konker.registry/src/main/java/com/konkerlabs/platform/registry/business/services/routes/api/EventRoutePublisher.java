package com.konkerlabs.platform.registry.business.services.routes.api;

import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.EventRoute;

public interface EventRoutePublisher {

    void send(Event outgoingEvent, EventRoute.RuleActor outgoingRuleActor);
}
