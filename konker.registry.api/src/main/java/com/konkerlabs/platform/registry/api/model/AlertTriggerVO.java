package com.konkerlabs.platform.registry.api.model;

import com.konkerlabs.platform.registry.business.model.AlertTrigger;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@ApiModel(
		value = "Alert Trigger",
		discriminator = "com.konkerlabs.platform.registry.api.model")
public class AlertTriggerVO extends AlertTriggerInputVO {

    @ApiModelProperty(value = "the trigger guid", example = "39a35764-5134-4003-8f1e-400959631618", position = 10)
    private String guid;

    public AlertTriggerVO(AlertTrigger alertTrigger) {

        this.guid = alertTrigger.getGuid();
        this.name = alertTrigger.getName();
        this.description = alertTrigger.getDescription();
        this.type = alertTrigger.getType().name().toLowerCase();
        this.deviceModelName  = alertTrigger.getDeviceModel().getName();
        this.locationName = alertTrigger.getLocation().getName();
        this.minutes = alertTrigger.getMinutes();

    }

}
