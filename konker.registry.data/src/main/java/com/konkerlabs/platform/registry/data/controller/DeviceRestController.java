package com.konkerlabs.platform.registry.data.controller;

import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Scope("request")
@RequestMapping(
        value = "/devices"
)
public class DeviceRestController {

    @GetMapping(path = "/")
    public String list() {
        return "oi";
    }

}
