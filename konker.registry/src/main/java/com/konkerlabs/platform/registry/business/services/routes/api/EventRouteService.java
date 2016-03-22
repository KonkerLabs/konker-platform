package com.konkerlabs.platform.registry.business.services.routes.api;


import com.konkerlabs.platform.registry.business.model.EventRoute;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;

import java.net.URI;
import java.util.List;

public interface EventRouteService {
    ServiceResponse<EventRoute> save(Tenant tenant, EventRoute route);
    ServiceResponse<List<EventRoute>> getAll(Tenant tenant);
    ServiceResponse<EventRoute> getById(Tenant tenant, String id);
    ServiceResponse<List<EventRoute>> findByIncomingUri(URI uri);
}