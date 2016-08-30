package com.konkerlabs.platform.registry.business.services.routes.api;


import com.konkerlabs.platform.registry.business.model.EventRoute;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.NewServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;

import java.net.URI;
import java.util.List;

public interface EventRouteService {

    enum Validations {
        INCOMING_OUTGOING_CHANNELS_SAME("service.event_route.incoming_outgoing_channels.same"),
        NAME_IN_USE("service.event_route.name.in_use"),
        GUID_NULL("service.event_route.guid.not_null"),
        EVENT_ROUTE_NOT_FOUND("service.event_route.not_found"),
        EVENT_ROUTE_URI_NULL("service.event_route.uri.not_null");

        private String code;

        public String getCode() {
            return code;
        }

        Validations(String code) {
            this.code = code;
        }
    }

    NewServiceResponse<EventRoute> save(Tenant tenant, EventRoute route);
    NewServiceResponse<EventRoute> update(Tenant tenant, String guid, EventRoute eventRoute);
    NewServiceResponse<List<EventRoute>> getAll(Tenant tenant);
    NewServiceResponse<EventRoute> getByGUID(Tenant tenant, String guid);
    NewServiceResponse<List<EventRoute>> findByIncomingUri(URI uri);
    NewServiceResponse<EventRoute> remove(Tenant tenant, String guid);
}