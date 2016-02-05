package com.konkerlabs.platform.registry.business.services.rules.api;

import com.konkerlabs.platform.registry.business.model.Event;

import java.net.URI;

public interface EventRuleExecutor {

    void execute(Event incomingEvent, URI uri);
}
