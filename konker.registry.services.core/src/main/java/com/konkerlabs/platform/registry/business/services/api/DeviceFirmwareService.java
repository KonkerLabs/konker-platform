package com.konkerlabs.platform.registry.business.services.api;

import java.util.List;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.DeviceFirmware;
import com.konkerlabs.platform.registry.business.model.DeviceModel;
import com.konkerlabs.platform.registry.business.model.Tenant;

public interface DeviceFirmwareService {

	enum Validations {

		FIRMWARE_ALREADY_REGISTERED("service.device_firmware.already_registered");

		public String getCode() {
			return code;
		}

		private String code;

		Validations(String code) {
			this.code = code;
		}

	}

    ServiceResponse<DeviceFirmware> save(Tenant tenant, Application application, DeviceFirmware deviceFirmware);

    ServiceResponse<List<DeviceFirmware>> listByDeviceModel(Tenant tenant, Application application, DeviceModel deviceModel);

}