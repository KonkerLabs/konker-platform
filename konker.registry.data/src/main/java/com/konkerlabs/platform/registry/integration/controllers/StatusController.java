package com.konkerlabs.platform.registry.integration.controllers;

import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Scope("request")
@RequestMapping(value = "/status")
public class StatusController {

    /**
     * Tell Marathon that the application is healthy
     *
     * @return
     */
    @GetMapping(path = "/")
    public String read() {
        return "ok";
    }

}
