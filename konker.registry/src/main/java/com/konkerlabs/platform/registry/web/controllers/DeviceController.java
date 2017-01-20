package com.konkerlabs.platform.registry.web.controllers;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.EventSchema;
import com.konkerlabs.platform.registry.business.model.EventSchema.SchemaField;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.EventSchemaService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.config.PubServerConfig;
import com.konkerlabs.platform.registry.web.forms.DeviceRegistrationForm;

@Controller
@Scope("request")
@RequestMapping(value = "devices")
public class DeviceController implements ApplicationContextAware {

	private static final Logger LOGGER = LoggerFactory.getLogger(DeviceController.class);
	
    public enum Messages {
        DEVICE_REGISTERED_SUCCESSFULLY("controller.device.registered.success"),
        DEVICE_REMOVED_SUCCESSFULLY("controller.device.removed.succesfully"),
        DEVICE_REMOVED_UNSUCCESSFULLY("controller.device.removed.unsuccesfully"),
        DEVICE_QRCODE_ERROR("service.device.qrcode.have_errors");

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
    private EventSchemaService eventSchemaService;
    
    private Tenant tenant;
    private User user;
    private PubServerConfig pubServerConfig;
    
    @Autowired
    public DeviceController(DeviceRegisterService deviceRegisterService, DeviceEventService deviceEventService, EventSchemaService eventSchemaService, Tenant tenant, User user, PubServerConfig pubServerConfig) {
        this.deviceRegisterService = deviceRegisterService;
        this.deviceEventService = deviceEventService;
        this.eventSchemaService = eventSchemaService;
        this.tenant = tenant;
        this.user = user;
        this.pubServerConfig = pubServerConfig;
    }

    @RequestMapping
    @PreAuthorize("hasAuthority('LIST_DEVICES')")
    public ModelAndView index() {
        List<Device> all = deviceRegisterService.findAll(tenant).getResult();
        return new ModelAndView("devices/index", "devices", all);
    }

    @RequestMapping("/new")
    @PreAuthorize("hasAuthority('ADD_DEVICE')")
    public ModelAndView newDevice() {
        return new ModelAndView("devices/form")
                .addObject("device", new DeviceRegistrationForm())
                .addObject("action", "/devices/save");
    }

    @RequestMapping("/{deviceGuid}")
    @PreAuthorize("hasAuthority('SHOW_DEVICE')")
    public ModelAndView show(@PathVariable("deviceGuid") String deviceGuid) {
        return new ModelAndView(
                "devices/show", "device",
                deviceRegisterService.getByDeviceGuid(tenant, deviceGuid).getResult()
        );
    }

    @RequestMapping("/{deviceGuid}/edit")
    @PreAuthorize("hasAuthority('EDIT_DEVICE')")
    public ModelAndView edit(@PathVariable("deviceGuid") String deviceGuid) {
        return new ModelAndView("devices/form")
                .addObject("device", new DeviceRegistrationForm().fillFrom(deviceRegisterService.getByDeviceGuid(tenant, deviceGuid).getResult()))
                .addObject("isEditing", true)
                .addObject("action", MessageFormat.format("/devices/{0}", deviceGuid))
                .addObject("method", "put");
    }

	@RequestMapping("/{deviceGuid}/events")
	@PreAuthorize("hasAuthority('VIEW_DEVICE_LOG')")
	public ModelAndView deviceEvents(@PathVariable String deviceGuid) {
		Device device = deviceRegisterService.getByDeviceGuid(tenant, deviceGuid).getResult();

		ModelAndView mv = new ModelAndView("devices/events");

		mv.addObject("recentIncomingEvents", deviceEventService.findIncomingBy(tenant, device.getGuid(), null, null, null, false, 50).getResult())
		  .addObject("recentOutgoingEvents", deviceEventService.findOutgoingBy(tenant, device.getGuid(), null, null, null, false, 50).getResult());

		addChartObjects(device, mv);

		return mv;
	}

