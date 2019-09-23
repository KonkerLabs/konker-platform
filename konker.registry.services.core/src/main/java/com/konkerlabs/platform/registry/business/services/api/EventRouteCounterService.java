package com.konkerlabs.platform.registry.business.services.api;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.EventRoute;
import com.konkerlabs.platform.registry.business.model.EventRouteCounter;
import com.konkerlabs.platform.registry.business.model.Tenant;

import java.time.Instant;

public interface EventRouteCounterService {

    enum Validations {
        EVENT_ROUTE_COUNTER_NULL("service.event_route_counter.null"),
        EVENT_ROUTE_NULL("service.event_route_counter.event_route_null");

        private String code;

        public String getCode() {
            return code;
        }

        Validations(String code) {
            this.code = code;
        }
    }

    ServiceResponse<EventRouteCounter> save(Tenant tenant, Application application, EventRouteCounter eventRouteCounter);
    ServiceResponse<EventRouteCounter> getByEventRouteAndCreationDate(Tenant tenant, Application application, EventRoute eventRoute, Instant creationDate);

}