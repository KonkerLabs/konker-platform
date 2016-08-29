package com.konkerlabs.platform.registry.business.services.api;


import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;

public interface EnrichmentExecutor {

    enum Validations {
        INCOMING_EVENT_NULL("executor.enrichment.incoming_event.not_null"),
        INCOMING_DEVICE_NULL("executor.enrichment.incoming_device.not_null");

        private String code;

        public String getCode() {
            return code;
        }

        Validations(String code) {
            this.code = code;
        }
    }

    NewServiceResponse<Event> enrich(Event incomingEvent, Device device);
}
