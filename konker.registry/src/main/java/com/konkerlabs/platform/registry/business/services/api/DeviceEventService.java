package com.konkerlabs.platform.registry.business.services.api;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;

public interface DeviceEventService {
    void logEvent(String payload, String deviceId) throws BusinessException;
}
