package com.konkerlabs.platform.registry.business.services.api;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Tenant;
import lombok.*;

import java.util.List;
import java.util.Locale;

public interface DeviceRegisterService {

	enum Validations {
		DEVICE_ID_NULL("service.device.id.not_null"),
		DEVICE_GUID_NULL("service.device.guid.not_null"),
		DEVICE_ID_ALREADY_REGISTERED("service.device.id.already_registered"),
		DEVICE_ID_DOES_NOT_EXIST("service.device.id.does_not_exist"),
		DEVICE_GUID_DOES_NOT_EXIST("service.device.guid.does_not_exist"),
		DEVICE_HAVE_EVENTROUTES("service.device.have_eventroutes"),
		DEVICE_TENANT_LIMIT("service.device.tenant.limit"),
		EQUALS_ORIGIN_DESTINATION_APPLICATIONS("service.device.equals_origin_destination_applications");

		public String getCode() {
			return code;
		}

		private String code;

		Validations(String code) {
			this.code = code;
		}
	}

    enum Messages {
        DEVICE_REGISTERED_SUCCESSFULLY("controller.device.registered.success"),
        DEVICE_REMOVED_SUCCESSFULLY("controller.device.removed.succesfully"),
        DEVICE_REMOVED_UNSUCCESSFULLY("controller.device.removed.unsuccesfully"),
        DEVICE_QRCODE_ERROR("service.device.qrcode.have_errors");

        public String getCode() {
            return code;
        }

        private String code;

        Messages(String code) {
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

    @Data
    @Builder
    class DeviceDataURLs {
        private String httpURLPub;
        private String httpURLSub;
        private String httpsURLPub;
        private String httpsURLSub;
        private String mqttURL;
        private String mqttsURL;
        private String mqttPubTopic;
        private String mqttSubTopic;
    }

	/**
	 * Persists a new Device.
	 *
	 * If the deviceGuid already exists in this tenant, an error is created. The
	 * tenant must exist.
	 *
	 * @param tenant
	 * @param device
	 * @return
	 */
	ServiceResponse<Device> register(Tenant tenant, Application application, Device device);

	/**
	 * Updates an already existent Tenant.
	 *
	 * If the deviceGuid does not exist in this tenant, an error is created. The
	 * tenant must exist.
	 *
	 * @param tenant
	 * @param device
	 * @return
	 */
	ServiceResponse<Device> update(Tenant tenant, Application application, String guid, Device device);

	/**
	 * TODO @andre implement throwable flow Remove a device in logical way
	 *
	 * @param guid
	 * @return ServiceResponse<Device>
	 */
	ServiceResponse<Device> remove(Tenant tenant, Application application, String guid);

	/**
	 * Returns all devices (enabled or disabled) owned by the provided tenant.
	 *
	 * If no device is owned by this tenant, returns an empty List instead.
	 *
	 * @param tenant
	 * @return
	 */
	ServiceResponse<List<Device>> findAll(Tenant tenant, Application application);

	ServiceResponse<Long> countAll(Tenant tenant, Application application);

	/**
	 * Returns a device by its deviceGuid and tenant.
	 *
	 * If the device does not exist, returns an error
	 *
	 * @param tenant
	 * @param guid
	 * @return
	 */
	ServiceResponse<Device> getByDeviceGuid(Tenant tenant, Application application, String guid);

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


	/**
	 * Returns a device by its deviceGuid and tenant name.
	 *
	 * If the device does not exist, returns an error
	 *
	 * @param tenantDomainName
	 * @param deviceGuid
	 * @return
	 */
	Device findByTenantDomainNameAndDeviceGuid(String tenantDomainName, String deviceGuid);

	/**
	 * Enables or disables a device
	 *
	 * If the device does not exist, returns an error
	 *
	 * @param tenant
	 * @param guid
	 * @return
	 */
	ServiceResponse<Device> switchEnabledDisabled(Tenant tenant, Application application, String guid);

	/**
	 * Generates a security token for an existing device
	 *
	 * @param tenant
	 * @param guid
	 * @return A random password used to create the token
	 */
	ServiceResponse<DeviceSecurityCredentials> generateSecurityPassword(Tenant tenant, Application application, String guid);

	/**
	 * Return device URLs for publish and subscribe events
	 *
	 * @param tenant
	 * @param device
	 * @param locale
	 * @return
	 */
	ServiceResponse<DeviceDataURLs> getDeviceDataURLs(Tenant tenant, Application application, Device device, Locale locale);

	/**
	 * Generates a security token for an existing device
	 *
	 * @param credentials
	 * @param width
	 * @param height
	 * @return A random password used to create the token
	 */
	ServiceResponse<String> generateQrCodeAccess(DeviceSecurityCredentials credentials, int width, int height);

	/**
	 * Copies the device to another application from the same tenant and removes it from the previous application
	 *
	 * @param tenant
	 * @param originApplication
	 * @param guid
	 * @param destApplication
	 * @return The new device created
	 */
	ServiceResponse<Device> move(Tenant tenant, Application originApplication, String guid, Application destApplication);
	
	
	ServiceResponse<Device> findByDeviceId(Tenant tenant, Application application, String deviceId);

}