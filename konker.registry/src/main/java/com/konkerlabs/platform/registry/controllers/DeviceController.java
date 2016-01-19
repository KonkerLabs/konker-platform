package com.konkerlabs.platform.registry.controllers;

import com.konkerlabs.platform.registry.model.Device;
import com.konkerlabs.platform.registry.repositories.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping(value = "devices")
public class DeviceController {

    @Autowired
    private DeviceRepository repository;

    @RequestMapping("index")
    public ModelAndView index() {
        return new ModelAndView("layout:devices/index","devices",repository.findAll());
    }

    @RequestMapping("form")
    public String form() {
        return "layout:devices/form";
    }
}
