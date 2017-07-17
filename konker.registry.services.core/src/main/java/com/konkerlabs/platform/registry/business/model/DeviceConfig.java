package com.konkerlabs.platform.registry.business.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode(of = {"locationGuid", "deviceModelGuid"})
public class DeviceConfig {

    private String deviceModelGuid;
    private String deviceModel;
    private String locationGuid;
    private String locationName;
    private String json;

}
