package com.konkerlabs.platform.registry.data.services;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Event.EventGeolocation;
import com.konkerlabs.platform.registry.business.model.EventSchema;
import com.konkerlabs.platform.registry.business.repositories.events.api.EventRepository;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService.Validations;
import com.konkerlabs.platform.registry.business.services.api.EventSchemaService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.config.EventStorageConfig;
import com.konkerlabs.platform.registry.data.services.api.DeviceLogEventService;
import com.konkerlabs.platform.registry.type.EventStorageConfigType;
import com.konkerlabs.platform.utilities.parsers.json.JsonParsingService;
import com.konkerlabs.platform.utilities.parsers.json.JsonParsingService.JsonPathData;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DeviceLogEventServiceImpl implements DeviceLogEventService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DeviceLogEventServiceImpl.class);

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private EventStorageConfig eventStorageConfig;
    private EventRepository eventRepository;
    @Autowired
    private EventSchemaService eventSchemaService;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private JedisTaskService jedisTaskService;
    @Autowired
    private JsonParsingService jsonParsingService;
    
    private Pattern geoPattern = Pattern.compile("^([-+]?\\d{1,3}[.]\\d+)");
    private static final String EVENT_GEO_INVALID = "Incoming event has invalid geolocation data: [Field: {0}] - [Value: {1}]";

    @PostConstruct
    public void init() {
        try {
            eventRepository =
                    (EventRepository) applicationContext.getBean(
                            eventStorageConfig.getEventRepositoryBean()
                    );
        } catch (Exception e) {
            eventRepository =
                    (EventRepository) applicationContext.getBean(
                            EventStorageConfigType.MONGODB.bean()
                    );
        }
    }

    @Override
    public ServiceResponse<Event> logIncomingEvent(Device device, Event event) {
        return doLog(device,event,() -> {
            try {
                ServiceResponse<EventSchema> schemaResponse = eventSchemaService.appendIncomingSchema(event);

                jedisTaskService.registerLastEventTimestamp(event);
                appendEventGeolocation(event, device);

                if (schemaResponse.isOk()) {
                    return ServiceResponseBuilder.<Event>ok()
                            .withResult(eventRepository.saveIncoming(device.getTenant(), device.getApplication(), event)).build();
                } else {
                    return ServiceResponseBuilder.<Event>error()
                        .withMessages(schemaResponse.getResponseMessages()).build();
                }

            } catch (BusinessException e) {
                return ServiceResponseBuilder.<Event>error()
                        .withMessage(e.getMessage()).build();
            }
        });
    }

    private void appendEventGeolocation(Event event, Device device)  {
    	try {
			Map<String, JsonParsingService.JsonPathData> data = jsonParsingService.toFlatMap(event.getPayload());

					
			if (isValidGeolocation(data, device)) {
				EventGeolocation geolocation = EventGeolocation.builder()
					.lat(parseLat(data))
					.lon(parseLon(data))
					.hdop(parseHdop(data, device))
					.elev(parseElev(data, device))
					.build();
				
				event.setGeolocation(geolocation);
			}
			
		} catch (JsonProcessingException e) {
			LOGGER.error("Error to append event geolocation", e);
		}
    	
	}

	private Double parseLat(Map<String, JsonPathData> data) {
		return (Double) data.get("_lat").getValue();
	}
	
	private Double parseLon(Map<String, JsonPathData> data) {
		return (Double) data.get("_lon").getValue();
	}
	
	private Long parseHdop(Map<String, JsonPathData> data, Device device) {
		Object hdopObj = data.containsKey("_hdop") ? data.get("_hdop").getValue() : null;
		
		if (hdopObj instanceof String) {
			LOGGER.warn(MessageFormat.format(EVENT_GEO_INVALID, "_hdop", hdopObj.toString()),
					device.toURI(),
            		device.getLogLevel());
			return null;
		}
		
		return (Long) Optional.ofNullable(hdopObj).orElse(null);
	}
	
	private Double parseElev(Map<String, JsonPathData> data, Device device) {
		Object elevObj = data.containsKey("_elev") ? data.get("_elev").getValue() : null;
		
		if (elevObj instanceof String) {
			LOGGER.warn(MessageFormat.format(EVENT_GEO_INVALID, "_elev", elevObj.toString()),
					device.toURI(),
            		device.getLogLevel());
			return null;
		}
		
		return (Double) Optional.ofNullable(elevObj).orElse(null);
	}

	private boolean isValidGeolocation(Map<String, JsonParsingService.JsonPathData> data, Device device) {
		Object latObj = data.containsKey("_lat") ? data.get("_lat").getValue() : null;
		Object lonObj = data.containsKey("_lon") ? data.get("_lon").getValue() : null;
		
		if (latObj instanceof String) {
			LOGGER.warn(MessageFormat.format(EVENT_GEO_INVALID, "_lat", latObj.toString()),
					device.toURI(),
            		device.getLogLevel());
			return false;
		} 
		
		if (lonObj instanceof String) {
			LOGGER.warn(MessageFormat.format(EVENT_GEO_INVALID, "_lon", lonObj.toString()),
					device.toURI(),
            		device.getLogLevel());
			return false;
		}
		
		if (latObj == null || lonObj == null) {
			return false;
		}
		
		if (geoPattern.matcher(latObj.toString()).matches() 
				&& geoPattern.matcher(lonObj.toString()).matches()) {
			return true;
		}
		
		return false;
	}

	@Override
    public ServiceResponse<Event> logOutgoingEvent(Device device, Event event) {
        return doLog(device,event,() -> {
            try {
                Event saved = eventRepository.saveOutgoing(device.getTenant(), device.getApplication(), event);
                redisTemplate.convertAndSend(
                        device.getApiKey(),
                        device.getGuid());

                redisTemplate.convertAndSend(
                        device.getApiKey() + "." + event.getOutgoing().getChannel(),
                        device.getGuid());

                return ServiceResponseBuilder.<Event>ok().withResult(saved).build();
            } catch (BusinessException e) {
                return ServiceResponseBuilder.<Event>error()
                        .withMessage(e.getMessage()).build();
            }
        });
    }

    private ServiceResponse<Event> doLog(Device device, Event event, Supplier<ServiceResponse<Event>> callable) {
        if (!Optional.ofNullable(device).isPresent())
            return ServiceResponseBuilder.<Event>error()
                    .withMessage(Validations.DEVICE_NULL.getCode()).build();
        if (!Optional.ofNullable(event).isPresent())
            return ServiceResponseBuilder.<Event>error()
                    .withMessage(Validations.EVENT_NULL.getCode()).build();
        if (!Optional.ofNullable(event.getPayload()).filter(s -> !s.isEmpty()).isPresent())
            return ServiceResponseBuilder.<Event>error()
                    .withMessage(Validations.EVENT_PAYLOAD_NULL.getCode()).build();

        if (!Optional.ofNullable(event.getTimestamp()).isPresent())
            event.setTimestamp(Instant.now());

        return callable.get();
    }


}
