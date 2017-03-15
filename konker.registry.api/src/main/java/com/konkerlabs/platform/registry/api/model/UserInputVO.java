package com.konkerlabs.platform.registry.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@ApiModel(
        value = "USer Input",
        discriminator = "com.konkerlabs.platform.registry.api.model")
public class UserInputVO {

	@ApiModelProperty(value = "the user's password", example = "mypass123", required = true, position = 1)
    protected String password;
    
    @ApiModelProperty(value = "the user's phone number", example = "988772233", position = 2)
    protected String phone;
    
    @ApiModelProperty(value = "the user's name", example = "John Smith", position = 3)
    protected String name;
    
    @ApiModelProperty(value = "allow the user to receive notification by email", example = "true", position = 5)
    protected boolean notificationViaEmail;

}
