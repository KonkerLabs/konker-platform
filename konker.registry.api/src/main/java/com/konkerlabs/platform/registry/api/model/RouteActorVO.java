package com.konkerlabs.platform.registry.api.model;

import com.konkerlabs.platform.registry.business.model.EventRoute;
import com.konkerlabs.platform.registry.business.model.EventRoute.RouteActor;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RouteActorVO {

    private String type;
    private String guid;
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

}
