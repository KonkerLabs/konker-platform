package com.konkerlabs.platform.registry.web.controllers;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.NewServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.web.forms.DeviceRegistrationForm;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import jdk.nashorn.internal.runtime.options.Options;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Controller
@Scope("request")
@RequestMapping(value = "devices")
public class DeviceController implements ApplicationContextAware {

    public enum Messages {
        DEVICE_REGISTERED_SUCCESSFULLY("controller.device.registered.success"),
        DEVICE_REMOVED_SUCCESSFULLY("controller.device.removed.succesfully");

        public String getCode() {
            return code;
        }

        private String code;

        Messages(String code) {
            this.code = code;
        }
    }

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private DeviceRegisterService deviceRegisterService;
    private Tenant tenant;

    @Autowired
    public DeviceController(DeviceRegisterService deviceRegisterService, Tenant tenant) {
        this.deviceRegisterService = deviceRegisterService;
        this.tenant = tenant;
    }

    @RequestMapping
    public ModelAndView index() {
        List<Device> all = deviceRegisterService.findAll(tenant).getResult();
        return new ModelAndView("devices/index", "devices", all);
    }

    @RequestMapping("/new")
    public ModelAndView newDevice() {
        return new ModelAndView("devices/form")
            .addObject("device", new DeviceRegistrationForm())
            .addObject("action", "/devices/save");
    }

    @RequestMapping("/{deviceId}")
    public ModelAndView show(@PathVariable("deviceId") String deviceId) {
        return new ModelAndView(
                "devices/show", "device",
                deviceRegisterService.getByDeviceId(tenant, deviceId).getResult()
        );
    }

    @RequestMapping("/{deviceId}/edit")
    public ModelAndView edit(@PathVariable("deviceId") String deviceId) {
        return new ModelAndView("devices/form")
            .addObject("device", new DeviceRegistrationForm().fillFrom(deviceRegisterService.getByDeviceId(tenant, deviceId).getResult()))
            .addObject("isEditing", true)
            .addObject("action", MessageFormat.format("/devices/{0}",deviceId))
            .addObject("method", "put");
    }

    @RequestMapping("/{deviceId}/events")
    public ModelAndView deviceEvents(@PathVariable String deviceId) {
        Device device = deviceRegisterService.getByDeviceId(tenant, deviceId).getResult();
        return new ModelAndView("devices/events").addObject("device", device).addObject("recentEvents",
                /*device.getMostRecentEvents()*/Collections.emptyList());
    }

    @RequestMapping(path = "/save", method = RequestMethod.POST)
    public ModelAndView saveNew(@ModelAttribute("deviceForm") DeviceRegistrationForm deviceForm,
                                RedirectAttributes redirectAttributes,
                                Locale locale) {

        return doSave(
                () -> deviceRegisterService.register(tenant, deviceForm.toModel()),
                deviceForm, locale,
                redirectAttributes, "");
    }

    @RequestMapping(path = "/{deviceId}", method = RequestMethod.PUT)
    public ModelAndView saveEdit(@PathVariable String deviceId,
                                 @ModelAttribute("deviceForm") DeviceRegistrationForm deviceForm,
                                 RedirectAttributes redirectAttributes, Locale locale) {

        return doSave(
                () -> deviceRegisterService.update(tenant, deviceId, deviceForm.toModel()),
                deviceForm, locale,
                redirectAttributes,"put");
    }

    @RequestMapping(path = "/{deviceId}", method = RequestMethod.DELETE)
    public ModelAndView remove(@PathVariable String deviceId,
                                 @ModelAttribute("deviceForm") DeviceRegistrationForm deviceForm,
                                 RedirectAttributes redirectAttributes, Locale locale) {

        NewServiceResponse<Device> serviceResponse = deviceRegisterService.remove(tenant, deviceId);
        if(serviceResponse.isOk()){
            redirectAttributes.addFlashAttribute("message",
                    applicationContext.getMessage(Messages.DEVICE_REMOVED_SUCCESSFULLY.getCode(),null,locale)
            );
        } else {
            List<String> messages = serviceResponse.getResponseMessages()
                    .entrySet().stream()
                    .map(message -> applicationContext.getMessage(message.getKey(), message.getValue(), locale))
                    .collect(Collectors.toList());
            redirectAttributes.addFlashAttribute("errors", messages);
        }


        return new ModelAndView("redirect:/devices");
    }

    @RequestMapping(path = "/{deviceId}/password", method = RequestMethod.GET)
    public ModelAndView password(@PathVariable String deviceId, RedirectAttributes redirectAttributes, Locale locale) {
        NewServiceResponse<Device> serviceResponse = deviceRegisterService.getByDeviceId(tenant, deviceId);

        if (serviceResponse.isOk()) {
            Device device = serviceResponse.getResult();
            return new ModelAndView("devices/password")
                    .addObject("action", MessageFormat.format("/devices/{0}/password",deviceId))
                    .addObject("deviceId", device.getDeviceId())
                    .addObject("apiKey", device.getApiKey())
                    .addObject("device", device)
                    .addObject("pubServerInfo", ConfigFactory.load().getConfig("pubServer"));
        } else {
            redirectAttributes.addFlashAttribute("message",
                    applicationContext.getMessage(CommonValidations.RECORD_NULL.getCode(),null,locale));
            return new ModelAndView("redirect:/devices/");
        }
    }

    @RequestMapping(path = "/{deviceId}/password", method = RequestMethod.POST)
    public ModelAndView generatePassword(@PathVariable String deviceId, RedirectAttributes redirectAttributes, Locale locale) {
        NewServiceResponse<DeviceRegisterService.DeviceSecurityCredentials> serviceResponse = deviceRegisterService.
                generateSecurityPassword(tenant,deviceId);

        if (serviceResponse.isOk()) {
            DeviceRegisterService.DeviceSecurityCredentials credentials = serviceResponse.getResult();
            return new ModelAndView("devices/password")
                    .addObject("action", MessageFormat.format("/devices/{0}/password",deviceId))
                    .addObject("deviceId", credentials.getDeviceId())
                    .addObject("apiKey", credentials.getApiKey())
                    .addObject("password", credentials.getPassword());
        } else {
            List<String> messages = serviceResponse.getResponseMessages()
                    .entrySet().stream()
                    .map(message -> applicationContext.getMessage(message.getKey(), message.getValue(), locale))
                    .collect(Collectors.toList());
            redirectAttributes.addFlashAttribute("errors", messages);
            return new ModelAndView(MessageFormat.format("redirect:/devices/{0}/password", deviceId));
        }
    }



    private ModelAndView doSave(Supplier<NewServiceResponse<Device>> responseSupplier,
                                DeviceRegistrationForm registrationForm, Locale locale,
                                RedirectAttributes redirectAttributes, String action) {

        NewServiceResponse<Device> serviceResponse = responseSupplier.get();

        if (serviceResponse.getStatus().equals(NewServiceResponse.Status.OK)) {
            redirectAttributes.addFlashAttribute("message",
                    applicationContext.getMessage(Messages.DEVICE_REGISTERED_SUCCESSFULLY.getCode(),null,locale));
            return new ModelAndView(MessageFormat.format("redirect:/devices/{0}", serviceResponse.getResult().getId()));
        } else {
            List<String> messages = serviceResponse.getResponseMessages()
                    .entrySet().stream()
                    .map(message -> applicationContext.getMessage(message.getKey(), message.getValue(), locale))
                    .collect(Collectors.toList());
            return new ModelAndView("devices/form").addObject("errors", messages)
                    .addObject("device", registrationForm)
                    .addObject("method", action);
        }

    }
}
