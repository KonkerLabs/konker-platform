package com.konkerlabs.platform.registry.api.web.controller;

import lombok.Builder;
import lombok.Data;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.http.MediaType;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping(value = "/probe")
@EnableAutoConfiguration
public class ProbeController {

    @ResponseBody
    @GetMapping
    public Probe get() {
        return Probe.builder()
                .webcontext(APP_STATUS.OK)
                .services(APP_STATUS.OK)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    @Builder
    @Data
    public static class Probe {
        private APP_STATUS webcontext;
        private APP_STATUS services;
        private Long timestamp;
    }

    public static enum APP_STATUS {
        OK,
        ERROR
    }
}

