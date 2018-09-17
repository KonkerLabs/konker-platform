package com.konkerlabs.platform.registry.data.core.services.routes.api;

import java.util.List;
import java.util.concurrent.Future;

import com.konkerlabs.platform.registry.business.model.EventRoute;
import org.springframework.scheduling.annotation.Async;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;

public interface EventRouteExecutor {

    String ECHO_CHANNEL = "_echo";
    String DEBUG_CHANNEL = "_debug";

    @Async
    Future<List<Event>> execute(Event incomingEvent, Device device);

    void execute(Event event, Device device, EventRoute eventRoute) throws Exception;

}
