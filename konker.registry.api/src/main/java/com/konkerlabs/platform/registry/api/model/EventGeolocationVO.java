package com.konkerlabs.platform.registry.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.konkerlabs.platform.registry.api.model.core.SerializableVO;
import com.konkerlabs.platform.registry.business.model.Event.EventGeolocation;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@ApiModel(value = "EventGeolocation", discriminator = "com.konkerlabs.platform.registry.api.model")
@JsonInclude(Include.NON_EMPTY)
public class EventGeolocationVO implements SerializableVO<EventGeolocation, EventGeolocationVO> {

    @ApiModelProperty(value = "latitude", position = 0, example = "-23.5746571")
    private Double lat;

    @ApiModelProperty(value = "longitude", position = 1, example = "-46.6910183")
    private Double lon;
    
    @ApiModelProperty(value = "hdop", position = 2, example = "2")
    private Long hdop;
    
    @ApiModelProperty(value = "elevation", position = 3, example = "3.66")
    private Double elev;

    @Override
    public EventGeolocationVO apply(EventGeolocation t) {

        if (t == null) {
            return null;
        } else {
            EventGeolocationVO vo = new EventGeolocationVO();
            vo.setLat(t.getLat());
            vo.setLon(t.getLon());
            vo.setHdop(t.getHdop());
            vo.setElev(t.getElev());
            
            return vo;
        }
    }

    @Override
    public EventGeolocation patchDB(EventGeolocation t) {
    	t.setLat(this.getLat());
    	t.setLon(this.getLon());
    	t.setHdop(this.getHdop());
    	t.setElev(this.getElev());
    	return t;
    }

}
