package com.konkerlabs.platform.registry.business.services.api;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;

import java.util.List;

public interface DeviceRegisterService {
    ServiceResponse register(Device device) throws BusinessException;
    ServiceResponse update(String deviceId, Device device) throws BusinessException;
    List<Device> getAll();

    // FIXME: deviceId should be unique within tenant, not globally
    Device findById(String deviceId);
    Device logEvent(String deviceId, String payload);
}