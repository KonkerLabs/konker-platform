package com.konkerlabs.platform.registry.business.model;

import com.fasterxml.jackson.annotation.JsonView;
import com.konkerlabs.platform.registry.web.serializers.EventJson;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Data
@Builder
public class Event {

    /*@JsonView(EventJson.class)*/
    private Instant timestamp;
    private EventActor incoming;
    private EventActor outgoing;
    /*@JsonView(EventJson.class)*/
    private String payload;

    @Data
    @Builder
    public static class EventActor {
        private String tenantDomain;
        private String deviceGuid;
        private String deviceId;
        private String channel;
    }
    
    public ZonedDateTime getZonedTimestamp(String zoneId) {
        return timestamp.atZone(ZoneId.of(zoneId));
    }
}