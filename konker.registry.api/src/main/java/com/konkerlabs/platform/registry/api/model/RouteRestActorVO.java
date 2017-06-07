package com.konkerlabs.platform.registry.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonInclude(Include.NON_EMPTY)
public class RouteRestActorVO
        extends RouteActorVO {

    public RouteRestActorVO() {
        super();
        this.setType(RouteActorVO.TYPE_REST);
    }

    @ApiModelProperty(position = 1, value = "actor (device, rest destination, model/location) guid", example = "818599ad-3502-4e70-a852-fc7af8e0a9f3")
    private String guid;

}
