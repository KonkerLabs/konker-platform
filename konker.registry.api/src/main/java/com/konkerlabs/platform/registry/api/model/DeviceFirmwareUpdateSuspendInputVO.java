package com.konkerlabs.platform.registry.api.model;

import com.konkerlabs.platform.registry.api.model.core.SerializableVO;
import com.konkerlabs.platform.registry.business.model.DeviceFwUpdate;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DeviceFirmwareUpdateSuspendInputVO implements SerializableVO<DeviceFwUpdate, DeviceFirmwareUpdateSuspendInputVO> {

    @ApiModelProperty(value = "the device guid", example = "39a35764-5134-4003-8f1e-400959631618", position = 0)
    private String deviceGuid;

    @ApiModelProperty(value = "the firmware version", example = "1.0", position = 1)
    private String version;

    @ApiModelProperty(value = "update status", example = "SUSPEND", position = 1)
    private String status;

    @Override
    public DeviceFirmwareUpdateSuspendInputVO apply(DeviceFwUpdate deviceFwUpdate) {
        DeviceFirmwareUpdateSuspendInputVO vo = new DeviceFirmwareUpdateSuspendInputVO();
        vo.setDeviceGuid(deviceFwUpdate.getDeviceGuid());
        vo.setVersion(deviceFwUpdate.getVersion());
        vo.setStatus(deviceFwUpdate.getStatus().name());

        return vo;
    }

    @Override
    public DeviceFwUpdate patchDB(DeviceFwUpdate deviceFwUpdate) {
        DeviceFwUpdate fwUpdate = DeviceFwUpdate
                .builder()
                .build();

        return fwUpdate;
    }

}
