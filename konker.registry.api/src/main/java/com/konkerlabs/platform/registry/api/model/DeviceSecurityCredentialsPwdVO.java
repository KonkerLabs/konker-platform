package com.konkerlabs.platform.registry.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService.DeviceSecurityCredentials;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_EMPTY)
@ApiModel(value = "Device Security Credentials With Password", discriminator = "com.konkerlabs.platform.registry.web.model")
public class DeviceSecurityCredentialsPwdVO extends DeviceSecurityCredentialsVO {

    @ApiModelProperty(position = 1, value = "the device password", example = "sW2YEG1i3e")
    private String password;

    public DeviceSecurityCredentialsPwdVO(DeviceSecurityCredentials credentials, DeviceRegisterService.DeviceDataURLs deviceDataURLs) {

        super.username = credentials.getDevice().getApiKey();
        this.password = credentials.getPassword();
        setDeviceDataURLs(deviceDataURLs);

    }

}
