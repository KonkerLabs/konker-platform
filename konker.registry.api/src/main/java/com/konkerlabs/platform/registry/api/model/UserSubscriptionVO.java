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
        value = "Use Subscription",
        discriminator = "com.konkerlabs.platform.registry.api.model")
public class UserSubscriptionVO  {

    @ApiModelProperty(value = "name", example = "Name Surname", required = true, position = 0)
    private String name;
    @ApiModelProperty(value = "company", example = "Company Inc.", required = false, position = 1)
    private String company;
    @ApiModelProperty(value = "email", example = "login@domain.com", required = true, position = 2)
    private String email;
    @ApiModelProperty(value = "password", example = "ye5RezC47VSQ", required = true, position = 3)
    private String password;
    @ApiModelProperty(value = "password type", example = "PASSWORD", allowableValues = "PASSWORD,BCRYPT_HASH,PBKDF2_HASH", required = true, position = 4)
    private String passwordType;
    @ApiModelProperty(value = "job title", example = "Information Technology (IT)", required = false, position = 5)
    private String jobTitle;
    @ApiModelProperty(value = "phone number", example = "+55-11-3303-3206", required = false, position = 6)
    private String phoneNumber;

}
