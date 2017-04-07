package com.konkerlabs.platform.registry.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@ApiModel(
		value = "Application input",
		discriminator = "com.konkerlabs.platform.registry.api.model")
public class ApplicationInputVO {
	
	@ApiModelProperty(
			value = "Friendly name to identify the application", 
			example = "konker devices application", 
			required = true, 
			position = 1)
	protected String friendlyName;
	
	@ApiModelProperty(
			value = "Describe what this application does", 
			example = "This application join devices and routes of konker application", 
			position = 2)
	protected String description;

}
