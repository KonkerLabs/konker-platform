package com.konkerlabs.platform.registry.api.model;

import com.konkerlabs.platform.registry.api.model.core.SerializableVO;
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
public class RestDestinationVO extends RestDestinationInputVO
		implements SerializableVO<RestDestination, RestDestinationVO>{

	@ApiModelProperty(value = "Kind of unique key generate when the rest destination is created", position = 0, example = "818599ad-3502-4e70-a852-fc7af8e0a9f3")
	private String guid;

	public RestDestinationVO(RestDestination restDestination) {
		this.guid = restDestination.getGuid();
		this.name = restDestination.getName();
		this.method = restDestination.getMethod();
		this.headers = restDestination.getHeaders();
		this.serviceURI = restDestination.getServiceURI();
		this.serviceUsername = restDestination.getServiceUsername();
		this.servicePassword = restDestination.getServicePassword();
		this.active = restDestination.isActive();
	}

	@Override
	public RestDestinationVO apply(RestDestination model) {
		RestDestinationVO vo = new RestDestinationVO();
		vo.setGuid(model.getGuid());
		vo.setName(model.getName());
		vo.setMethod(model.getMethod());
		vo.setHeaders(model.getHeaders());
		vo.setServiceURI(model.getServiceURI());
		vo.setServiceUsername(model.getServiceUsername());
		vo.setServicePassword(model.getServicePassword());
		vo.setActive(model.isActive());
		return vo;
	}

	@Override
	public RestDestination patchDB(RestDestination model) {
		model.setGuid(this.getGuid());
		model.setName(this.getName());
		model.setMethod(this.getMethod());
		model.setHeaders(this.getHeaders());
		model.setServiceURI(this.getServiceURI());
		model.setServiceUsername(this.getServiceUsername());
		model.setServicePassword(this.getServicePassword());
		model.setActive(this.isActive());
		return model;
	}
}
