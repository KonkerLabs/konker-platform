package com.konkerlabs.platform.registry.api.model;

import com.konkerlabs.platform.registry.api.web.controller.ProbeController;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProbeVO {

    private ProbeController.APP_STATUS webcontext;
    private ProbeController.APP_STATUS services;
    private Long timestamp;
}
