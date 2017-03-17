package com.konkerlabs.platform.registry.api.model;

import com.konkerlabs.platform.registry.api.model.core.SerializableVO;
import com.konkerlabs.platform.registry.business.model.User;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@ApiModel(
        value = "User",
        discriminator = "com.konkerlabs.platform.registry.api.model")
public class UserVO extends UserInputVO implements SerializableVO<User, UserVO> {

    @ApiModelProperty(value = "the user's email", example = "login@domain.com", required = true, position = 0)
    private String email;
    
	@Override
	public UserVO apply(User t) {
		UserVO vo = new UserVO();
		vo.setEmail(t.getEmail());
		vo.setPhone(t.getPhone());
		vo.setName(t.getName());
		vo.setNotificationViaEmail(t.isNotificationViaEmail());
		return vo;
	}

	@Override
	public User patchDB(User t) {
		t.setEmail(this.getEmail());
		t.setPassword(this.getPassword());
		t.setName(this.getName());
		t.setPhone(this.getPhone());
		t.setNotificationViaEmail(this.isNotificationViaEmail());
		return t;
	}
}
