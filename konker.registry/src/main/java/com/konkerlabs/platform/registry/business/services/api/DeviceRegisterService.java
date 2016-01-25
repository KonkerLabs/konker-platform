package com.konkerlabs.platform.registry.business.services.api;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Device;

import java.util.List;

public interface DeviceRegisterService {
    ServiceResponse register(Device device) throws BusinessException;
    List<Device> getAll();
    Device findById(String deviceId);
}