package com.konkerlabs.platform.registry.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@ApiModel(
        value = "Route Input",
        discriminator = "com.konkerlabs.platform.registry.api.model")
public class EventRouteInputVO {

    @ApiModelProperty(value = "the route name", position = 1)
    protected String name;
    @ApiModelProperty(value = "the route description", position = 2)
    protected String description;
    @ApiModelProperty(value = "the route incoming", position = 3)
    protected RouteActorVO incoming;
    @ApiModelProperty(value = "the route outgoing", position = 4)
    protected RouteActorVO outgoing;
    @ApiModelProperty(value = "the route filtering expression", position = 5)
    protected String filteringExpression;
    @ApiModelProperty(value = "the route transformation guid", position = 6)
    protected String transformationGuid;
    @ApiModelProperty(position = 7)
    protected boolean active;

}
