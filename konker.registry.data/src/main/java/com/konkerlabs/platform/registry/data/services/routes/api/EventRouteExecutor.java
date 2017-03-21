package com.konkerlabs.platform.registry.data.services.routes.api;

import com.konkerlabs.platform.registry.business.model.Event;
import org.springframework.scheduling.annotation.Async;

import java.net.URI;
import java.util.List;
import java.util.concurrent.Future;

public interface EventRouteExecutor {

    @Async
    Future<List<Event>> execute(Event incomingEvent, URI uri);
}
