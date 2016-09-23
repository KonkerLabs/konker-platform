package com.konkerlabs.platform.registry.business.repositories.events;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Tenant;

import java.time.Instant;
import java.util.List;

public interface EventRepository {

    void push(Tenant tenant, Event event) throws BusinessException;

    List<Event> findBy(Tenant tenant, String deviceId, Instant startInstant, Instant endInstant, Integer limit);
}
