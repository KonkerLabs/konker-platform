package com.konkerlabs.platform.registry.api.model;

import com.konkerlabs.platform.registry.api.web.controller.ProbeController;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class ProbeRest {

    private ProbeController.APP_STATUS webcontext;
    private ProbeController.APP_STATUS services;
    private Long timestamp;
}
