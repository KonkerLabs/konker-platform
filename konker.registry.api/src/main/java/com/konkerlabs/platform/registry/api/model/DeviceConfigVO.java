package com.konkerlabs.platform.registry.api.model;

import com.konkerlabs.platform.registry.api.model.core.SerializableVO;
import com.konkerlabs.platform.registry.business.model.DeviceConfig;
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

    @ApiModelProperty(value = "the device model guid", example = "39a35764-5134-4003-8f1e-400959631618", position = 0)
    private String deviceModelGuid;
    @ApiModelProperty(value = "the device model name", example = "air conditioner", position = 1)
    private String deviceModel;
    @ApiModelProperty(value = "the location guid", example = "39a35764-5134-4003-8f1e-400959631618", position = 2)
    private String locationGuid;
    @ApiModelProperty(value = "the location name", example = "kitchen", position = 3)
    private String locationName;
    @ApiModelProperty(value = "json config", example = "{ 'code' : '670b6c9f2580' }", position = 4)
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
