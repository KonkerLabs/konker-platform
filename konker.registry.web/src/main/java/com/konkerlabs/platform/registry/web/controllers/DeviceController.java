package com.konkerlabs.platform.registry.web.controllers;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.EventSchema;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService.DeviceDataURLs;
import com.konkerlabs.platform.registry.business.services.api.EventSchemaService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.config.MapGeolocationConfig;
import com.konkerlabs.platform.registry.config.PubServerConfig;
import com.konkerlabs.platform.registry.web.forms.DeviceRegistrationForm;
import com.konkerlabs.platform.utilities.parsers.json.JsonParsingService;

@Controller
@Scope("request")
@RequestMapping(value = "devices")
public class DeviceController implements ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceController.class);

    public static class ChannelVO {
        private String name;
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ChannelVO other = (ChannelVO) obj;
            if (name == null) {
                if (other.name != null)
                    return false;
            } else if (!name.equals(other.name))
                return false;
            return true;
        }

        private boolean isDefaultChannel = false;

        public ChannelVO(String channelName) {
            this.name = channelName;
        }

        public String getName() {
            return name;
        }

        public void setDefault() {
            this.isDefaultChannel = true;
        }

        public boolean isDefault() {
            return isDefaultChannel;
        }
    }

    public static class MetricVO {
        private String name;
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            MetricVO other = (MetricVO) obj;
            if (name == null) {
                if (other.name != null)
                    return false;
            } else if (!name.equals(other.name))
                return false;
            return true;
        }

        private boolean isDefaultMetric = false;

        public MetricVO(String channelName) {
            this.name = channelName;
        }

        public String getName() {
            return name;
        }

        public void setDefault() {
            this.isDefaultMetric = true;
        }

        public boolean isDefault() {
            return isDefaultMetric;
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
    private ApplicationService applicationService;
    private JsonParsingService jsonParsing;

    private Tenant tenant;
    private User user;
    private Application application;
    private PubServerConfig pubServerConfig = new PubServerConfig();
    private MapGeolocationConfig mapGeolocationConfig = new MapGeolocationConfig();

    @Autowired
    public DeviceController(DeviceRegisterService deviceRegisterService,
    		DeviceEventService deviceEventService,
    		EventSchemaService eventSchemaService,
    		Tenant tenant,
    		Application application,
    		User user,
    		ApplicationService applicationService,
    		JsonParsingService jsonParsing) {
        this.deviceRegisterService = deviceRegisterService;
        this.deviceEventService = deviceEventService;
        this.eventSchemaService = eventSchemaService;
        this.tenant = tenant;
        this.application = application;
        this.user = user;
        this.applicationService = applicationService;
        this.jsonParsing = jsonParsing;
    }

    @RequestMapping
    @PreAuthorize("hasAuthority('LIST_DEVICES')")
    public ModelAndView index() {
    	List<Application> applications = applicationService.findAll(tenant).getResult();
    	List<Device> all = new ArrayList<>();

    	applications.forEach(
    			app -> all.addAll(deviceRegisterService.findAll(tenant, app).getResult()));

        return new ModelAndView("devices/index", "devices", all);
    }

    @RequestMapping("/new")
    @PreAuthorize("hasAuthority('ADD_DEVICE')")
    public ModelAndView newDevice() {
        return new ModelAndView("devices/form")
                .addObject("device", new DeviceRegistrationForm())
                .addObject("action", MessageFormat.format("/devices/{0}/save", application.getName()));
    }

    @RequestMapping("/{applicationName}/{deviceGuid}")
    @PreAuthorize("hasAuthority('SHOW_DEVICE')")
    public ModelAndView show(@PathVariable("deviceGuid") String deviceGuid, @PathVariable("applicationName") String applicationName) {
    	application = applicationService.getByApplicationName(tenant, applicationName).getResult();
        return new ModelAndView(
                "devices/show", "device",
                deviceRegisterService.getByDeviceGuid(
                		tenant,
                		application,
                		deviceGuid).getResult()
        );
    }

    @RequestMapping("/{applicationName}/{deviceGuid}/edit")
    @PreAuthorize("hasAuthority('EDIT_DEVICE')")
    public ModelAndView edit(@PathVariable("deviceGuid") String deviceGuid, @PathVariable("applicationName") String applicationName) {
    	application = applicationService.getByApplicationName(tenant, applicationName).getResult();
        return new ModelAndView("devices/form")
                .addObject("device", new DeviceRegistrationForm().fillFrom(deviceRegisterService.getByDeviceGuid(
                		tenant,
                		application,
                		deviceGuid).getResult()))
                .addObject("isEditing", true)
                .addObject("action", MessageFormat.format("/devices/{0}/{1}", application.getName(), deviceGuid))
                .addObject("method", "put");
    }

    @RequestMapping("/{applicationName}/{deviceGuid}/events")
    @PreAuthorize("hasAuthority('VIEW_DEVICE_LOG')")
    public ModelAndView deviceEvents(@PathVariable String deviceGuid, @PathVariable("applicationName") String applicationName) {
    	application = applicationService.getByApplicationName(tenant, applicationName).getResult();
        Device device = deviceRegisterService.getByDeviceGuid(
        		tenant,
        		application,
        		deviceGuid).getResult();

        ModelAndView mv = new ModelAndView("devices/events");

        List<Event> incomingEvents = deviceEventService.findIncomingBy(tenant, application, device.getGuid(), null, null, null, false, 50).getResult();
        List<Event> outgoingEvents = deviceEventService.findOutgoingBy(tenant, application, device.getGuid(), null, null, null, false, 50).getResult();

        boolean hasAnyEvent = !incomingEvents.isEmpty() || !outgoingEvents.isEmpty();
        
        mv.addObject("userDateFormat", user.getDateFormat().name())
                .addObject("recentIncomingEvents", incomingEvents)
                .addObject("recentOutgoingEvents", outgoingEvents)
                .addObject("hasAnyEvent", hasAnyEvent);
        
        if (mapGeolocationConfig.isEnabled()) {
        	String eventsJson = parseToJson(incomingEvents);
        	mv.addObject("eventsJson", eventsJson)
        		.addObject("mapApiKey", mapGeolocationConfig.getApiKey())
        		.addObject("titleMapDetail", applicationContext.getMessage(DeviceRegisterService.Messages.DEVICE_TITLE_MAP_DETAIL.getCode(), null, user.getLanguage().getLocale()))
        		.addObject("lastDataLabel", applicationContext.getMessage(DeviceRegisterService.Messages.DEVICE_LAST_DATA_LABEL.getCode(), null, user.getLanguage().getLocale()))
        		.addObject("lastIngestedTimeLabel", applicationContext.getMessage(DeviceRegisterService.Messages.DEVICE_LAST_INGESTED_TIME_LABEL.getCode(), null, user.getLanguage().getLocale()));
        }
        
        addChartObjects(device, mv);

        return mv;
    }

	private String parseToJson(List<Event> incomingEvents) {
		String json = "";
		try {
			Optional<Event> lastEventGeotagged = incomingEvents
					.stream()
					.filter(event -> Optional.ofNullable(event.getGeolocation()).isPresent())
					.findFirst();
			
			if (lastEventGeotagged.isPresent()) {
				json = jsonParsing.toJsonString(Collections.singletonMap("events", Collections.singletonList(lastEventGeotagged.get())));
			}
		} catch (JsonProcessingException e) {
			LOGGER.error(e.getMessage());
		}
		return json;
	}

    @RequestMapping("/{applicationName}/{deviceGuid}/events/incoming")
    @PreAuthorize("hasAuthority('VIEW_DEVICE_LOG')")
    public ModelAndView loadIncomingEvents(@PathVariable String deviceGuid,
    									   @PathVariable("applicationName") String applicationName,
                                           @RequestParam(required = false) String dateStart,
                                           @RequestParam(required = false) String dateEnd) {
    	application = applicationService.getByApplicationName(tenant, applicationName).getResult();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss", user.getLanguage().getLocale());
        Instant instantStart = StringUtils.isNotEmpty(dateStart) ? ZonedDateTime.of(LocalDateTime.parse(dateStart, dtf), ZoneId.of(user.getZoneId().getId())).toInstant() : null;
        Instant instantEnd = StringUtils.isNotEmpty(dateEnd) ? ZonedDateTime.of(LocalDateTime.parse(dateEnd, dtf), ZoneId.of(user.getZoneId().getId())).toInstant() : null;

        ModelAndView mv = new ModelAndView("devices/events-incoming", "recentIncomingEvents",
                deviceEventService.findIncomingBy(tenant, application, deviceGuid, null, instantStart, instantEnd, false, 50).getResult());

        return mv;
    }

    @RequestMapping("/{applicationName}/{deviceGuid}/events/outgoing")
    @PreAuthorize("hasAuthority('VIEW_DEVICE_LOG')")
    public ModelAndView loadOutgoingEvents(@PathVariable String deviceGuid,
    		 							   @PathVariable("applicationName") String applicationName,
                                           @RequestParam(required = false) String dateStart,
                                           @RequestParam(required = false) String dateEnd) {
    	application = applicationService.getByApplicationName(tenant, applicationName).getResult();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss", user.getLanguage().getLocale());
        Instant instantStart = StringUtils.isNotEmpty(dateStart) ? ZonedDateTime.of(LocalDateTime.parse(dateStart, dtf), ZoneId.of(user.getZoneId().getId())).toInstant() : null;
        Instant instantEnd = StringUtils.isNotEmpty(dateEnd) ? ZonedDateTime.of(LocalDateTime.parse(dateEnd, dtf), ZoneId.of(user.getZoneId().getId())).toInstant() : null;

        ModelAndView mv = new ModelAndView("devices/events-outgoing", "recentOutgoingEvents",
                deviceEventService.findOutgoingBy(tenant, application, deviceGuid, null, instantStart, instantEnd, false, 50).getResult());

        return mv;
    }

    private void addChartObjects(Device device, ModelAndView mv) {

        String deviceGuid = device.getGuid();

        String defaultChannel = null;
        String defaultMetric = null;
        List<String> listMetrics = Collections.emptyList();

        // Try to find the last numeric metric
        ServiceResponse<EventSchema> lastEvent = eventSchemaService.findLastIncomingBy(tenant, application, deviceGuid, JsonNodeType.NUMBER);
        if (lastEvent.isOk() && lastEvent.getResult() != null) {
            defaultChannel = lastEvent.getResult().getChannel();
            defaultMetric = lastEvent.getResult().getFields().iterator().next().getPath();
        }

        // Load lists
        ServiceResponse<List<String>> channelsServiceResponse = eventSchemaService.findKnownIncomingChannelsBy(tenant, application, deviceGuid);

        if (defaultChannel != null && !channelsServiceResponse.getResult().contains(defaultChannel)) {
            defaultChannel = null; // invalid channel
        }

        if (defaultChannel != null) {

            ServiceResponse<List<String>> metrics = eventSchemaService.findKnownIncomingMetricsBy(tenant, application, deviceGuid, defaultChannel, JsonNodeType.NUMBER);

            listMetrics = metrics.isOk() ? metrics.getResult() : new ArrayList<>();

            if (defaultMetric != null && !listMetrics.contains(defaultMetric)) {
                defaultMetric = null; // invalid metric
            }

        }

        // Check if there is any numeric metric
        boolean existsNumericMetric = false;

        ServiceResponse<List<String>> allNumericMetrics = eventSchemaService.findKnownIncomingMetricsBy(tenant, application, deviceGuid, JsonNodeType.NUMBER);
        if (allNumericMetrics.isOk() && !allNumericMetrics.getResult().isEmpty()) {
            existsNumericMetric = true;
        }

        // prepare a list of VOs to be displayed

        List<ChannelVO> channels = new ArrayList<ChannelVO>();
        for (String channelName : channelsServiceResponse.getResult()) {
            ChannelVO channelVO = new ChannelVO(channelName);
            if (channelVO.getName().equals(defaultChannel)) {
                channelVO.setDefault();
            }
            channels.add(channelVO);
        }

        List<MetricVO> metrics = new ArrayList<MetricVO>();
        for (String metricName : listMetrics) {
            MetricVO metricVO = new MetricVO(metricName);
            if (metricVO.getName().equals(defaultMetric)) {
                metricVO.setDefault();
            }
            metrics.add(metricVO);
        }

        // Add objects
        mv.addObject("device", device)
          .addObject("channels", channels)
          .addObject("metrics", metrics)
          .addObject("existsNumericMetric", existsNumericMetric);

    }

    @RequestMapping(path = "/{applicationName}/save", method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('ADD_DEVICE')")
    public ModelAndView saveNew(@PathVariable("applicationName") String applicationName,
    							@ModelAttribute("deviceForm") DeviceRegistrationForm deviceForm,
                                RedirectAttributes redirectAttributes,
                                Locale locale) {
    	application = applicationService.getByApplicationName(tenant, applicationName).getResult();

        return doSave(
                () -> deviceRegisterService.register(
                		tenant,
                		application,
                		deviceForm.toModel()),
                deviceForm, locale,
                redirectAttributes, "");
    }

    @RequestMapping(path = "/{applicationName}/{deviceGuid}", method = RequestMethod.PUT)
    @PreAuthorize("hasAuthority('EDIT_DEVICE')")
    public ModelAndView saveEdit(@PathVariable String deviceGuid,
    							 @PathVariable("applicationName") String applicationName,
                                 @ModelAttribute("deviceForm") DeviceRegistrationForm deviceForm,
                                 RedirectAttributes redirectAttributes, Locale locale) {
    	application = applicationService.getByApplicationName(tenant, applicationName).getResult();

        Device deviceUpdated = deviceForm.toModel();

    	ServiceResponse<Device> deviceFindResponse = deviceRegisterService.getByDeviceGuid(tenant, application, deviceGuid);
    	if (deviceFindResponse.isOk()) {
            // set non editable at screen fields
            deviceUpdated.setLocation(deviceFindResponse.getResult().getLocation());
            deviceUpdated.setDeviceModel(deviceFindResponse.getResult().getDeviceModel());
    	}

        return doSave(
                () -> deviceRegisterService.update(
                		tenant,
                		application,
                		deviceGuid,
                		deviceUpdated),
                deviceForm, locale,
                redirectAttributes, "put");
    }

    @RequestMapping(path = "/{applicationName}/{deviceGuid}", method = RequestMethod.DELETE)
    @PreAuthorize("hasAuthority('REMOVE_DEVICE')")
    public ModelAndView remove(@PathVariable String deviceGuid,
    						   @PathVariable("applicationName") String applicationName,
                               @ModelAttribute("deviceForm") DeviceRegistrationForm deviceForm,
                               RedirectAttributes redirectAttributes, Locale locale) {
    	application = applicationService.getByApplicationName(tenant, applicationName).getResult();

        ServiceResponse<Device> serviceResponse = deviceRegisterService.remove(
        		tenant,
        		application,
        		deviceGuid);
        if (serviceResponse.isOk()) {
            redirectAttributes.addFlashAttribute("message",
                    applicationContext.getMessage(DeviceRegisterService.Messages.DEVICE_REMOVED_SUCCESSFULLY.getCode(), null, locale)
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

    @RequestMapping(path = "/{applicationName}/{deviceGuid}/password", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('CREATE_DEVICE_KEYS')")
    public ModelAndView password(@PathVariable String deviceGuid, @PathVariable("applicationName") String applicationName, RedirectAttributes redirectAttributes, Locale locale) {
    	application = applicationService.getByApplicationName(tenant, applicationName).getResult();
        ServiceResponse<Device> serviceResponse = deviceRegisterService.getByDeviceGuid(
        		tenant,
        		application,
        		deviceGuid);

        if (serviceResponse.isOk()) {

            Device device = serviceResponse.getResult();
            String username = device.getApiKey();

            ServiceResponse<DeviceDataURLs> serviceURLResponse = deviceRegisterService.getDeviceDataURLs(
            		tenant,
            		application,
            		device,
            		user.getLanguage().getLocale());

            return new ModelAndView("devices/password")
                    .addObject("action", MessageFormat.format("/devices/{0}/{1}/password", application.getName(), deviceGuid))
                    .addObject("deviceGuid", device.getDeviceId())
                    .addObject("apiKey", username)
                    .addObject("device", device)
                    .addObject("deviceDataURLs", serviceURLResponse.getResult());

        } else {
            redirectAttributes.addFlashAttribute("message",
                    applicationContext.getMessage(CommonValidations.RECORD_NULL.getCode(), null, locale));
            return new ModelAndView("redirect:/devices/");
        }
    }

    @RequestMapping(path = "/{applicationName}/{deviceGuid}/password", method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('CREATE_DEVICE_KEYS')")
    public ModelAndView generatePassword(@PathVariable String deviceGuid,
    									 @PathVariable("applicationName") String applicationName,
                                         RedirectAttributes redirectAttributes,
                                         Locale locale) {
    	application = applicationService.getByApplicationName(tenant, applicationName).getResult();

        ServiceResponse<DeviceRegisterService.DeviceSecurityCredentials> serviceResponse = deviceRegisterService
                .generateSecurityPassword(tenant, application, deviceGuid);

        if (serviceResponse.isOk()) {
            DeviceRegisterService.DeviceSecurityCredentials credentials = serviceResponse.getResult();
            ServiceResponse<String> base64QrCode =
                    deviceRegisterService.generateQrCodeAccess(credentials, 200, 200);

            Device device = credentials.getDevice();
            ServiceResponse<DeviceDataURLs> serviceURLResponse = deviceRegisterService.getDeviceDataURLs(
            		tenant,
            		application,
            		device,
            		user.getLanguage().getLocale());

            return new ModelAndView("devices/password")
                    .addObject("action", MessageFormat.format("/devices/{0}/{1}/password", application.getName(), deviceGuid))
                    .addObject("password", credentials.getPassword())
                    .addObject("device", device)
                    .addObject("pubServerInfo", pubServerConfig)
                    .addObject("qrcode", base64QrCode.getResult())
                    .addObject("deviceDataURLs", serviceURLResponse.getResult());
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
                    applicationContext.getMessage(DeviceRegisterService.Messages.DEVICE_REGISTERED_SUCCESSFULLY.getCode(), null, locale));
            return new ModelAndView(MessageFormat.format("redirect:/devices/{0}/{1}",
            								serviceResponse.getResult().getApplication().getName(),
            								serviceResponse.getResult().getGuid()));
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
