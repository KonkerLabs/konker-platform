package com.konkerlabs.platform.registry.business.services.api;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Tenant;

import java.time.Instant;
import java.util.List;

public interface DeviceEventService {

    enum Validations {
        LIMIT_NULL("service.device_events.limit.not_null");

        public String getCode() {
            return code;
        }

        private String code;

        Validations(String code) {
            this.code = code;
        }
    }

    void logEvent(Device device, String channel, Event event) throws BusinessException;

    /**
     * Return all existing device events with timestamp greater than or equals to provided starting timestamp
     *
     * @param tenant
     * @param deviceGuid
     * @param startingTimestamp
     * @param endTimestamp
     * @param limit
     * @return Found events
     */
    NewServiceResponse<List<Event>> findEventsBy(Tenant tenant,
                                                 String deviceGuid,
                                                 Instant startingTimestamp,
                                                 Instant endTimestamp,
                                                 Integer limit);

	NewServiceResponse<List<Event>> findLastEventBy(Tenant tenant, String deviceGuid);
}
