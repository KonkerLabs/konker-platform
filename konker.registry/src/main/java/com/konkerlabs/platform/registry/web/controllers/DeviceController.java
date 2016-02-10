package com.konkerlabs.platform.registry.web.controllers;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.web.forms.DeviceRegistrationForm;

import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.MessageFormat;
import java.util.Arrays;

@Controller
@RequestMapping(value = "devices")
public class DeviceController {

    private DeviceRegisterService deviceRegisterService;

    @Autowired
    public DeviceController(DeviceRegisterService deviceRegisterService) {
        this.deviceRegisterService = deviceRegisterService;
    }

    @RequestMapping
    public ModelAndView index() {
        return new ModelAndView("devices/index", "devices", deviceRegisterService.getAll());
    }

    @RequestMapping("/new")
    public ModelAndView newDevice() {
        return new ModelAndView("devices/new-form", "device", Device.builder().build());
    }

    @RequestMapping("/{deviceId}")
    public ModelAndView show(@PathVariable("deviceId") String deviceId) {
        return new ModelAndView("devices/show", "device", deviceRegisterService.findById(deviceId));
    }

    @RequestMapping("/{deviceId}/edit")
    public ModelAndView edit(@PathVariable("deviceId") String deviceId) {
        return new ModelAndView("devices/edit-form", "device", deviceRegisterService.findById(deviceId));
    }

    @RequestMapping("/{deviceId}/events")
    public ModelAndView deviceEvents(@PathVariable String deviceId) {
        Device device = deviceRegisterService.findById(deviceId);
        return new ModelAndView("devices/events").addObject("device", device).addObject("recentEvents",
                device.getMostRecentEvents());
    }

    @RequestMapping(path = "/save", method = RequestMethod.POST)
    public ModelAndView saveNew(@ModelAttribute("deviceForm") DeviceRegistrationForm deviceForm,
            RedirectAttributes redirectAttributes) {

        ServiceResponse serviceResponse = null;
        try {
            serviceResponse = deviceRegisterService.register(deviceForm.toModel());
        } catch (BusinessException e) {
            return new ModelAndView("devices/new-form").addObject("errors", Arrays.asList(new String[] { e.getMessage() }))
                    .addObject("device", deviceForm);
        }

        if (serviceResponse.getStatus().equals(ServiceResponse.Status.OK)) {
            redirectAttributes.addFlashAttribute("message", "Device registered successfully");
            return new ModelAndView("redirect:/devices");
        } else
            return new ModelAndView("devices/new-form").addObject("errors", serviceResponse.getResponseMessages())
                    .addObject("device", deviceForm);
    }

    @RequestMapping(path = "/{deviceId}", method = RequestMethod.POST)
    public ModelAndView saveEdit(@PathVariable String deviceId,
            @ModelAttribute("deviceForm") DeviceRegistrationForm deviceForm, RedirectAttributes redirectAttributes) {

        ServiceResponse serviceResponse = null;
        try {
            serviceResponse = deviceRegisterService.update(deviceId, Device.builder().deviceId(deviceId)
                    .name(deviceForm.getName()).description(deviceForm.getDescription()).build());
        } catch (BusinessException e) {
            return new ModelAndView("devices/edit-form")
                    .addObject("errors", Arrays.asList(new String[] { e.getMessage() }))
                    .addObject("device", deviceForm);
        }

        if (serviceResponse.getStatus().equals(ServiceResponse.Status.OK)) {
            redirectAttributes.addFlashAttribute("message", "Device saved successfully");
            return new ModelAndView(MessageFormat.format("redirect:/devices/{0}", deviceForm.getDeviceId()));
        } else
            return new ModelAndView("devices/edit-form").addObject("errors", serviceResponse.getResponseMessages())
                    .addObject("device", deviceForm);
    }

}
