package com.konkerlabs.platform.registry.api.model;

import com.konkerlabs.platform.registry.api.model.core.SerializableVO;
import com.konkerlabs.platform.registry.business.model.EventRoute;
import com.konkerlabs.platform.registry.business.model.EventRoute.RouteActor;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RouteActorVO
        implements SerializableVO<RouteActor, RouteActorVO> {

    @ApiModelProperty(value = "type", allowableValues = "DEVICE")
    private String type;
    @ApiModelProperty(value = "actor guid")
    private String guid;
    @ApiModelProperty(value = "actor channel")
    private String channel;

    @Override
    public RouteActorVO apply(RouteActor t) {
        RouteActorVO vo = new RouteActorVO();
        if (t.isDevice()) {
            String uriPath = t.getUri().getPath();

            vo.type = RouteActorType.DEVICE.name();
            vo.guid = uriPath.startsWith("/") ? uriPath.substring(1) : uriPath;
            vo.channel = t.getData().get(EventRoute.DEVICE_MQTT_CHANNEL);
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
