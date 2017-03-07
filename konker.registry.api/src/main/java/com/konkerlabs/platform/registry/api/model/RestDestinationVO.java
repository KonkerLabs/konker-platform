package com.konkerlabs.platform.registry.api.model;

import com.konkerlabs.platform.registry.business.model.RestDestination;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@ApiModel(
		value = "Rest Destination",
		discriminator = "com.konkerlabs.platform.registry.api.model")
public class RestDestinationVO extends RestDestinationInputVO {

	@ApiModelProperty(value = "Kind of unique key generate when the rest destination is created", position = 0)
	private String guid;
	
	public RestDestinationVO(RestDestination restDestination) {
		this.guid = restDestination.getGuid();
		this.name = restDestination.getName();
		this.method = restDestination.getMethod();
		this.headers = restDestination.getHeaders();
		this.serviceURI = restDestination.getServiceURI();
		this.serviceUserName = restDestination.getServiceUsername();
		this.servicePassword = restDestination.getServicePassword();
		this.active = restDestination.isActive();
	}
}
