package com.konkerlabs.platform.registry.business.services.api;

import java.net.URI;
import java.util.List;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.EventRoute;
import com.konkerlabs.platform.registry.business.model.Tenant;

public interface EventRouteService {

    enum Validations {
        NAME_IN_USE("service.event_route.name.in_use"),
        GUID_NULL("service.event_route.guid.not_null"),
        EVENT_ROUTE_NOT_FOUND("service.event_route.not_found"),
        EVENT_ROUTE_URI_NULL("service.event_route.uri.not_null"),
        CROSS_APPLICATION("service.event_route.cross_application"),;

        private String code;

        public String getCode() {
            return code;
        }

        Validations(String code) {
            this.code = code;
        }
    }

    ServiceResponse<EventRoute> save(Tenant tenant, Application application, EventRoute route);
    ServiceResponse<EventRoute> update(Tenant tenant, Application application, String guid, EventRoute eventRoute);
    ServiceResponse<List<EventRoute>> getAll(Tenant tenant, Application application);
    ServiceResponse<EventRoute> getByGUID(Tenant tenant, Application application, String guid);
	ServiceResponse<List<EventRoute>> findByIncomingUri(URI uri);
	ServiceResponse<EventRoute> remove(Tenant tenant, Application application, String guid);

}