package com.konkerlabs.platform.registry.web.controllers;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.node.JsonNodeType;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.EventSchema;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.EventSchemaService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.web.forms.DeviceVisualizationForm;

@Controller
@Scope("request")
@RequestMapping(value = "visualization")
public class DeviceVisualizationController implements ApplicationContextAware {

    public enum Messages {
    	DEVICE_IS_MANDATORY("visualization.device.mandatory"),
    	CHANNE_IS_MANDATORY("visualization.channel.mandatory"),
    	DATESTART_IS_MANDATORY("visualization.datestart.mandatory"),
    	DATEEND_IS_MANDATORY("visualization.dateend.mandatory");

        public String getCode() {
            return code;
        }

        private String code;

        Messages(String code) {
            this.code = code;
        }
    }
    
    private DeviceRegisterService deviceRegisterService;
    private DeviceEventService deviceEventService;
    private Tenant tenant;
    private ApplicationContext applicationContext;
    private EventSchemaService eventSchemaService;
    private User user;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Autowired
    public DeviceVisualizationController(DeviceRegisterService deviceRegisterService, DeviceEventService deviceEventService, Tenant tenant,
    		EventSchemaService eventSchemaService, User user) {
        this.deviceRegisterService = deviceRegisterService;
        this.deviceEventService = deviceEventService;
        this.tenant = tenant;
        this.eventSchemaService = eventSchemaService;
        this.user = user;
    }

    @RequestMapping
    public ModelAndView index() {
        List<Device> all = deviceRegisterService.findAll(tenant).getResult();
        DeviceVisualizationForm deviceVisualizationForm = new DeviceVisualizationForm();
		return new ModelAndView("visualization/index", "devices", all).addObject("visualization", deviceVisualizationForm);
    }
    
    @RequestMapping(path = "/load/")
    public @ResponseBody List load(@RequestParam(required = false) String dateStart,
				    		@RequestParam(required = false) String dateEnd,
				    		@RequestParam(required = false) boolean online,
				    		@RequestParam String deviceGuid,
				    		@RequestParam String channel,
				    		Locale locale) {
    	
    	if (deviceGuid.isEmpty()) {
    		Map<String, String> message = new HashMap<>();
    		message.put("message", applicationContext.getMessage(Messages.DEVICE_IS_MANDATORY.getCode(),null,locale));
    		return Arrays.asList(message);
    	}
    	
    	if (channel.isEmpty()) {
    		Map<String, String> message = new HashMap<>();
    		message.put("message", applicationContext.getMessage(Messages.CHANNE_IS_MANDATORY.getCode(),null,locale));
    		return Arrays.asList(message);
    	}
    	
    	if (!online && dateStart.isEmpty()) {
    		Map<String, String> message = new HashMap<>();
    		message.put("message", applicationContext.getMessage(Messages.DATESTART_IS_MANDATORY.getCode(),null,locale));
    		return Arrays.asList(message);
    	}
    	
    	if (!online && dateEnd.isEmpty()) {
    		Map<String, String> message = new HashMap<>();
    		message.put("message", applicationContext.getMessage(Messages.DATEEND_IS_MANDATORY.getCode(),null,locale));
    		return Arrays.asList(message);
    	}
    	
    	if (online) {
    		ServiceResponse<List<Event>> response = deviceEventService.findIncomingBy(tenant, deviceGuid, channel, null,
        			null, false, 100);
        	
    		return response.getResult();
    	}
    	
    	LocalDateTime start = LocalDateTime.parse(dateStart, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    	LocalDateTime end = LocalDateTime.parse(dateEnd, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    	ZonedDateTime zonedDateStart = ZonedDateTime.of(start, ZoneId.of(user.getZoneId()));
    	ZonedDateTime zonedDateEnd = ZonedDateTime.of(end, ZoneId.of(user.getZoneId()));
    	
    	ServiceResponse<List<Event>> response = deviceEventService.findIncomingBy(tenant,
				deviceGuid, channel, zonedDateStart.toInstant(),
    			zonedDateEnd.toInstant(), false, 100);
    	
    	
		return response.getResult();
    	
    }
    
    @RequestMapping("/loading/channel/")
    public ModelAndView loadChannels(@RequestParam String deviceGuid) {
    	ServiceResponse<List<String>> channels = eventSchemaService.findKnownIncomingChannelsBy(tenant, deviceGuid);
    	
    	return new ModelAndView("visualization/channels", "channels", channels.getResult());
    }
    
    @RequestMapping("/loading/metrics/")
    public ModelAndView loadMetrics(@RequestParam String deviceGuid, 
    								@RequestParam String channel) {
    	ServiceResponse<EventSchema> metrics = eventSchemaService.findIncomingBy(deviceGuid, channel);
    	
    	if (metrics.getResult() == null) {
    		return new ModelAndView("visualization/metrics", "metrics", new ArrayList<>());
    	}
    	
    	List<String> listMetrics = metrics.getResult()
				.getFields().stream()
				.filter(schemaField -> schemaField.getKnownTypes().contains(JsonNodeType.NUMBER))
				.map(m -> m.getPath()).collect(Collectors.toList());
    	return new ModelAndView("visualization/metrics", "metrics", listMetrics);
    }
}
