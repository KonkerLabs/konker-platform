package com.konkerlabs.platform.registry.web.forms;

import lombok.Data;

@Data
public class DeviceRegistrationForm {

    private String deviceId;
    private String name;
    private String description;

}
