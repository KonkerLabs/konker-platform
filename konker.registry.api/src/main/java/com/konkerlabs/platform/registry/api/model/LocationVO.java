package com.konkerlabs.platform.registry.api.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
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
@JsonInclude(Include.NON_EMPTY)
@ApiModel(value = "Location", discriminator = "com.konkerlabs.platform.registry.api.model")
public class LocationVO extends LocationInputVO implements SerializableVO<Location, LocationVO> {

    @ApiModelProperty(value = "Unique identifier of the location", position = 0, example = "818599ad-3502-4e70-a852-fc7af8e0a9f3")
    private String guid;

    public LocationVO(Location location) {
        this.guid = location.getGuid();
        this.parentName = getParentName(location.getParent());
        this.name = location.getName();
        this.description = location.getDescription();
        this.defaultLocation = location.isDefaultLocation();
        this.subLocations = getSubLocations(location.getChildrens());
    }

    @Override
    public LocationVO apply(Location model) {
        LocationVO vo = new LocationVO();
        vo.setGuid(model.getGuid());
        vo.setParentName(getParentName(model.getParent()));
        vo.setName(model.getName());
        vo.setDescription(model.getDescription());
        vo.setDefaultLocation(model.isDefaultLocation());
        vo.setSubLocations(getSubLocations(model.getChildrens()));
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
        model.setDefaultLocation(this.isDefaultLocation());
        return model;
    }

    private String getParentName(Location parent) {
        if (parent == null) {
            return null;
        } else {
            return parent.getName();
        }
    }

    private List<LocationVO> getSubLocations(List<Location> childrens) {
        List<LocationVO> subLocations = null;

        if (childrens != null && !childrens.isEmpty()) {
            subLocations = new ArrayList<>();
            for (Location subLocation : childrens) {
                subLocations.add(new LocationVO(subLocation));
            }
        }

        return subLocations;
    }

}