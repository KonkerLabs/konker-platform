package com.konkerlabs.platform.registry.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.konkerlabs.platform.registry.api.model.core.SerializableVO;
import com.konkerlabs.platform.registry.business.model.Event;
import com.mongodb.util.JSON;
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

    @ApiModelProperty(value = "timestamp (ISO 8601 format)", position = 0, example = "2017-04-05T13:55:20.150Z")
    private String timestamp;

    @ApiModelProperty(value = "incoming", position = 1)
    private EventActorVO incoming;

    @ApiModelProperty(value = "outgoing", position = 2)
    private EventActorVO outgoing;
    
    @ApiModelProperty(value = "geolocation", position = 3)
    private EventGeolocationVO geolocation;
    
    @ApiModelProperty(value = "payload", position = 4, example = "{\"temperature\": 18, \"unit\": \"celsius\"}")
    private Object payload;

    @Override
    public EventVO apply(Event t) {
        EventVO vo = new EventVO();

        vo.setTimestamp(t.getTimestamp().toString());
        vo.setIncoming(new EventActorVO().apply(t.getIncoming()));
        vo.setOutgoing(new EventActorVO().apply(t.getOutgoing()));
        vo.setGeolocation(new EventGeolocationVO().apply(t.getGeolocation()));
        vo.setPayload(JSON.parse(t.getPayload()));

        return vo;
    }

    @Override
    public Event patchDB(Event t) {
        // api doesn't update events
        return t;
    }

}
