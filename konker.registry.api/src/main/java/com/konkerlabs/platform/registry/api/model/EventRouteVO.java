package com.konkerlabs.platform.registry.api.model;

import java.util.Optional;

import com.konkerlabs.platform.registry.business.model.EventRoute;
import com.konkerlabs.platform.registry.business.model.Transformation;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@ApiModel(
        value = "Route",
        discriminator = "com.konkerlabs.platform.registry.api.model")
public class EventRouteVO {

    @ApiModelProperty(value = "the route guid")
    private String guid;
    @ApiModelProperty(value = "the route name")
    private String name;
    @ApiModelProperty(value = "the route description")
    private String description;
    @ApiModelProperty(value = "the route incoming")
    private RouteActorVO incoming;
    @ApiModelProperty(value = "the route outgoing")
    private RouteActorVO outgoing;
    @ApiModelProperty(value = "the route filtering expression")
    private String filteringExpression;
    @ApiModelProperty(value = "the route transformation guid")
    private String transformationGuid;
    private boolean active;

    public EventRouteVO(EventRoute route) {
        this.guid   = route.getGuid();
        this.name   = route.getName();
        this.incoming = new RouteActorVO(route.getIncoming());
        this.outgoing = new RouteActorVO(route.getOutgoing());
        this.description = route.getDescription();
        this.filteringExpression = route.getFilteringExpression();
        this.transformationGuid =  Optional.ofNullable(route.getTransformation()).map(Transformation::getGuid).orElse(null);
        this.active = route.isActive();
    }

}
