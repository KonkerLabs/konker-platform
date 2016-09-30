package com.konkerlabs.platform.registry.web.forms;

import java.time.Instant;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class DeviceVisualizationForm {

    private String deviceGuid;
    private String channel;
    private String metric;
    
    @DateTimeFormat(iso = ISO.DATE_TIME)
    private LocalDateTime dateStart;
    private Instant dateEnd;
    private boolean online;



}
