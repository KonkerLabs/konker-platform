package com.konkerlabs.platform.registry.api.model;

import java.util.Optional;

import com.konkerlabs.platform.registry.business.model.EventRoute;
import com.konkerlabs.platform.registry.business.model.Transformation;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@ApiModel(
        value = "Route",
        discriminator = "com.konkerlabs.platform.registry.api.model")
public class EventRouteVO extends EventRouteInputVO {

    @ApiModelProperty(value = "the route guid", position = 0)
    private String guid;

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
