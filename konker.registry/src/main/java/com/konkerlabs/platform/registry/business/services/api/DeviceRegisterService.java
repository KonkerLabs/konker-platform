package com.konkerlabs.platform.registry.business.services.api;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Tenant;

import java.util.List;

public interface DeviceRegisterService {
    ServiceResponse register(Tenant tenant, Device device) throws BusinessException;
    ServiceResponse update(String id, Device device) throws BusinessException;
    List<Device> getAll(Tenant tenant);

    // FIXME: deviceId should be unique within tenant, not globally
    Device findById(String id);
    Device findByApiKey(String apiKey);
}