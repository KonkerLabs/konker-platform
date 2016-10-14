package com.konkerlabs.platform.registry.business.model;

import com.fasterxml.jackson.annotation.JsonView;
import com.konkerlabs.platform.registry.integration.serializers.EventJsonView;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Data
@Builder
public class Event {

    private Instant timestamp;

    @JsonView(EventJsonView.class)
    private EventActor incoming;

    @JsonView(EventJsonView.class)
    private EventActor outgoing;

    @JsonView(EventJsonView.class)
    private String payload;



    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventActor {

        private String tenantDomain;
        private String deviceGuid;

        @JsonView(EventJsonView.class)
        private String deviceId;

        @JsonView(EventJsonView.class)
        private String channel;
    }
    
    public ZonedDateTime getZonedTimestamp(String zoneId) {
        return timestamp.atZone(ZoneId.of(zoneId));
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventDecorator {
    	private String timestampFormated;
    	private Long timestamp;
    	private EventActor incoming;
    	private EventActor outgoing;
    	private String payload;
    }
}
