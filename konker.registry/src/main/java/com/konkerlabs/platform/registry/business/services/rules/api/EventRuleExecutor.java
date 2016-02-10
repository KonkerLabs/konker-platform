package com.konkerlabs.platform.registry.business.services.rules.api;

import com.konkerlabs.platform.registry.business.model.Event;

import java.net.URI;
import java.util.List;
import java.util.concurrent.Future;

public interface EventRuleExecutor {

    Future<List<Event>> execute(Event incomingEvent, URI uri);
}
