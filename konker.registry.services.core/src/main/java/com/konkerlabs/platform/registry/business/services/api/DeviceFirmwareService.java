package com.konkerlabs.platform.registry.business.services.api;

import java.util.List;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.DeviceFirmware;
import com.konkerlabs.platform.registry.business.model.DeviceModel;
import com.konkerlabs.platform.registry.business.model.Tenant;
import org.springframework.stereotype.Service;

@Service
public interface DeviceFirmwareService {

	enum Validations {

		FIRMWARE_ALREADY_REGISTERED("service.device_firmware.already_registered"),
		FIRMWARE_NOT_FOUND("service.device_firmware.not_found");

		public String getCode() {
			return code;
		}

		private String code;

		Validations(String code) {
			this.code = code;
		}

	}


	ServiceResponse<DeviceFirmware> save(Tenant tenant, Application application, DeviceFirmware deviceFirmware);

	ServiceResponse<DeviceFirmware> findByVersion(Tenant tenant, Application application, DeviceModel deviceModel, String version);

    ServiceResponse<List<DeviceFirmware>> listByDeviceModel(Tenant tenant, Application application, DeviceModel deviceModel);

}