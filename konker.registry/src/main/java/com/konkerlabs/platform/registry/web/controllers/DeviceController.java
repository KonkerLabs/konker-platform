package com.konkerlabs.platform.registry.web.controllers;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.web.forms.DeviceRegistrationForm;

import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.function.Supplier;

@Controller
@Scope("request")
@RequestMapping(value = "devices")
public class DeviceController {

    private DeviceRegisterService deviceRegisterService;
    private Tenant tenant;

    @Autowired
    public DeviceController(DeviceRegisterService deviceRegisterService, Tenant tenant) {
        this.deviceRegisterService = deviceRegisterService;
        this.tenant = tenant;
    }

    @RequestMapping
    public ModelAndView index() {
        return new ModelAndView("devices/index", "devices", deviceRegisterService.getAll(tenant));
    }

    @RequestMapping("/new")
    public ModelAndView newDevice() {
        return new ModelAndView("devices/form")
            .addObject("device", new DeviceRegistrationForm())
            .addObject("action", "/devices/save");
    }

    @RequestMapping("/{deviceId}")
    public ModelAndView show(@PathVariable("deviceId") String deviceId) {
        return new ModelAndView("devices/show", "device", deviceRegisterService.findById(deviceId));
    }

    @RequestMapping("/{deviceId}/edit")
    public ModelAndView edit(@PathVariable("deviceId") String deviceId) {
        return new ModelAndView("devices/form")
            .addObject("device", new DeviceRegistrationForm().fillFrom(deviceRegisterService.findById(deviceId)))
            .addObject("isEditing", true)
            .addObject("action", MessageFormat.format("/devices/{0}",deviceId));
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

        Supplier<ServiceResponse<Device>> responseSupplier = () -> {
            ServiceResponse<Device> serviceResponse;
            try {
                serviceResponse = deviceRegisterService.register(tenant, deviceForm.toModel());
            } catch (BusinessException e) {
                serviceResponse = ServiceResponse.<Device>builder()
                        .status(ServiceResponse.Status.ERROR)
                        .responseMessages(Arrays.asList(new String[] { e.getMessage() }))
                        .<Device>build();
            }

            return serviceResponse;
        };

        return doSave(responseSupplier, deviceForm, redirectAttributes);
    }

    @RequestMapping(path = "/{deviceId}", method = RequestMethod.POST)
    public ModelAndView saveEdit(@PathVariable String deviceId,
            @ModelAttribute("deviceForm") DeviceRegistrationForm deviceForm, RedirectAttributes redirectAttributes) {

        Supplier<ServiceResponse<Device>> responseSupplier = () -> {
            ServiceResponse<Device> serviceResponse;
            try {
                serviceResponse = deviceRegisterService.update(deviceId, deviceForm.toModel());
            } catch (BusinessException e) {
                serviceResponse = ServiceResponse.<Device>builder()
                        .status(ServiceResponse.Status.ERROR)
                        .responseMessages(Arrays.asList(new String[] { e.getMessage() }))
                        .<Device>build();
            }

            return serviceResponse;
        };

        return doSave(responseSupplier, deviceForm, redirectAttributes);
    }

    private ModelAndView doSave(Supplier<ServiceResponse<Device>> responseSupplier,
                                DeviceRegistrationForm registrationForm,
                                RedirectAttributes redirectAttributes) {

        ServiceResponse<Device> serviceResponse = responseSupplier.get();

        if (serviceResponse.getStatus().equals(ServiceResponse.Status.OK)) {
            redirectAttributes.addFlashAttribute("message", "Device saved successfully");
            return new ModelAndView(MessageFormat.format("redirect:/devices/{0}", serviceResponse.getResult().getId()));
        } else
            return new ModelAndView("devices/form").addObject("errors", serviceResponse.getResponseMessages())
                    .addObject("device", registrationForm);

    }
}
