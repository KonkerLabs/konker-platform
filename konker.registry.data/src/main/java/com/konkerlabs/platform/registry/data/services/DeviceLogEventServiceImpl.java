package com.konkerlabs.platform.registry.data.services;

import java.text.MessageFormat;
import java.time.Duration;
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
    
    private final Pattern integerPattern = Pattern.compile("^[0-9]*$") ;
    private final Pattern decimalPattern = Pattern.compile("^[-+]?[0-9]*[.][0-9]*$") ;
    private static final String EVENT_GEO_INVALID = "Incoming event has invalid geolocation data: [Field: {0}] - [Value: {1}]";
    private static final String EVENT_TIME_INVALID = "Incoming event has invalid timestamp data: [Field: {0}] - [Value: {1}]";

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
                appendCreationTimestamp(event, device);

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

	private void appendCreationTimestamp(Event event, Device device) {
		try {
			Map<String, JsonPathData> data = jsonParsingService.toFlatMap(event.getPayload());
			
			if (!data.containsKey("_ts")) {
				return;			
			}
			
			Instant tomorrow = Instant.now().plus(Duration.ofDays(1L));
			Instant aYearAgo = Instant.now().minus(Duration.ofDays(365L));
			Instant creationTime = integerPattern.matcher(data.get("_ts").getValue().toString()).matches() 
									? Instant.ofEpochMilli(new Long(data.get("_ts").getValue().toString())) 
									: null;
							
			if (Optional.ofNullable(creationTime).isPresent() && 
					creationTime.isAfter(aYearAgo) && 
					creationTime.isBefore(tomorrow)) {
				event.setCreationTimestamp(creationTime);
				
			} else {
				event.setCreationTimestamp(event.getIngestedTimestamp());
				LOGGER.warn(MessageFormat.format(EVENT_TIME_INVALID, "_ts", data.get("_ts").getValue().toString()),
						device.toURI(),
						device.getLogLevel());
			}
			
		} catch (JsonProcessingException e) {
			LOGGER.error("Error to append event creation time", e);
		}
	}

	private Double parseLat(Map<String, JsonPathData> data) {
		return new Double(data.get("_lat").getValue().toString());
	}
	
	private Double parseLon(Map<String, JsonPathData> data) {
		return new Double(data.get("_lon").getValue().toString());
	}
	
	private Long parseHdop(Map<String, JsonPathData> data, Device device) {
		if (!data.containsKey("_hdop")) {
			
			return null;
		} else if (data.containsKey("_hdop") 
				&& integerPattern.matcher(data.get("_hdop").getValue().toString()).matches()) {
			
			return new Long(data.get("_hdop").getValue().toString());
		} else {
			LOGGER.warn(MessageFormat.format(EVENT_GEO_INVALID, "_hdop", data.get("_hdop").getValue()),
					device.toURI(),
					device.getLogLevel());
			
			return null;
		}
		
	}
	
	private Double parseElev(Map<String, JsonPathData> data, Device device) {
		if (!data.containsKey("_elev")) {
			
			return null;
		} else if (data.containsKey("_elev") 
				&& decimalPattern.matcher(data.get("_elev").getValue().toString()).matches()) {
			
			return new Double(data.get("_elev").getValue().toString());
		} else {
			LOGGER.warn(MessageFormat.format(EVENT_GEO_INVALID, "_elev", data.get("_elev").getValue()),
					device.toURI(),
					device.getLogLevel());
			
			return null;
		}
	}

	private boolean isValidGeolocation(Map<String, JsonParsingService.JsonPathData> data, Device device) {
		if (!data.containsKey("_lat") || !data.containsKey("_lon")) {
			return false;
		}
		
		Object latObj = data.get("_lat").getValue();
		Object lonObj = data.get("_lon").getValue();
		
		if (!decimalPattern.matcher(latObj.toString()).matches()) {
			LOGGER.warn(MessageFormat.format(EVENT_GEO_INVALID, "_lat", latObj.toString()),
					device.toURI(),
            		device.getLogLevel());
			return false;
		} 
		
		if (!decimalPattern.matcher(lonObj.toString()).matches()) {
			LOGGER.warn(MessageFormat.format(EVENT_GEO_INVALID, "_lon", lonObj.toString()),
					device.toURI(),
            		device.getLogLevel());
			return false;
		}
		
		Double lat = new Double(latObj.toString());
		Double lon = new Double(lonObj.toString());
		
		if (lat < -90.0 ||  lat > 90.0) {
			LOGGER.warn(MessageFormat.format(EVENT_GEO_INVALID, "_lat", lat),
					device.toURI(),
            		device.getLogLevel());
			return false;
		}
		if (lon < -180.0 || lon > 180.0) {
			LOGGER.warn(MessageFormat.format(EVENT_GEO_INVALID, "_lon", lon),
					device.toURI(),
            		device.getLogLevel());
			return false;
		}
		
		return true;
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
                        device.getApiKey() + '.' + event.getOutgoing().getChannel(),
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

        if (!Optional.ofNullable(event.getCreationTimestamp()).isPresent())
            event.setCreationTimestamp(Instant.now());

        return callable.get();
    }


}
