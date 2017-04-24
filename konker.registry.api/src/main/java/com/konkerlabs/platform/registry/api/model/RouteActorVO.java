package com.konkerlabs.platform.registry.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.konkerlabs.platform.registry.api.model.core.SerializableVO;
import com.konkerlabs.platform.registry.business.model.EventRoute;
import com.konkerlabs.platform.registry.business.model.EventRoute.RouteActor;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_EMPTY)
public class RouteActorVO
        implements SerializableVO<RouteActor, RouteActorVO> {

    @ApiModelProperty(position = 0, value = "type", allowableValues = "DEVICE,REST", example = "DEVICE")
    private String type;
    @ApiModelProperty(position = 1, value = "actor (device or rest destination) guid", example = "818599ad-3502-4e70-a852-fc7af8e0a9f3")
    private String guid;
    @ApiModelProperty(position = 2, value = "actor channel", example = "out")
    private String channel;

    @Override
    public RouteActorVO apply(RouteActor t) {
        RouteActorVO vo = new RouteActorVO();
        if (t.isDevice()) {
            String uriPath = t.getUri().getPath();

            vo.type = RouteActorType.DEVICE.name();
            vo.guid = uriPath.startsWith("/") ? uriPath.substring(1) : uriPath;
            vo.channel = t.getData().get(EventRoute.DEVICE_MQTT_CHANNEL);
        } else if (t.isRestDestination()) {
            String uriPath = t.getUri().getPath();

            vo.type = RouteActorType.REST.name();
            vo.guid = uriPath.startsWith("/") ? uriPath.substring(1) : uriPath;
        } else {
            String uriPath = t.getUri().getPath();

            vo.type = t.getUri().getScheme();
            vo.guid = uriPath.startsWith("/") ? uriPath.substring(1) : uriPath;
        }

        return vo;
    }

    @Override
    public RouteActor patchDB(RouteActor t) {
        return t;
    }
}