	@RequestMapping("/{deviceGuid}/events/incoming")
	@PreAuthorize("hasAuthority('VIEW_DEVICE_LOG')")
	public ModelAndView loadIncomingEvents(@PathVariable String deviceGuid,
			@RequestParam(required = false) String dateStart,
   		 	@RequestParam(required = false) String dateEnd) {
		Device device = deviceRegisterService.getByDeviceGuid(tenant, deviceGuid).getResult();

    	DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss", user.getLanguage().getLocale());
    	Instant instantStart = StringUtils.isNotEmpty(dateStart) ? ZonedDateTime.of(LocalDateTime.parse(dateStart, dtf), ZoneId.of(user.getZoneId().getId())).toInstant() : null;
    	Instant instantEnd = StringUtils.isNotEmpty(dateEnd) ? ZonedDateTime.of(LocalDateTime.parse(dateEnd, dtf), ZoneId.of(user.getZoneId().getId())).toInstant() : null;

		ModelAndView mv = new ModelAndView("devices/events-incoming", "recentIncomingEvents", 
				deviceEventService.findIncomingBy(tenant, device.getGuid(), null, instantStart, instantEnd, false, 50).getResult());

		return mv;
	}

	@RequestMapping("/{deviceGuid}/events/outgoing")
	@PreAuthorize("hasAuthority('VIEW_DEVICE_LOG')")
	public ModelAndView loadOutgoingEvents(@PathVariable String deviceGuid,
			@RequestParam(required = false) String dateStart,
   		 	@RequestParam(required = false) String dateEnd) {
		Device device = deviceRegisterService.getByDeviceGuid(tenant, deviceGuid).getResult();

    	DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss", user.getLanguage().getLocale());
    	Instant instantStart = StringUtils.isNotEmpty(dateStart) ? ZonedDateTime.of(LocalDateTime.parse(dateStart, dtf), ZoneId.of(user.getZoneId().getId())).toInstant() : null;
    	Instant instantEnd = StringUtils.isNotEmpty(dateEnd) ? ZonedDateTime.of(LocalDateTime.parse(dateEnd, dtf), ZoneId.of(user.getZoneId().getId())).toInstant() : null;

		ModelAndView mv = new ModelAndView("devices/events-outgoing", "recentOutgoingEvents", 
				deviceEventService.findOutgoingBy(tenant, device.getGuid(), null, instantStart, instantEnd, false, 50).getResult());

		return mv;
	}

	private void addChartObjects(Device device, ModelAndView mv) {

		String deviceGuid = device.getGuid();

		String defaultChannel = null;
		String defaultMetric = null;
		List<String> listMetrics = null;

		// Try to find the last numeric metric
		List<Event> lastEvents = deviceEventService.findIncomingBy(tenant, deviceGuid, null, null, null, false, 200).getResult();

		for (Event event : lastEvents) {
			if (event.getIncoming() == null) {
				continue;
			}
			
			ServiceResponse<EventSchema> schemaResponse = eventSchemaService.findIncomingBy(deviceGuid, event.getIncoming().getChannel());
			EventSchema schema = schemaResponse.getResult();
			if (schema == null) {
				continue;
			}

			for (SchemaField field :  schema.getFields()) {
				if (field.getKnownTypes().contains(JsonNodeType.NUMBER)) {
					try {
						// check if node exists
						JSONObject json = new JSONObject(event.getPayload());
						if (json.get(field.getPath()) != null) {
							defaultChannel = event.getIncoming().getChannel();
							defaultMetric = field.getPath();
							break;
						}
					} catch (JSONException e) {
						LOGGER.warn(e.getMessage());
					}
				}
			}

			// found?
			if (defaultChannel != null) {
				break; 
			}
		}

		// Load lists
		ServiceResponse<List<String>> channels = eventSchemaService.findKnownIncomingChannelsBy(tenant, deviceGuid);

		if (defaultChannel != null && !channels.getResult().contains(defaultChannel)) {
			defaultChannel = null; // invalid channel
		}

		if (defaultChannel != null) {

			ServiceResponse<List<String>> metrics = eventSchemaService.findKnownIncomingMetricsBy(tenant, deviceGuid, defaultChannel, JsonNodeType.NUMBER);

	    	listMetrics = metrics.isOk() ? metrics.getResult() : new ArrayList<>();

	    	if (defaultMetric != null && !listMetrics.contains(defaultMetric)) {
	    		defaultMetric = null; // invalid metric
	    	}

		}

		// Add objects
		mv.addObject("device", device)
		  .addObject("channels", channels.getResult())
		  .addObject("defaultChannel", defaultChannel)
		  .addObject("metrics", listMetrics)
		  .addObject("defaultMetric", defaultMetric);

	}

