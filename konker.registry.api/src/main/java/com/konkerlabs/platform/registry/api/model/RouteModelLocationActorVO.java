package com.konkerlabs.platform.registry.api.model;

import org.springframework.data.annotation.Transient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonInclude(Include.NON_EMPTY)
public class RouteModelLocationActorVO
        extends RouteActorVO {

    public RouteModelLocationActorVO() {
        super();
        this.setType(RouteActorVO.TYPE_MODEL_LOCATION);
    }

    @ApiModelProperty(position = 1, value = "device model name", example = "default")
    private String deviceModelName;

    @ApiModelProperty(position = 2, value = "location name", example = "default")
    private String locationName;

    @ApiModelProperty(position = 2, value = "actor channel", example = "out")
    private String channel;

    @Transient
    private String deviceModelGuid;

    @Transient
    private String locationGuid;

}
