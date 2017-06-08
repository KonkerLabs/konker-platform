package com.konkerlabs.platform.registry.api.model;

import java.util.Optional;

import com.konkerlabs.platform.registry.api.model.core.SerializableVO;
import com.konkerlabs.platform.registry.business.model.Device;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@ApiModel(
        value = "Device Stats",
        discriminator = "com.konkerlabs.platform.registry.api.model")
public class DeviceStatsVO implements SerializableVO<Device, DeviceStatsVO> {

    @ApiModelProperty(value = "the registration date of device", position = 0, example = "2017-04-05T13:55:20.150Z")
    private String registrationDate;
    
    @ApiModelProperty(value = "the last modification date of device", position = 1, example = "2017-04-05T13:55:20.150Z")
    private String lastModificationDate;
    
    @ApiModelProperty(value = "the last data received date of device", position = 2, example = "2017-04-05T13:55:20.150Z")
    private String lastDataReceivedDate;

    @Override
    public DeviceStatsVO apply(Device t) {
        DeviceStatsVO vo = new DeviceStatsVO();
        vo.setRegistrationDate(t.getRegistrationDate().toString());
        vo.setLastModificationDate(Optional.ofNullable(t.getLastModificationDate()).isPresent() ? t.getLastModificationDate().toString() : "");
        return vo;
    }

	@Override
	public Device patchDB(Device t) {
		return null;
	}
 
}
