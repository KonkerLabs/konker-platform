package com.konkerlabs.platform.registry.business.repositories.events;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Tenant;

import java.time.Instant;
import java.util.List;

public interface EventRepository {

    enum Validations {
        EVENT_TIMESTAMP_NULL("repository.events.timestamp.not_null");

        private String code;

        public String getCode() {
            return code;
        }

        Validations(String code) {
            this.code = code;
        }
    }

    void push(Tenant tenant, Event event) throws BusinessException;

    List<Event> findBy(Tenant tenant, String deviceGuid, Instant startInstant, Instant endInstant, Integer limit);

	List<Event> findLastBy(Tenant tenant, String deviceGuid);
}
