package com.konkerlabs.platform.registry.api.model;

import com.konkerlabs.platform.registry.api.model.core.SerializableVO;
import com.konkerlabs.platform.registry.business.model.HealthAlert;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@ApiModel(
        value = "DeviceHealthAlert",
        discriminator = "com.konkerlabs.platform.registry.api.model")
public class DeviceHealthAlertVO implements SerializableVO<HealthAlert, DeviceHealthAlertVO> {

	@ApiModelProperty(value = "the severity status", position = 0, example = "1cfc3f38-497e-4ff3-b5b4-c04940c53c6d")
	private String guid;

    @ApiModelProperty(value = "the severity status", position = 1, example = "FAIL")
    private String severity;

    @ApiModelProperty(value = "the severity status", position = 2, example = "Device does not sent message for 5 minutes")
    private String description;

    @ApiModelProperty(value = "last update of device health", position = 3, example = "2017-04-05T13:55:20.150Z")
    private String occurenceDate;

    @ApiModelProperty(value = "the severity status", position = 4, example = "SILENCE")
    private String type;

    @ApiModelProperty(value = "the device guid identification", position = 5, example = "1cfc3f38-497e-4ff3-b5b4-c04940c22a9a")
    private String deviceGuid;

    @ApiModelProperty(value = "the trigger guid identification", position = 5, example = "1cfc3f38-497e-4ff3-b5b4-c04940c53c6c")
    private String triggerGuid;

    @Override
    public DeviceHealthAlertVO apply(HealthAlert t) {
        DeviceHealthAlertVO vo = new DeviceHealthAlertVO();
        vo.setGuid(t.getGuid());
        vo.setSeverity(t.getSeverity().name());
        vo.setOccurenceDate(t.getLastChange().toString());
        vo.setType(t.getType().name());
        vo.setDeviceGuid(t.getDeviceGuid());
        vo.setTriggerGuid(t.getTriggerGuid());
        return vo;
    }

	@Override
	public HealthAlert patchDB(HealthAlert t) {
		return null;
	}

}
