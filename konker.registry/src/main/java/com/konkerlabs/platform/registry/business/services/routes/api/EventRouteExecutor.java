package com.konkerlabs.platform.registry.business.services.routes.api;

import com.konkerlabs.platform.registry.business.model.Event;

import java.net.URI;
import java.util.List;
import java.util.concurrent.Future;

public interface EventRouteExecutor {

    Future<List<Event>> execute(Event incomingEvent, URI uri);
}
