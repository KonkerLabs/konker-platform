package com.konkerlabs.platform.registry.business.services.api;

import java.util.List;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Tenant;

public interface DeviceRegisterService {
    ServiceResponse<Device> register(Tenant tenant, Device device);

    ServiceResponse<Device> update(Tenant tenant, String id, Device device);

    List<Device> getAll(Tenant tenant);

    // Device findById(String id);
    ServiceResponse<Device> getById(Tenant tenant, String id);

    Device findByApiKey(String apiKey);

    // TODO This method must be extinguished when event route specialized URI
    // gets available
    Device findByTenantDomainNameAndDeviceId(String tenantDomainName, String deviceId);

    ServiceResponse<Device> switchActivation(Tenant tenant, String id);
}