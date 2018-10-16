package com.konkerlabs.platform.registry.api.model;

import java.util.Collections;

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
        value = "Device",
        discriminator = "com.konkerlabs.platform.registry.api.model")
public class DeviceVO extends DeviceInputVO
        implements SerializableVO<Device, DeviceVO> {

    @ApiModelProperty(value = "the device guid", position = 0, example = "818599ad-3502-4e70-a852-fc7af8e0a9f3")
    private String guid;

    @Override
    public DeviceVO apply(Device t) {
        DeviceVO vo = new DeviceVO();
        vo.setGuid(t.getGuid());
        vo.setName(t.getName());
        vo.setDescription(t.getDescription());
        vo.setLocationName(t.getLocation() != null ? t.getLocation().getName() : null);
        vo.setDeviceModelName(t.getDeviceModel() != null ? t.getDeviceModel().getName() : null);
        vo.setActive(t.isActive());
        vo.setDebug(t.isDebug());
        vo.setId(t.getDeviceId());
        vo.setTags(t.getTags()!=null ? t.getTags() : Collections.emptySet());
        return vo;
    }

    @Override
    public Device patchDB(Device t) {
        t.setActive(this.isActive());
        t.setName(this.getName());
        t.setDescription(this.getDescription());
        t.setId(this.getId());
        t.setGuid(this.getGuid());
        t.setTags(this.getTags());
        t.setDebug(this.isDebug());
        return t;
    }
}
