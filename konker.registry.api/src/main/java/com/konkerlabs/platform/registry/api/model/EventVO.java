package com.konkerlabs.platform.registry.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.konkerlabs.platform.registry.api.model.core.SerializableVO;
import com.konkerlabs.platform.registry.business.model.Event;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@ApiModel(
        value = "Event",
        discriminator = "com.konkerlabs.platform.registry.api.model")
@JsonInclude(Include.NON_EMPTY)
public class EventVO implements SerializableVO<Event, EventVO> {

    @ApiModelProperty(value = "timestamp (ISO 8601 format)", position = 0)
    private String timestamp;

    @ApiModelProperty(value = "incoming", position = 0)
    private EventActorVO incoming;

    @ApiModelProperty(value = "outgoing", position = 0)
    private EventActorVO outgoing;

    @ApiModelProperty(value = "payload", position = 1)
    private String payload;

    @Override
    public EventVO apply(Event t) {
        EventVO vo = new EventVO();

        vo.setTimestamp(t.getTimestamp().toString());
        vo.setIncoming(new EventActorVO().apply(t.getIncoming()));
        vo.setOutgoing(new EventActorVO().apply(t.getOutgoing()));
        vo.setPayload(t.getPayload());

        return vo;
    }

    @Override
    public Event patchDB(Event t) {
        // api doesn't update events
        return t;
    }

}
