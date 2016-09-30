package com.konkerlabs.platform.registry.business.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Data
@Builder
public class Event {

    private Instant timestamp;
    private EventActor incoming;
    private EventActor outgoing;
    private String payload;
    private Boolean deleted = false;

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
}