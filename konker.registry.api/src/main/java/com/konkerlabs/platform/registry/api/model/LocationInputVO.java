package com.konkerlabs.platform.registry.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@ApiModel(
		value = "Location input",
		discriminator = "com.konkerlabs.platform.registry.api.model")
public class LocationInputVO {

	@ApiModelProperty(
			value = "Name to identify the location",
			example = "br",
			required = true,
			position = 1)
	protected String name;

	@ApiModelProperty(
			value = "Description of this location",
			example = "Brazil",
			position = 2)
	protected String description;

    @ApiModelProperty(
            value = "Is the default location of application?",
            example = "false",
            position = 3)
    protected boolean defaultLocation;

}
