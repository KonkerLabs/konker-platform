package com.konkerlabs.platform.registry.web.controllers;

import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.model.Event.EventDecorator;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.EventSchemaService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.config.EnvironmentConfig;
import com.konkerlabs.platform.registry.web.controllers.DeviceController.MetricVO;
import com.konkerlabs.platform.registry.web.converters.InstantToStringConverter;
import com.konkerlabs.platform.registry.web.csv.EventCsvDownload;
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
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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
    private DeviceRegisterService deviceRegisterService;
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
    public DeviceVisualizationController(
            DeviceEventService deviceEventService,
            DeviceRegisterService deviceRegisterService,
            Tenant tenant,
    		EventSchemaService eventSchemaService, User user,
    		InstantToStringConverter instantToStringConverter, EnvironmentConfig environmentConfig) {
        this.deviceEventService = deviceEventService;
        this.deviceRegisterService = deviceRegisterService;
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

        Device device = deviceRegisterService.findByTenantDomainNameAndDeviceGuid(tenant.getDomainName(), deviceGuid);
        Application application = device.getApplication();

    	if (online) {
    		ServiceResponse<List<Event>> response = deviceEventService.findIncomingBy(tenant, application, deviceGuid, null, channel,
                    null, null, false, limit);

    		List<EventDecorator> eventsResult = decorateEventResult(response);
    		return eventsResult;
    	}

    	LocalDateTime start = LocalDateTime.parse(dateStart, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss", user.getLanguage().getLocale()));
    	LocalDateTime end = LocalDateTime.parse(dateEnd, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss",  user.getLanguage().getLocale()));
    	ZonedDateTime zonedDateStart = ZonedDateTime.of(start, ZoneId.of(user.getZoneId().getId()));
    	ZonedDateTime zonedDateEnd = ZonedDateTime.of(end, ZoneId.of(user.getZoneId().getId()));

    	ServiceResponse<List<Event>> response = deviceEventService.findIncomingBy(tenant, application,
				deviceGuid, null, channel,
                zonedDateStart.toInstant(), zonedDateEnd.toInstant(), false, limit);

    	List<EventDecorator> eventsResult = decorateEventResult(response);
		return eventsResult;
	}

	private List<EventDecorator> decorateEventResult(ServiceResponse<List<Event>> response) {
		List<EventDecorator> eventsResult = new ArrayList<>();
		response.getResult().forEach(r -> eventsResult.add(EventDecorator.builder()
				.timestampFormated(instantToStringConverter.convert(r.getCreationTimestamp()))
				.timestamp(r.getCreationTimestamp().toEpochMilli())
				.incoming(r.getIncoming())
				.payload(r.getPayload())
				.build()));
		return eventsResult;
	}

    @RequestMapping("/loading/channel/")
    @PreAuthorize("hasAuthority('VIEW_DEVICE_CHART')")
    public ModelAndView loadChannels(@RequestParam String deviceGuid) {
    	ServiceResponse<List<String>> channels = eventSchemaService.findKnownIncomingChannelsBy(tenant, null, deviceGuid);

    	return new ModelAndView("devices/visualization/channels", "channels", channels.getResult());
    }

    @RequestMapping("/loading/metrics/")
    @PreAuthorize("hasAuthority('VIEW_DEVICE_CHART')")
    public ModelAndView loadMetrics(@RequestParam String deviceGuid,
    								@RequestParam String channel) {

        Device device = deviceRegisterService.findByTenantDomainNameAndDeviceGuid(tenant.getDomainName(), deviceGuid);
        Application application = device.getApplication();

        ServiceResponse<List<String>> metricsResponse = eventSchemaService.findKnownIncomingMetricsBy(tenant, application, deviceGuid, channel, JsonNodeType.NUMBER);

        if (metricsResponse.getResult() == null) {
            return new ModelAndView("devices/visualization/metrics", "metrics", new ArrayList<>());
        }

        String defaultMetric = CollectionUtils.isEmpty(metricsResponse.getResult()) ? null : metricsResponse.getResult().get(0);

        List<MetricVO> metrics = new ArrayList<MetricVO>();
        for (String metricName : metricsResponse.getResult()) {
            MetricVO metricVO = new MetricVO(metricName);
            if (metricVO.getName().equals(defaultMetric)) {
                metricVO.setDefault();
            }
            metrics.add(metricVO);
        }

        return new ModelAndView("devices/visualization/metrics", "metrics", metrics);
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
            Device device = deviceRegisterService.findByTenantDomainNameAndDeviceGuid(tenant.getDomainName(), deviceGuid);
            Application application = device.getApplication();

            ServiceResponse<EventSchema> metrics = eventSchemaService.findIncomingBy(tenant, application, deviceGuid, channel);

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
