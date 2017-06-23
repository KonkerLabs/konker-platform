package com.konkerlabs.platform.registry.data.services.routes.api;

import java.util.List;
import java.util.concurrent.Future;

import org.springframework.scheduling.annotation.Async;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;

public interface EventRouteExecutor {

    @Async
    Future<List<Event>> execute(Event incomingEvent, Device device);

}
