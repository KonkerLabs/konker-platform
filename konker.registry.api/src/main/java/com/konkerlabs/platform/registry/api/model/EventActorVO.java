package com.konkerlabs.platform.registry.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.konkerlabs.platform.registry.api.model.core.SerializableVO;
import com.konkerlabs.platform.registry.business.model.Event.EventActor;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@ApiModel(value = "EventActor", discriminator = "com.konkerlabs.platform.registry.api.model")
@JsonInclude(Include.NON_EMPTY)
public class EventActorVO implements SerializableVO<EventActor, EventActorVO> {

    @ApiModelProperty(value = "device guid", position = 0, example = "818599ad-0000-0000-0000-000000000000")
    private String deviceGuid;

    @ApiModelProperty(value = "channel", position = 1, example = "temperature")
    private String channel;

    @Override
    public EventActorVO apply(EventActor t) {

        if (t == null) {
            return null;

        } else {
            EventActorVO vo = new EventActorVO();

            vo.setChannel(t.getChannel());
            vo.setDeviceGuid(t.getDeviceGuid());

            return vo;
        }
    }

    @Override
    public EventActor patchDB(EventActor t) {
        t.setDeviceGuid(t.getDeviceGuid());
        t.setChannel(t.getChannel());
        return t;
    }

}
