package com.konkerlabs.platform.registry.business.services.publishers.api;

import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Tenant;

import java.net.URI;
import java.util.Map;


public interface EventPublisher {

    void send(Event outgoingEvent, URI destinationUri, Map<String,String> data, Tenant tenant);
}
