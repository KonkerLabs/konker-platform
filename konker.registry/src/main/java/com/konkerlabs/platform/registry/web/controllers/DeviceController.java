package com.konkerlabs.platform.registry.web.controllers;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.web.forms.DeviceRegistrationForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
		return new ModelAndView("devices/form", "device", Device.builder().build());
	}

	@RequestMapping(path = "/save", method = RequestMethod.POST)
	public ModelAndView save(@ModelAttribute("deviceForm") DeviceRegistrationForm deviceForm,
			RedirectAttributes redirectAttributes) {

		ServiceResponse serviceResponse = null;
		try {
			serviceResponse = deviceRegisterService.register(Device.builder().deviceId(deviceForm.getDeviceId())
					.name(deviceForm.getName()).description(deviceForm.getDescription()).build());
		} catch (BusinessException e) {
			return new ModelAndView("devices/form").addObject("errors", Arrays.asList(new String[] { e.getMessage() }))
					.addObject("device", deviceForm);
		}

		if (serviceResponse.getStatus().equals(ServiceResponse.Status.OK)) {
			redirectAttributes.addFlashAttribute("message", "Device registered successfully");
			return new ModelAndView("redirect:/devices");
		} else
			return new ModelAndView("devices/form").addObject("errors", serviceResponse.getResponseMessages())
					.addObject("device", deviceForm);
	}

	@RequestMapping("/{deviceId}/show")
	public ModelAndView show(@PathVariable("deviceId") String deviceId) {
		return new ModelAndView("devices/show", "device", deviceRegisterService.findById(deviceId));
	}

	@RequestMapping("/{deviceId}/events")
	public ModelAndView deviceEvents(@PathVariable String deviceId) {
		Device device = deviceRegisterService.findById(deviceId);
		return new ModelAndView("devices/events").addObject("device", device).addObject("recentEvents",
				device.getMostRecentEvents());
	}
}
