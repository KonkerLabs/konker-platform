package com.konkerlabs.platform.registry.business.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class Event {

    private String deviceGuid;
    private Instant timestamp;
    private String channel;
    private String payload;
    private Boolean deleted = false;

}