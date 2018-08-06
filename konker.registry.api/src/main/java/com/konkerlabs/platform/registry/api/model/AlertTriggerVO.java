package com.konkerlabs.platform.registry.api.model;

import com.konkerlabs.platform.registry.business.model.AlertTrigger;

import io.swagger.annotations.ApiModel;
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

    public AlertTriggerVO(AlertTrigger alertTrigger) {

        this.name = alertTrigger.getName();
        this.description = alertTrigger.getDescription();
        this.type = alertTrigger.getType().name();
        if (alertTrigger.getType() == AlertTrigger.AlertTriggerType.SILENCE) {
            this.deviceModelName = alertTrigger.getDeviceModel().getName();
            this.locationName = alertTrigger.getLocation().getName();
            this.minutes = alertTrigger.getMinutes();
        }

    }

}