    @RequestMapping(path = "/save", method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('ADD_DEVICE')")
    public ModelAndView saveNew(@ModelAttribute("deviceForm") DeviceRegistrationForm deviceForm,
                                RedirectAttributes redirectAttributes,
                                Locale locale) {

        return doSave(
                () -> deviceRegisterService.register(tenant, deviceForm.toModel()),
                deviceForm, locale,
                redirectAttributes, "");
    }

    @RequestMapping(path = "/{deviceGuid}", method = RequestMethod.PUT)
    @PreAuthorize("hasAuthority('EDIT_DEVICE')")
    public ModelAndView saveEdit(@PathVariable String deviceGuid,
                                 @ModelAttribute("deviceForm") DeviceRegistrationForm deviceForm,
                                 RedirectAttributes redirectAttributes, Locale locale) {

        return doSave(
                () -> deviceRegisterService.update(tenant, deviceGuid, deviceForm.toModel()),
                deviceForm, locale,
                redirectAttributes, "put");
    }

    @RequestMapping(path = "/{deviceGuid}", method = RequestMethod.DELETE)
    @PreAuthorize("hasAuthority('REMOVE_DEVICE')")
    public ModelAndView remove(@PathVariable String deviceGuid,
                               @ModelAttribute("deviceForm") DeviceRegistrationForm deviceForm,
                               RedirectAttributes redirectAttributes, Locale locale) {

        ServiceResponse<Device> serviceResponse = deviceRegisterService.remove(tenant, deviceGuid);
        if (serviceResponse.isOk()) {
            redirectAttributes.addFlashAttribute("message",
                    applicationContext.getMessage(Messages.DEVICE_REMOVED_SUCCESSFULLY.getCode(), null, locale)
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
    @PreAuthorize("hasAuthority('CREATE_DEVICE_KEYS')")
    public ModelAndView password(@PathVariable String deviceGuid, RedirectAttributes redirectAttributes, Locale locale) {
        ServiceResponse<Device> serviceResponse = deviceRegisterService.getByDeviceGuid(tenant, deviceGuid);
        if (serviceResponse.isOk()) {
            Device device = serviceResponse.getResult();

            return new ModelAndView("devices/password")
                    .addObject("action", MessageFormat.format("/devices/{0}/password", deviceGuid))
                    .addObject("deviceGuid", device.getDeviceId())
                    .addObject("apiKey", device.getApiKey())
                    .addObject("device", device)
                    .addObject("pubServerInfo", pubServerConfig);


        } else {
            redirectAttributes.addFlashAttribute("message",
                    applicationContext.getMessage(CommonValidations.RECORD_NULL.getCode(), null, locale));
            return new ModelAndView("redirect:/devices/");
        }
    }

    @RequestMapping(path = "/{deviceGuid}/password", method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('CREATE_DEVICE_KEYS')")
    public ModelAndView generatePassword(@PathVariable String deviceGuid,
                                         RedirectAttributes redirectAttributes,
                                         Locale locale) {
        ServiceResponse<DeviceRegisterService.DeviceSecurityCredentials> serviceResponse = deviceRegisterService
                .generateSecurityPassword(tenant, deviceGuid);

        if (serviceResponse.isOk()) {
            DeviceRegisterService.DeviceSecurityCredentials credentials = serviceResponse.getResult();
            ServiceResponse<String> base64QrCode =
                    deviceRegisterService.generateQrCodeAccess(credentials, 200, 200);

            return new ModelAndView("devices/password")
                    .addObject("action", MessageFormat.format("/devices/{0}/password", deviceGuid))
                    .addObject("password", credentials.getPassword())
                    .addObject("device", credentials.getDevice())
                    .addObject("pubServerInfo", pubServerConfig)
                    .addObject("qrcode", base64QrCode.getResult());
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
                    applicationContext.getMessage(Messages.DEVICE_REGISTERED_SUCCESSFULLY.getCode(), null, locale));
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
