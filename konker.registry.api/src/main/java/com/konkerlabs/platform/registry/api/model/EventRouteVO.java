package com.konkerlabs.platform.registry.api.model;

import java.util.Optional;

import com.konkerlabs.platform.registry.business.model.EventRoute;
import com.konkerlabs.platform.registry.business.model.Transformation;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EventRouteVO {

    private String guid;
    private String name;
    private String description;
    private String filteringExpression;
    private String transformationGuid;
    private boolean active;

    public EventRouteVO(EventRoute route) {
        this.guid   = route.getGuid();
        this.name   = route.getName();
        this.description = route.getDescription();
        this.filteringExpression = route.getFilteringExpression();
        this.transformationGuid =  Optional.ofNullable(route.getTransformation()).map(Transformation::getGuid).orElse(null);
        this.active = route.isActive();
    }

}
