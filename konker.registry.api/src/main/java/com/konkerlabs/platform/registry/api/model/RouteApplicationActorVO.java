package com.konkerlabs.platform.registry.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@JsonInclude(Include.NON_EMPTY)
public class RouteApplicationActorVO
        extends RouteActorVO {

    public RouteApplicationActorVO() {
        super();
        this.setType(RouteActorVO.TYPE_APPLICATION);
    }

    @ApiModelProperty(position = 1, value = "actor application name", example = "smartff")
    private String name;

}
