package com.konkerlabs.platform.registry.business.services.api;

import java.util.List;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Tenant;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

public interface DeviceRegisterService {

    enum Validations {
        DEVICE_ID_NULL("service.device.id.not_null"),
        DEVICE_ID_ALREADY_REGISTERED("service.device.id.already_registered"),
        DEVICE_ID_DOES_NOT_EXIST("service.device.id.does_not_exist");

        public String getCode() {
            return code;
        }

        private String code;

        Validations(String code) {
            this.code = code;
        }
    }

    @RequiredArgsConstructor
    @Getter
    class DeviceSecurityCredentials {
        @NonNull
        private String deviceId;
        @NonNull
        private String apiKey;
        @NonNull
        private String password;
    }

    /**
     * Persists a new Device.
     *
     * If the deviceId already exists in this tenant, an error is created. The tenant must exist.
     *
     * @param tenant
     * @param device
     * @return
     */
    NewServiceResponse<Device> register(Tenant tenant, Device device);

    /**
     * Updates an already existent Tenant.
     * 
     * If the deviceId does not exist in this tenant, an error is created. The tenant must exist. 
     * 
     * @param tenant
     * @param device
     * @return
     */
    NewServiceResponse<Device> update(Tenant tenant, String id, Device device);

    /**
     * Returns all devices (enabled or disabled) owned by the provided tenant.
     * 
     * If no device is owned by this tenant, returns an empty List instead.
     * 
     * @param tenant
     * @return
     */
    NewServiceResponse<List<Device>> findAll(Tenant tenant);

    /**
     * Returns a device by its deviceId and tenant.
     * 
     * If the device does not exist, returns an error
     * 
     * @param tenant
     * @param id
     * @return
     */
    NewServiceResponse<Device> getByDeviceId(Tenant tenant, String id);

    /**
     * Returns a device associated with the provided API Key.
     * 
     * If the device does not exist, returns an error
     * 
     * @param apiKey
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
    NewServiceResponse<Device> switchEnabledDisabled(Tenant tenant, String id);

    /**
     * Generates a security token for an existing device
     *
     * @param tenant
     * @param id
     * @return A random password used to create the token
     */
    NewServiceResponse<DeviceSecurityCredentials> generateSecurityPassword(Tenant tenant, String id);
}