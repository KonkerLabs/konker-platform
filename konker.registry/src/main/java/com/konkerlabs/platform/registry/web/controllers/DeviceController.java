package com.konkerlabs.platform.registry.web.controllers;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.web.forms.DeviceRegistrationForm;
import com.typesafe.config.ConfigFactory;

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
    private DeviceEventService deviceEventService;
    private Tenant tenant;

    @Autowired
    public DeviceController(DeviceRegisterService deviceRegisterService, DeviceEventService deviceEventService, Tenant tenant) {
        this.deviceRegisterService = deviceRegisterService;
        this.deviceEventService = deviceEventService;
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

    @RequestMapping("/{deviceGuid}")
    public ModelAndView show(@PathVariable("deviceGuid") String deviceGuid) {
        return new ModelAndView(
                "devices/show", "device",
                deviceRegisterService.getByDeviceGuid(tenant, deviceGuid).getResult()
        );
    }

    @RequestMapping("/{deviceGuid}/edit")
    public ModelAndView edit(@PathVariable("deviceGuid") String deviceGuid) {
        return new ModelAndView("devices/form")
            .addObject("device", new DeviceRegistrationForm().fillFrom(deviceRegisterService.getByDeviceGuid(tenant, deviceGuid).getResult()))
            .addObject("isEditing", true)
            .addObject("action", MessageFormat.format("/devices/{0}",deviceGuid))
            .addObject("method", "put");
    }

    @RequestMapping("/{deviceGuid}/events")
    public ModelAndView deviceEvents(@PathVariable String deviceGuid) {
        Device device = deviceRegisterService.getByDeviceGuid(tenant, deviceGuid).getResult();
		return new ModelAndView("devices/events").addObject("device", device)
				.addObject("recentIncomingEvents",
						deviceEventService.findIncomingBy(tenant, device.getGuid(), null, null, false, 50).getResult())
				.addObject("recentOutgoingEvents",
						deviceEventService.findOutgoingBy(tenant, device.getGuid(), null, null, false, 50).getResult());
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

    @RequestMapping(path = "/{deviceGuid}", method = RequestMethod.PUT)
    public ModelAndView saveEdit(@PathVariable String deviceGuid,
                                 @ModelAttribute("deviceForm") DeviceRegistrationForm deviceForm,
                                 RedirectAttributes redirectAttributes, Locale locale) {

        return doSave(
                () -> deviceRegisterService.update(tenant, deviceGuid, deviceForm.toModel()),
                deviceForm, locale,
                redirectAttributes,"put");
    }

    @RequestMapping(path = "/{deviceGuid}", method = RequestMethod.DELETE)
    public ModelAndView remove(@PathVariable String deviceGuid,
                                 @ModelAttribute("deviceForm") DeviceRegistrationForm deviceForm,
                                 RedirectAttributes redirectAttributes, Locale locale) {

        ServiceResponse<Device> serviceResponse = deviceRegisterService.remove(tenant, deviceGuid);
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

    @RequestMapping(path = "/{deviceGuid}/password", method = RequestMethod.GET)
    public ModelAndView password(@PathVariable String deviceGuid, RedirectAttributes redirectAttributes, Locale locale) {
        ServiceResponse<Device> serviceResponse = deviceRegisterService.getByDeviceGuid(tenant, deviceGuid);

        if (serviceResponse.isOk()) {
            Device device = serviceResponse.getResult();
            return new ModelAndView("devices/password")
                    .addObject("action", MessageFormat.format("/devices/{0}/password",deviceGuid))
                    .addObject("deviceGuid", device.getDeviceId())
                    .addObject("apiKey", device.getApiKey())
                    .addObject("device", device)
                    .addObject("pubServerInfo", ConfigFactory.load().getConfig("pubServer"));
        } else {
            redirectAttributes.addFlashAttribute("message",
                    applicationContext.getMessage(CommonValidations.RECORD_NULL.getCode(),null,locale));
            return new ModelAndView("redirect:/devices/");
        }
    }

	@RequestMapping(path = "/{deviceGuid}/password", method = RequestMethod.POST)
	public ModelAndView generatePassword(@PathVariable String deviceGuid, RedirectAttributes redirectAttributes,
			Locale locale) {
		ServiceResponse<DeviceRegisterService.DeviceSecurityCredentials> serviceResponse = deviceRegisterService
				.generateSecurityPassword(tenant, deviceGuid);
		

        if (serviceResponse.isOk()) {
            DeviceRegisterService.DeviceSecurityCredentials credentials = serviceResponse.getResult();
            return new ModelAndView("devices/password")
                    .addObject("action", MessageFormat.format("/devices/{0}/password",deviceGuid))
                    .addObject("password", credentials.getPassword())
                    .addObject("device", credentials.getDevice())
                    .addObject("pubServerInfo", ConfigFactory.load().getConfig("pubServer"));
        } else {
            List<String> messages = serviceResponse.getResponseMessages()
                    .entrySet().stream()
                    .map(message -> applicationContext.getMessage(message.getKey(), message.getValue(), locale))
                    .collect(Collectors.toList());
            redirectAttributes.addFlashAttribute("errors", messages);
            return new ModelAndView(MessageFormat.format("redirect:/devices/{0}/password", deviceGuid));
        }
    }


    private ModelAndView doSave(Supplier<ServiceResponse<Device>> responseSupplier,
                                DeviceRegistrationForm registrationForm, Locale locale,
                                RedirectAttributes redirectAttributes, String action) {

        ServiceResponse<Device> serviceResponse = responseSupplier.get();

        if (serviceResponse.getStatus().equals(ServiceResponse.Status.OK)) {
            redirectAttributes.addFlashAttribute("message",
                    applicationContext.getMessage(Messages.DEVICE_REGISTERED_SUCCESSFULLY.getCode(),null,locale));
            return new ModelAndView(MessageFormat.format("redirect:/devices/{0}", serviceResponse.getResult().getGuid()));
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
