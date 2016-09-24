package com.konkerlabs.platform.registry.business.services.api;

import java.util.List;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Tenant;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

public interface DeviceRegisterService {

	enum Validations {
		DEVICE_ID_NULL("service.device.id.not_null"),
		DEVICE_GUID_NULL("service.device.guid.not_null"),
		DEVICE_ID_ALREADY_REGISTERED("service.device.id.already_registered"), 
		DEVICE_ID_DOES_NOT_EXIST("service.device.id.does_not_exist"), 
		DEVICE_GUID_DOES_NOT_EXIST("service.device.guid.does_not_exist"), 
		DEVICE_HAVE_EVENTROUTES("service.device.have_eventroutes"), 
		DEVICE_HAVE_ENRICHMENTS("service.device.have_enrichments");

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
        private Device device;
        @NonNull
        private String password;

    }

	/**
	 * Persists a new Device.
	 *
	 * If the deviceId already exists in this tenant, an error is created. The
	 * tenant must exist.
	 *
	 * @param tenant
	 * @param device
	 * @return
	 */
	NewServiceResponse<Device> register(Tenant tenant, Device device);

	/**
	 * Updates an already existent Tenant.
	 * 
	 * If the deviceId does not exist in this tenant, an error is created. The
	 * tenant must exist.
	 * 
	 * @param tenant
	 * @param device
	 * @return
	 */
	NewServiceResponse<Device> update(Tenant tenant, String guid, Device device);

	/**
	 * TODO @andre implement throwable flow Remove a device in logical way
	 * 
	 * @param guid
	 * @return NewServiceResponse<Device>
	 */
	NewServiceResponse<Device> remove(Tenant tenant, String guid);

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
	 * Returns a device by its deviceGuid and tenant.
	 * 
	 * If the device does not exist, returns an error
	 * 
	 * @param tenant
	 * @param guid
	 * @return
	 */
	NewServiceResponse<Device> getByDeviceGuid(Tenant tenant, String guid);

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
	Device findByTenantDomainNameAndDeviceId(String tenantDomainName, String deviceGuid);

	/**
	 * Enables or disables a device
	 * 
	 * If the device does not exist, returns an error
	 * 
	 * @param tenant
	 * @param guid
	 * @return
	 */
	NewServiceResponse<Device> switchEnabledDisabled(Tenant tenant, String guid);

	/**
	 * Generates a security token for an existing device
	 *
	 * @param tenant
	 * @param guid
	 * @return A random password used to create the token
	 */
	NewServiceResponse<DeviceSecurityCredentials> generateSecurityPassword(Tenant tenant, String guid);
}