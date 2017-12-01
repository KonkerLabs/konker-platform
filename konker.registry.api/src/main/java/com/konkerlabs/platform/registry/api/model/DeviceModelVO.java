package com.konkerlabs.platform.registry.api.model;

import com.konkerlabs.platform.registry.api.model.core.SerializableVO;
import com.konkerlabs.platform.registry.business.model.DeviceModel;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@ApiModel(
		value = "Device Model",
		discriminator = "com.konkerlabs.platform.registry.api.model")
public class DeviceModelVO extends DeviceModelInputVO
		implements SerializableVO<DeviceModel, DeviceModelVO>{

	@ApiModelProperty(value = "Kind of unique key generate when the device model is created", example = "818599ad-3502-4e70-a852-fc7af8e0a9f3")
	private String guid;

	public DeviceModelVO(DeviceModel deviceModel) {
		this.guid = deviceModel.getGuid();
		this.name = deviceModel.getName();
		this.description = deviceModel.getDescription();
		this.contentType = deviceModel.getContentType().getValue();
		this.defaultModel = deviceModel.isDefaultModel();
	}

	@Override
	public DeviceModelVO apply(DeviceModel model) {
		DeviceModelVO vo = new DeviceModelVO();
		vo.setGuid(model.getGuid());
		vo.setName(model.getName());
		vo.setDescription(model.getDescription());
		if (model.getContentType() != null) {
			vo.setContentType(model.getContentType().getValue());
		} else {
			vo.setContentType(DeviceModel.ContentType.APPLICATION_JSON.getValue());
		}
		vo.setDefaultModel(model.isDefaultModel());
		return vo;
	}

	@Override
	public DeviceModel patchDB(DeviceModel model) {
		model.setGuid(this.getGuid());
		model.setName(this.getName());
		model.setDescription(this.getDescription());
		model.setContentType(DeviceModel.ContentType.getByValue(this.getContentType()));
		model.setDefaultModel(this.isDefaultModel());
		return model;
	}

}
