package com.konkerlabs.platform.registry.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@ApiModel(
        value = "Application Destination",
        discriminator = "com.konkerlabs.platform.registry.api.model")
public class ApplicationDestinationVO {

    @ApiModelProperty(value = "the destination application name", example = "otherApp", required = true, position = 1)
    protected String destinationApplicationName;

}
