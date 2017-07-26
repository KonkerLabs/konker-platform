package com.konkerlabs.platform.registry.api.model;

import java.time.Instant;

import com.konkerlabs.platform.registry.api.model.core.SerializableVO;
import com.konkerlabs.platform.registry.business.model.DeviceFirmware;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@ApiModel(
        value = "Device Firmware",
        discriminator = "com.konkerlabs.platform.registry.api.model")
public class DeviceFirmwareVO implements SerializableVO<DeviceFirmware, DeviceFirmwareVO> {

    private String version;
    private Long uploadTimestamp;

    @Override
    public DeviceFirmwareVO apply(DeviceFirmware t) {

        DeviceFirmwareVO deviceFirmwareVO = new DeviceFirmwareVO();
        deviceFirmwareVO.setVersion(t.getVersion());
        deviceFirmwareVO.setUploadTimestamp(t.getUploadDate().toEpochMilli());

        return deviceFirmwareVO;

    }

    @Override
    public DeviceFirmware patchDB(DeviceFirmware t) {
        return t;
    }

}
