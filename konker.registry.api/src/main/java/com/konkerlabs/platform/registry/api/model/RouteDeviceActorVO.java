package com.konkerlabs.platform.registry.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_EMPTY)
public class RouteDeviceActorVO
        extends RouteActorVO {

    @ApiModelProperty(position = 1, value = "actor (device, rest destination, model/location) guid", example = "818599ad-3502-4e70-a852-fc7af8e0a9f3")
    private String guid;

    @ApiModelProperty(position = 2, value = "actor channel", example = "out")
    private String channel;

}
