package com.konkerlabs.platform.registry.business.services.api;

import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.model.enumerations.FirmwareUpdateStatus;

import java.util.List;

public interface DeviceFirmwareUpdateService {

    enum Validations {
        FIRMWARE_UPDATE_PENDING_STATUS_DOES_NOT_EXIST("service.device.firmware_update_pending.does_not_exist");

        public String getCode() {
            return code;
        }

        private String code;

        Validations(String code) {
            this.code = code;
        }
    }

    enum Messages {
        FIRMWARE_UPDATE_STATUS_CHANGED("service.device.firmware_update.updated");

        public String getCode() {
            return code;
        }

        private String code;

        Messages(String code) {
            this.code = code;
        }
    }

    ServiceResponse<DeviceFwUpdate> save(Tenant tenant, Application application, Device device, DeviceFirmware deviceFirmware);
    ServiceResponse<DeviceFwUpdate> setDeviceAsUpdated(Tenant tenant, Application application, Device device);
    ServiceResponse<List<DeviceFwUpdate>> findByVersion(Tenant tenant, Application application, String version);
    ServiceResponse<DeviceFwUpdate> findPendingFwUpdateByDevice(Tenant tenant, Application application,  Device device);
    ServiceResponse<DeviceFwUpdate> confirmFwUpdateByDevice(Tenant tenant, Application application,  Device device);


}