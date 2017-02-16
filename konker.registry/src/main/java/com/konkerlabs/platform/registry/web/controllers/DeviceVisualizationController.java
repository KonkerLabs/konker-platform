package com.konkerlabs.platform.registry.web.controllers;

import java.io.IOException;
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
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Event.EventDecorator;
import com.konkerlabs.platform.registry.business.model.EventSchema;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import com.konkerlabs.platform.registry.business.services.api.EventSchemaService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.config.EnvironmentConfig;
import com.konkerlabs.platform.registry.web.converters.InstantToStringConverter;
import com.konkerlabs.platform.registry.web.csv.EventCsvDownload;

@Controller
@Scope("request")
@RequestMapping(value = "devices/visualization")
public class DeviceVisualizationController implements ApplicationContextAware {

	private static final Logger LOGGER = LoggerFactory.getLogger(DeviceVisualizationController.class);
	
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
    
    private DeviceEventService deviceEventService;
    private Tenant tenant;
    private ApplicationContext applicationContext;
    private EventSchemaService eventSchemaService;
    private User user;
    private InstantToStringConverter instantToStringConverter;
    private EnvironmentConfig environmentConfig;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Autowired
    public DeviceVisualizationController(DeviceEventService deviceEventService, Tenant tenant,
    		EventSchemaService eventSchemaService, User user, 
    		InstantToStringConverter instantToStringConverter, EnvironmentConfig environmentConfig) {
        this.deviceEventService = deviceEventService;
        this.tenant = tenant;
        this.eventSchemaService = eventSchemaService;
        this.user = user;
        this.instantToStringConverter = instantToStringConverter;
        this.environmentConfig = environmentConfig;
    }

	@RequestMapping(path = "/load/")
    @PreAuthorize("hasAuthority('VIEW_DEVICE_CHART')")
    @SuppressWarnings("rawtypes")
	public @ResponseBody List load(@RequestParam(required = false) String dateStart,
				    		@RequestParam(required = false) String dateEnd,
				    		@RequestParam(required = false) boolean online,
				    		@RequestParam String deviceGuid,
				    		@RequestParam String channel,
				    		Locale locale) {
    	
    	return doSearch(dateStart, dateEnd, online, deviceGuid, channel, locale, 100);
    	
    }

	@SuppressWarnings("rawtypes")
	private List doSearch(String dateStart, String dateEnd, boolean online, String deviceGuid,
			String channel, Locale locale, int limit) {
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
    	
    	if (!online && StringUtils.isEmpty(dateStart)) {
    		Map<String, String> message = new HashMap<>();
    		message.put("message", applicationContext.getMessage(Messages.DATESTART_IS_MANDATORY.getCode(),null,locale));
    		return Arrays.asList(message);
    	}
    	
    	if (!online && StringUtils.isEmpty(dateEnd)) {
    		Map<String, String> message = new HashMap<>();
    		message.put("message", applicationContext.getMessage(Messages.DATEEND_IS_MANDATORY.getCode(),null,locale));
    		return Arrays.asList(message);
    	}
    	
    	if (online) {
    		ServiceResponse<List<Event>> response = deviceEventService.findIncomingBy(tenant, deviceGuid, channel, null,
        			null, false, limit);
        	
    		List<EventDecorator> eventsResult = decorateEventResult(response);
    		return eventsResult;
    	}
    	
    	LocalDateTime start = LocalDateTime.parse(dateStart, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss", user.getLanguage().getLocale()));
    	LocalDateTime end = LocalDateTime.parse(dateEnd, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss",  user.getLanguage().getLocale()));
    	ZonedDateTime zonedDateStart = ZonedDateTime.of(start, ZoneId.of(user.getZoneId().getId()));
    	ZonedDateTime zonedDateEnd = ZonedDateTime.of(end, ZoneId.of(user.getZoneId().getId()));
    	
    	ServiceResponse<List<Event>> response = deviceEventService.findIncomingBy(tenant,
				deviceGuid, channel, zonedDateStart.toInstant(),
    			zonedDateEnd.toInstant(), false, limit);
    	
    	List<EventDecorator> eventsResult = decorateEventResult(response);
		return eventsResult;
	}

	private List<EventDecorator> decorateEventResult(ServiceResponse<List<Event>> response) {
		List<EventDecorator> eventsResult = new ArrayList<>();
		response.getResult().forEach(r -> eventsResult.add(EventDecorator.builder()
				.timestampFormated(instantToStringConverter.convert(r.getTimestamp()))
				.timestamp(r.getTimestamp().toEpochMilli())
				.incoming(r.getIncoming())
				.payload(r.getPayload())
				.build()));
		return eventsResult;
	}
    
    @RequestMapping("/loading/channel/")
    @PreAuthorize("hasAuthority('VIEW_DEVICE_CHART')")
    public ModelAndView loadChannels(@RequestParam String deviceGuid) {
    	ServiceResponse<List<String>> channels = eventSchemaService.findKnownIncomingChannelsBy(tenant, deviceGuid);
    	
    	return new ModelAndView("devices/visualization/channels", "channels", channels.getResult());
    }
    
    @RequestMapping("/loading/metrics/")
    @PreAuthorize("hasAuthority('VIEW_DEVICE_CHART')")
    public ModelAndView loadMetrics(@RequestParam String deviceGuid, 
    								@RequestParam String channel) {
    	ServiceResponse<List<String>> metricsResponse = eventSchemaService.findKnownIncomingMetricsBy(tenant, deviceGuid, channel, JsonNodeType.NUMBER);
    	
    	if (metricsResponse.getResult() == null) {
    		return new ModelAndView("devices/visualization/metrics", "metrics", new ArrayList<>());
    	}
    	
    	String defaultMetric = CollectionUtils.isEmpty(metricsResponse.getResult()) ? null : metricsResponse.getResult().get(0);
    	
    	return new ModelAndView("devices/visualization/metrics", "metrics", metricsResponse.getResult())
    			.addObject("defaultMetric", defaultMetric);
    }
    
    @RequestMapping(path = "/csv/download")
    @PreAuthorize("hasAuthority('EXPORT_DEVICE_CSV')")
    public void download(@RequestParam(required = false) String dateStart,
			    		 @RequestParam(required = false) String dateEnd,
			    		 @RequestParam(required = false) boolean online,
			    		 @RequestParam String deviceGuid,
			    		 @RequestParam String channel,
    					 Locale locale, HttpServletResponse response) {
    	
    	try  {
			ServiceResponse<EventSchema> metrics = eventSchemaService.findIncomingBy(tenant, deviceGuid, channel);

            List<String> additionalHeaders = new ArrayList<String>();
            if (metrics.isOk()) {
                additionalHeaders = metrics.getResult().getFields().stream()
                        .map(m -> m.getPath())
                        .collect(Collectors.toList());
            }

            int limit = environmentConfig.getCsvDownloadRowsLimit();
            List events = doSearch(dateStart, dateEnd, online, deviceGuid, channel, locale, limit);
    		
    		EventCsvDownload csvDownload = new EventCsvDownload();
			csvDownload.download(events, response, additionalHeaders);
		} catch (IOException | SecurityException | NoSuchMethodException e) {
			LOGGER.error("Error to generate CSV", 
						Device.builder().guid(deviceGuid).build().toURI(),
						Device.builder().guid(deviceGuid).build().getLogLevel(), 
						e);
		}
    }

}
