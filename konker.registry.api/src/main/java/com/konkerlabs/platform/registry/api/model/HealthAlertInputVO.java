package com.konkerlabs.platform.registry.api.model;

import com.konkerlabs.platform.registry.business.model.HealthAlert;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@ApiModel(
        value = "HealthAlertInput",
        discriminator = "com.konkerlabs.platform.registry.api.model")
public class HealthAlertInputVO  {

    @ApiModelProperty(value = "the alert id", position = 1, example = "SN4940c22a9a", required = false)
    private String alertId;

    @ApiModelProperty(value = "the device id", position = 2, example = "SN4940c22a9a", required = true)
    private String deviceId;

    @ApiModelProperty(value = "the severity status", position = 3, example = "FAIL", allowableValues = "OK,WARN,FAIL", required = true)
    private String severity;

    @ApiModelProperty(value = "the severity status", position = 4, example = "Device does not sent message for 5 minutes", required = false)
    private String description;

    public HealthAlertInputVO apply(HealthAlert t) {
        HealthAlertInputVO vo = new HealthAlertInputVO();

        vo.setDeviceId(t.getDevice().getDeviceId());
        vo.setAlertId(t.getAlertId());
        vo.setSeverity(t.getSeverity().name());
        vo.setDescription(t.getDescription());

        return vo;
    }

}
