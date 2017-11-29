package com.konkerlabs.platform.registry.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@ApiModel(
		value = "Device Model input",
		discriminator = "com.konkerlabs.platform.registry.api.model")
public class DeviceModelInputVO {
	
	@ApiModelProperty(
			value = "Descriptive name to identify the device model",
			example = "PresenceSensor", 
			required = true, 
			position = 1)
	protected String name;
	
	@ApiModelProperty(
			value = "A short description about device model", 
			example = "This is a device model to group all the presence sensor", 
			position = 2)
	protected String description;

	@ApiModelProperty(
			value = "The nature and format of the data send by the device",
			example = "application/json",
			position = 3)
	protected String contentType;

	@ApiModelProperty(
			value = "Property to indicate whether the device model is default or not", 
			example = "true", 
			position = 4)
	protected boolean defaultModel;

}
