package com.konkerlabs.platform.registry.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@ApiModel(
        value = "Route Input",
        discriminator = "com.konkerlabs.platform.registry.web.model")
public class EventRouteInputVO {

    @ApiModelProperty(value = "the route name", required = true, position = 1, example = "route01")
    protected String name;
    @ApiModelProperty(value = "the route description", position = 2, example = "route from device01 to device02")
    protected String description;
    @ApiModelProperty(value = "the route incoming", required = true, position = 3)
    protected RouteActorVO incoming;
    @ApiModelProperty(value = "the route outgoing", required = true, position = 4)
    protected RouteActorVO outgoing;
    @ApiModelProperty(value = "the route filtering expression", position = 5)
    protected String filteringExpression;
    @ApiModelProperty(value = "the route transformation guid", position = 6, example = "818599ad-3502-4e70-a852-fc7af8e0a9f3")
    protected String transformationGuid;
    @ApiModelProperty(example = "true", position = 7)
    protected boolean active;

}
