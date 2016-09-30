package com.konkerlabs.platform.registry.web.forms;

import java.time.Instant;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class DeviceVisualizationForm {

    private String deviceGuid;
    private String channel;
    private String metric;
    private Instant dateStart;
    private Instant dateEnd;
    private boolean online;



}
