package com.konkerlabs.platform.registry.business.model;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Document(collection = "deviceFirmwares")
public class DeviceFirmware {

    public enum Validations {

        FIRMWARE_NULL("model.device_firmware.firmware_null"),
        INVALID_CHECKSUM("model.device_firmware.invalid_checksum"),
        INVALID_VERSION("model.device_firmware.invalid_version");

        public String getCode() {
            return code;
        }

        private String code;

        Validations(String code) {
            this.code = code;
        }

    }

    @Id
    private String id;
    @DBRef
    private Tenant tenant;
    @DBRef
    private Application application;
    @DBRef
    private DeviceModel deviceModel;
    private String version;
    private String description;
    private Binary firmware;
    private Instant uploadDate;

    public Optional<Map<String, Object[]>> applyValidations() {

        Map<String, Object[]> validations = new HashMap<>();

        if (getVersion() == null || !getVersion().matches("([0-9]+)\\.([0-9]+)\\.([0-9]+)(?:(\\-[0-9A-Za-z-]+(?:\\.[0-9A-Za-z-]+)*))?(?:\\+[0-9A-Za-z-\\-\\.]+)?")) {
            validations.put(Validations.INVALID_VERSION.getCode(), null);
        }
        if (getFirmware() == null || getFirmware().getData() == null) {
            validations.put(Validations.FIRMWARE_NULL.getCode(), null);
        }

        return Optional.of(validations).filter(map -> !map.isEmpty());

    }

}
