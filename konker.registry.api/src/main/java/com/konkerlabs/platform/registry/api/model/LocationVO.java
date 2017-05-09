package com.konkerlabs.platform.registry.api.model;

import com.konkerlabs.platform.registry.api.model.core.SerializableVO;
import com.konkerlabs.platform.registry.business.model.Location;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@ApiModel(value = "Location", discriminator = "com.konkerlabs.platform.registry.api.model")
public class LocationVO extends LocationInputVO implements SerializableVO<Location, LocationVO> {

    @ApiModelProperty(value = "Unique identifier of the location", position = 0, example = "818599ad-3502-4e70-a852-fc7af8e0a9f3")
    private String guid;

    public LocationVO(Location location) {
        this.guid = location.getGuid();
        this.name = location.getName();
        this.description = location.getDescription();
    }

    @Override
    public LocationVO apply(Location model) {
        LocationVO vo = new LocationVO();
        vo.setGuid(model.getGuid());
        vo.setName(model.getName());
        vo.setDescription(model.getDescription());
        return vo;
    }

    @Override
    public Location patchDB(Location model) {
        model.setGuid(this.getGuid());
        model.setName(this.getName());
        model.setDescription(this.getDescription());
        return model;
    }

}
