package com.konkerlabs.platform.registry.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@ApiModel(
        value = "Gateway input",
        discriminator = "com.konkerlabs.platform.registry.api.model")
public class GatewayInputVO {

    @ApiModelProperty(value = "the gateway name", example = "Gateway001", required = true, position = 2)
    protected String name;
    @ApiModelProperty(value = "the gateway description", example = "room 101 gateway", position = 3)
    protected String description;
    @ApiModelProperty(value = "the location name of gateway", example = "br_sp", position = 4)
    protected String locationName;
    @ApiModelProperty(example = "true", position = 6)
    protected boolean active = true;

}
