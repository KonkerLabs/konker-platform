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

    public RouteActorVO(RouteActor routeActor) {
        if (routeActor.isDevice()) {
            String uriPath = routeActor.getUri().getPath();

            this.type = RouteActorType.DEVICE.name();
            this.guid = uriPath.startsWith("/") ? uriPath.substring(1) : uriPath;
            this.channel = routeActor.getData().get(EventRoute.DEVICE_MQTT_CHANNEL);
        } else {
            String uriPath = routeActor.getUri().getPath();

            this.type = routeActor.getUri().getScheme();
            this.guid = uriPath.startsWith("/") ? uriPath.substring(1) : uriPath;
        }
    }

    @Override
    public RouteActorVO apply(RouteActor t) {
        return null;
    }

    @Override
    public RouteActor applyDB(RouteActor t) {
        return t;
    }
}
