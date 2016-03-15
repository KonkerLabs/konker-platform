package com.konkerlabs.platform.registry.business.services.api;

import java.util.List;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Tenant;

public interface DeviceRegisterService {
    /**
     * Persists a new Device.
     * 
     * If the deviceId already exists in this tenant, an error is created. The tenant must exist. 
     * 
     * @param tenant
     * @param device
     * @return
     */
    ServiceResponse<Device> register(Tenant tenant, Device device);

    /**
     * Updates an already existent Tenant.
     * 
     * If the deviceId does not exist in this tenant, an error is created. The tenant must exist. 
     * 
     * @param tenant
     * @param device
     * @return
     */
    ServiceResponse<Device> update(Tenant tenant, String id, Device device);

    /**
     * Returns all devices (enabled or disabled) owned by the provided tenant.
     * 
     * If no device is owned by this tenant, returns an empty List instead.
     * 
     * @param tenant
     * @return
     */
    ServiceResponse<List<Device>> findAll(Tenant tenant);

    /**
     * Returns a device by its deviceId and tenant.
     * 
     * If the device does not exist, returns an error
     * 
     * @param tenant
     * @param id
     * @return
     */
    ServiceResponse<Device> getByDeviceId(Tenant tenant, String id);

    /**
     * Returns a device associated with the provided API Key.
     * 
     * If the device does not exist, returns an error
     * 
     * @param tenant
     * @param id
     * @return
     */
    // TODO: should be moved to a KEYs service
    Device findByApiKey(String apiKey);

    // TODO This method must be extinguished when event route specialized URI
    // gets available
    Device findByTenantDomainNameAndDeviceId(String tenantDomainName, String deviceId);

    /**
     * Enables or disables a device
     * 
     * If the device does not exist, returns an error
     * 
     * @param tenant
     * @param id
     * @return
     */
    ServiceResponse<Device> switchEnabledDisabled(Tenant tenant, String id);
}