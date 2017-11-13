package com.konkerlabs.platform.registry.api.model;

import com.konkerlabs.platform.registry.api.model.core.SerializableVO;
import com.konkerlabs.platform.registry.business.model.Application;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@ApiModel(
		value = "Application",
		discriminator = "com.konkerlabs.platform.registry.api.model")
public class ApplicationVO extends ApplicationInputVO
		implements SerializableVO<Application, ApplicationVO>{

	@ApiModelProperty(
			value = "Unique and global name without whitespace and special characters",
			required = true,
			position = 0,
			example = "konker")
	protected String name;

	public ApplicationVO(Application application) {
		this.name = application.getName();
		this.friendlyName = application.getFriendlyName();
		this.description = application.getDescription();
	}

	private String getAlias(String inputName, String baseName, String alias) {
		String returnAlias;
		if(inputName.equals(baseName)){
			returnAlias=alias ;
		}else {
			returnAlias=inputName;
		}
		return returnAlias;
	}
	
	@Override
	public ApplicationVO apply(Application model) {
		ApplicationVO vo = new ApplicationVO();
		vo.setName(getAlias(model.getName(),model.getContext(),"default"));
		vo.setFriendlyName(model.getFriendlyName());
		vo.setDescription(model.getDescription());
		return vo;
	}

	@Override
	public Application patchDB(Application model) {
		model.setName(this.getName());
		model.setFriendlyName(this.getFriendlyName());
		model.setDescription(this.getDescription());
		return model;
	}
}
