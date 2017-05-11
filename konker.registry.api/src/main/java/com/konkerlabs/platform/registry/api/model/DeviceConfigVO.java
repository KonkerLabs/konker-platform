package com.konkerlabs.platform.registry.api.model;

import org.apache.commons.lang3.StringUtils;

import com.konkerlabs.platform.registry.api.model.core.SerializableVO;
import com.konkerlabs.platform.registry.business.model.DeviceConfig;
import com.konkerlabs.platform.registry.business.model.Location;
import com.konkerlabs.platform.registry.business.model.RestDestination;
import com.mongodb.util.JSON;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@ApiModel(
		value = "Device Config",
		discriminator = "com.konkerlabs.platform.registry.api.model")
public class DeviceConfigVO implements SerializableVO<DeviceConfig, DeviceConfigVO> {

    private String deviceModelGuid;
    private String deviceModel;
    private String locationGuid;
    @ApiModelProperty(value = "the location name of device", example = "br_sp", position = 4)
    private String locationName;
    private Object json;

    public DeviceConfigVO(DeviceConfig deviceConfig) {
        this.deviceModelGuid = deviceConfig.getDeviceModelGuid();
        this.deviceModel  = deviceConfig.getDeviceModel();
        this.locationGuid = deviceConfig.getLocationGuid();
        this.locationName = deviceConfig.getLocationName();
        this.json = JSON.parse(deviceConfig.getJson());
    }

    public DeviceConfigVO apply(DeviceConfig t) {
        return new DeviceConfigVO(t);
    }

    @Override
    public DeviceConfig patchDB(DeviceConfig model) {
        model.setDeviceModelGuid(model.getDeviceModelGuid());
        model.setDeviceModel(model.getDeviceModel());
        model.setLocationGuid(model.getLocationGuid());
        model.setLocationName(model.getLocationName());
        model.setJson(model.getJson());

        return model;
    }

}
