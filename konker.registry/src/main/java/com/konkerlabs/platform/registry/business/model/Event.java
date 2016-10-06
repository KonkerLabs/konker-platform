package com.konkerlabs.platform.registry.business.model;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Event {

    private Instant timestamp;
    private EventActor incoming;
    private EventActor outgoing;
    private String payload;

    @Data
    @Builder
    public static class EventActor {
        private String tenantDomain;
        private String deviceGuid;
        private String channel;
    }
    
    public ZonedDateTime getZonedTimestamp(String zoneId) {
        return timestamp.atZone(ZoneId.of(zoneId));
    }

    @Data
    @Builder
    public static class EventDecorator {
    	private String timestamp;
    	private EventActor incoming;
    	private EventActor outgoing;
    	private String payload;
    }
}
