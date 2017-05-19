package com.konkerlabs.platform.registry.api.model;

import java.util.List;

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
            value = "The parent location name",
            example = "root",
            required = false,
            position = 1)
    protected String parentName;

	@ApiModelProperty(
			value = "Name to identify the location",
			example = "br",
			required = true,
			position = 2)
	protected String name;

	@ApiModelProperty(
			value = "Description of this location",
			example = "Brazil",
			position = 3)
	protected String description;

    @ApiModelProperty(
            value = "Is the default location of application?",
            example = "false",
            required = true,
            position = 4)
    protected boolean defaultLocation;

    @ApiModelProperty(
            value = "List sub-locations",
            required = true,
            position = 5)
    protected List<LocationVO> sublocations;

}
