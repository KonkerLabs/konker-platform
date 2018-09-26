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
@ApiModel(value = "Device Register Gateway", discriminator = "com.konkerlabs.platform.registry.api.model")
public class DeviceRegisterGatewayVO extends DeviceSecurityCredentialsPwdVO {

    @ApiModelProperty(position = 10, value = "the device id", example = "awsff-0293")
    private String id;

    @ApiModelProperty(position = 11, value = "the device name", example = "Temperature sensor")
    private String name;


    public DeviceRegisterGatewayVO(DeviceSecurityCredentials credentials, DeviceRegisterService.DeviceDataURLs deviceDataURLs) {
        super(credentials, deviceDataURLs);
        this.id = credentials.getDevice().getDeviceId();
        this.name = credentials.getDevice().getName();
    }

}
