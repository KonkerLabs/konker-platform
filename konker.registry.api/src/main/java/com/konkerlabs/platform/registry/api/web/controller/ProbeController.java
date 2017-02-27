package com.konkerlabs.platform.registry.api.web.controller;

import com.konkerlabs.platform.registry.api.model.ProbeVO;
import io.swagger.annotations.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
        value = "/probe",
        consumes = {MediaType.APPLICATION_JSON_VALUE},
        produces = {MediaType.APPLICATION_JSON_VALUE}
)
public class ProbeController {

    @GetMapping
    public @ResponseBody ProbeVO get() {
        return ProbeVO.builder()
                .webcontext(APP_STATUS.OK)
                .services(APP_STATUS.OK)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static enum APP_STATUS {
        OK,
        ERROR
    }
}

