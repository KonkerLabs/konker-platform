package com.konkerlabs.platform.registry.api.model;

import java.util.Optional;

import com.konkerlabs.platform.registry.api.model.core.SerializableVO;
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
public class EventRouteVO extends EventRouteInputVO
implements SerializableVO<EventRoute, EventRouteVO> {

    @ApiModelProperty(value = "the route guid", position = 0)
    private String guid;

    @Override
    public EventRouteVO apply(EventRoute t) {
        this.guid   = t.getGuid();
        this.name   = t.getName();
        this.incoming = new RouteActorVO().apply(t.getIncoming());
        this.outgoing = new RouteActorVO().apply(t.getOutgoing());
        this.description = t.getDescription();
        this.filteringExpression = t.getFilteringExpression();
        this.transformationGuid =  Optional.ofNullable(t.getTransformation()).map(Transformation::getGuid).orElse(null);
        this.active = t.isActive();
        return this;
    }

    @Override
    public EventRoute applyDB(EventRoute t) {
        t.setGuid(this.getGuid());
        t.setName(this.getName());
        t.setActive(this.isActive());
        t.setIncoming(new RouteActorVO().apply(t.getIncoming()).applyDB(t.getIncoming()));
        t.setOutgoing(new RouteActorVO().apply(t.getOutgoing()).applyDB(t.getOutgoing()));
        t.setDescription(this.getDescription());
        t.setTransformation(new TransformationVO()
                .apply(t.getTransformation()).applyDB(t.getTransformation()));

        return t;
    }
}
