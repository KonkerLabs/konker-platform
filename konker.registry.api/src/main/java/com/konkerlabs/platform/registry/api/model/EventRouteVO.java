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

    @ApiModelProperty(value = "the route guid", position = 0, example = "818599ad-3502-4e70-a852-fc7af8e0a9f3")
    private String guid;

    @Override
    public EventRouteVO apply(EventRoute t) {
        EventRouteVO vo = new EventRouteVO();
        vo.guid   = t.getGuid();
        vo.name   = t.getName();
        vo.incoming = new RouteActorVO().apply(t.getIncoming());
        vo.outgoing = new RouteActorVO().apply(t.getOutgoing());
        vo.description = t.getDescription();
        vo.filteringExpression = t.getFilteringExpression();
        vo.transformationGuid =  Optional.ofNullable(t.getTransformation()).map(Transformation::getGuid).orElse(null);
        vo.active = t.isActive();
        return vo;
    }

    @Override
    public EventRoute patchDB(EventRoute t) {
        t.setGuid(this.getGuid());
        t.setName(this.getName());
        t.setActive(this.isActive());
        t.setIncoming(new RouteActorVO().apply(t.getIncoming()).patchDB(t.getIncoming()));
        t.setOutgoing(new RouteActorVO().apply(t.getOutgoing()).patchDB(t.getOutgoing()));
        t.setDescription(this.getDescription());
        t.setTransformation(new RestTransformationVO()
                .apply(t.getTransformation()).patchDB(t.getTransformation()));

        return t;
    }
}
