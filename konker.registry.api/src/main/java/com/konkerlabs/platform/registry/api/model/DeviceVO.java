package com.konkerlabs.platform.registry.api.model;

import com.konkerlabs.platform.registry.api.model.core.SerializableVO;
import com.konkerlabs.platform.registry.business.model.Device;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Transient;

import java.util.List;
import java.util.stream.Collectors;


@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@ApiModel(
        value = "Device",
        discriminator = "com.konkerlabs.platform.registry.api.model")
public class DeviceVO extends DeviceInputVO
        implements SerializableVO<Device, DeviceVO> {

    @ApiModelProperty(value = "the device guid", position = 0)
    private String guid;

    @Override
    public DeviceVO apply(Device t) {
        DeviceVO vo = new DeviceVO();
        vo.setGuid(t.getGuid());
        vo.setName(t.getName());
        vo.setDescription(t.getDescription());
        vo.setActive(t.isActive());
        vo.setId(t.getDeviceId());
        return vo;
    }

    @Override
    public Device patchDB(Device t) {
        t.setActive(this.isActive());
        t.setName(this.getName());
        t.setDescription(this.getDescription());
        t.setId(this.getId());
        t.setGuid(this.getGuid());
        return t;
    }
}
