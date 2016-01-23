package com.konkerlabs.platform.registry.web.controllers;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping(value = "devices")
public class DeviceController {

    private DeviceRegisterService deviceRegisterService;

    @Autowired
    public DeviceController(DeviceRegisterService deviceRegisterService) {
        this.deviceRegisterService = deviceRegisterService;
    }

    @RequestMapping("/")
    public ModelAndView index() {
        return new ModelAndView("layout:devices/index","devices",deviceRegisterService.getAll());
    }

    @RequestMapping("/new")
    public ModelAndView newDevice() {
        return new ModelAndView("layout:devices/form","device",Device.builder().build());
    }

    @RequestMapping(path = "/save",method = RequestMethod.POST)
    public ModelAndView save(@ModelAttribute("deviceForm") DeviceRegistrationForm deviceForm) {

        ServiceResponse serviceResponse = null;
        try {
            serviceResponse = deviceRegisterService.register(Device.builder()
                    .deviceId(deviceForm.getDeviceId())
                    .name(deviceForm.getName())
                    .description(deviceForm.getDescription())
                    .build()
            );
        } catch (BusinessException e) {

        }

        if (serviceResponse.getStatus().equals(ServiceResponse.Status.OK))
            return new ModelAndView("layout:devices/index","message","Device registered successfully");
        else
            return new ModelAndView("layout:devices/form")
                .addObject("errors",serviceResponse.getResponseMessages())
                .addObject("device",deviceForm);
    }

    @Data
    public static class DeviceRegistrationForm {

        private String deviceId;
        private String name;
        private String description;

    }
}
