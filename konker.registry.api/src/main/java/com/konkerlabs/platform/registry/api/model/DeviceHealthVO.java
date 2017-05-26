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
        value = "DeviceHealth",
        discriminator = "com.konkerlabs.platform.registry.api.model")
public class DeviceHealthVO implements SerializableVO<HealthAlert, DeviceHealthVO> {

    @ApiModelProperty(value = "the severity status", position = 0, example = "FAIL")
    private String severity;
    
    @ApiModelProperty(value = "last update of device health", position = 1, example = "2017-04-05T13:55:20.150Z")
    private String lastUpdate;

    @Override
    public DeviceHealthVO apply(HealthAlert t) {
        DeviceHealthVO vo = new DeviceHealthVO();
        vo.setSeverity(t.getSeverity().name());
        vo.setLastUpdate(t.getLastChange().toString());
        return vo;
    }

	@Override
	public HealthAlert patchDB(HealthAlert t) {
		return null;
	}
 
}
