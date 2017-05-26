package com.konkerlabs.platform.registry.business.services.api;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.DeviceCustomData;
import com.konkerlabs.platform.registry.business.model.Tenant;

public interface DeviceCustomDataService {

	enum Validations {
		DEVICE_CUSTOM_DATA_NULL("service.devicemodel.null"),
		DEVICE_CUSTOM_DATA_ALREADY_REGISTERED("service.devicemodel.already.registered"),
		DEVICE_CUSTOM_DATA_DOES_NOT_EXIST("service.devicemodel.does.not.exist"),
		DEVICE_CUSTOM_DATA_INVALID_JSON("service.device.custom_data.invalid_json");

		public String getCode() {
			return code;
		}

		private String code;

		Validations(String code) {
			this.code = code;
		}
	}

	public enum Messages {
		DEVICE_CUSTOM_DATA_REMOVED_SUCCESSFULLY("controller.devicemodel.removed.succesfully"),
		DEVICE_CUSTOM_DATA_REMOVED_UNSUCCESSFULLY("controller.devicemodel.removed.unsuccesfully");

		public String getCode() {
			return code;
		}

		private String code;

		Messages(String code) {
			this.code = code;
		}
	}

	ServiceResponse<DeviceCustomData> save(Tenant tenant, Application application, Device device, String jsonCustomData);
	ServiceResponse<DeviceCustomData> update(Tenant tenant, Application application, Device device, String jsonCustomData);
	ServiceResponse<DeviceCustomData> remove(Tenant tenant, Application application, Device device);
	ServiceResponse<DeviceCustomData> getByTenantApplicationAndDevice(Tenant tenant, Application application, Device device);

}