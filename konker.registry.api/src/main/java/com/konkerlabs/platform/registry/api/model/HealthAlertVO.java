package com.konkerlabs.platform.registry.api.model;

import com.konkerlabs.platform.registry.api.model.core.SerializableVO;
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
        value = "HealthAlert",
        discriminator = "com.konkerlabs.platform.registry.api.model")
public class HealthAlertVO extends HealthAlertInputVO implements SerializableVO<HealthAlert, HealthAlertVO> {

    @ApiModelProperty(value = "occurrence date time (ISO 8601 format)", position = 0, example = "2017-04-05T13:55:20.150Z")
    private String occurrenceDate;

    @ApiModelProperty(value = "type of alert", position = 101, example = "2017-04-05T13:55:20.150Z")
    private String type;

    @ApiModelProperty(value = "name of trigger alert", position = 102, example = "silence")
    private String triggerName;

    @Override
    public HealthAlertVO apply(HealthAlert t) {
        HealthAlertVO vo = new HealthAlertVO();
        vo.setAlertId(t.getAlertId());
        vo.setDeviceId(t.getDevice().getDeviceId());
        vo.setDescription(t.getDescription());
        vo.setOccurrenceDate(t.getRegistrationDate().toString());
        vo.setSeverity(t.getSeverity().name());
        vo.setType(t.getType().name());
        vo.setTriggerName(t.getAlertTrigger().getName());
        return vo;
    }

	@Override
	public HealthAlert patchDB(HealthAlert t) {
		return null;
	}

}
