package com.konkerlabs.platform.registry.api.web.controller;

import com.konkerlabs.platform.registry.api.model.ProbeRest;
import io.swagger.annotations.SwaggerDefinition;
import lombok.Builder;
import lombok.Data;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/probe")
public class ProbeController {

    @GetMapping
    public @ResponseBody ProbeRest get() {
        return ProbeRest.builder()
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

