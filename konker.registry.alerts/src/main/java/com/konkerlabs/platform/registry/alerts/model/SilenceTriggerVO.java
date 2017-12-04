package com.konkerlabs.platform.registry.alerts.model;

import com.konkerlabs.platform.registry.api.model.core.SerializableVO;
import com.konkerlabs.platform.registry.business.model.SilenceTrigger;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@ApiModel(
		value = "Silence Trigger",
		discriminator = "com.konkerlabs.platform.registry.api.model")
public class SilenceTriggerVO
                    extends SilenceTriggerInputVO
                    implements SerializableVO<SilenceTrigger, SilenceTriggerVO> {

    @ApiModelProperty(value = "the trigger guid", example = "39a35764-5134-4003-8f1e-400959631618", position = 0)
    private String guid;
    @ApiModelProperty(value = "the device model name of device", example = "PresenceSensor", position = 2)
    protected String deviceModelName;
    @ApiModelProperty(value = "the location name of device", example = "br_sp", position = 3)
    protected String locationName;
    @ApiModelProperty(value = "alert type", example = "silence", position = 4)
    protected String type;

    public SilenceTriggerVO(SilenceTrigger silenceTrigger) {
        this.guid = silenceTrigger.getGuid();
        this.description = silenceTrigger.getDescription();
        this.deviceModelName  = silenceTrigger.getDeviceModel().getName();
        this.locationName = silenceTrigger.getLocation().getName();
        this.minutes = silenceTrigger.getMinutes();
        this.type = silenceTrigger.getType().name().toLowerCase();
    }

    public SilenceTriggerVO apply(SilenceTrigger t) {
        return new SilenceTriggerVO(t);
    }

    @Override
    public SilenceTrigger patchDB(SilenceTrigger model) {
        model.setGuid(this.getGuid());
        model.setDescription(this.getDescription());
        model.setMinutes(this.getMinutes());

        return model;
    }

}
