package com.konkerlabs.platform.registry.api.model;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

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
        this.parentName = getParentName(location.getParent());
        this.name = location.getName();
        this.description = location.getDescription();

        if (location.getChildrens() != null && !location.getChildrens().isEmpty()) {
            this.subLocations = new ArrayList<>();
            for (Location subLocation : location.getChildrens()) {
                this.subLocations.add(new LocationVO(subLocation));
            }
        }
    }

    @Override
    public LocationVO apply(Location model) {
        LocationVO vo = new LocationVO();
        vo.setGuid(model.getGuid());
        vo.setParentName(getParentName(model.getParent()));
        vo.setName(model.getName());
        vo.setDescription(model.getDescription());
        return vo;
    }

    @Override
    public Location patchDB(Location model) {
        model.setGuid(this.getGuid());
        if (StringUtils.isNotBlank(this.getParentName())) {
            model.setParent(Location.builder().name(this.getParentName()).build());
        }
        model.setName(this.getName());
        model.setDescription(this.getDescription());
        return model;
    }

    private String getParentName(Location parent) {
        if (parent == null) {
            return null;
        } else {
            return parent.getName();
        }
    }

}
