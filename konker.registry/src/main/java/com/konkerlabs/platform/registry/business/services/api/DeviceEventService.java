package com.konkerlabs.platform.registry.business.services.api;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Event;

public interface DeviceEventService {
    void logEvent(Event event, String deviceId) throws BusinessException;
}
