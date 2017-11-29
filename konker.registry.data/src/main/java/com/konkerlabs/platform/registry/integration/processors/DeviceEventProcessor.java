package com.konkerlabs.platform.registry.integration.processors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.DeviceModel;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Gateway;
import com.konkerlabs.platform.registry.business.services.LocationTreeUtils;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.data.services.api.DeviceLogEventService;
import com.konkerlabs.platform.registry.data.services.routes.api.EventRouteExecutor;
import com.konkerlabs.platform.registry.integration.converters.JsonConverter;
import com.konkerlabs.platform.utilities.parsers.json.JsonParsingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DeviceEventProcessor {

    public enum Messages {
        APIKEY_MISSING("integration.event_processor.api_key.missing"),
        CHANNEL_MISSING("integration.event_processor.channel.missing"),
        DEVICE_NOT_FOUND("integration.event_processor.channel.not_found"),
        INVALID_GATEWAY_LOCATION("integration.event_processor.gateway.location.invalid"),
        INVALID_PAYLOAD("integration.event_processor.payload.invalid");

        private String code;

        public String getCode() {
            return code;
        }

        Messages(String code) {
            this.code = code;
        }
    }

    private static final String EVENT_DROPPED = "Incoming event has been dropped: [Device: {0}] - [Payload: {1}]";
    private static final String GATEWAY_EVENT_DROPPED = "Incoming event has been dropped: [Gateway: {0}] - [Payload: {1}]";
    private Pattern integerPattern = Pattern.compile("^[0-9]*$") ;

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceEventProcessor.class);

    private EventRouteExecutor eventRouteExecutor;
    private DeviceRegisterService deviceRegisterService;
    private DeviceLogEventService deviceLogEventService;
    private JsonParsingService jsonParsingService;
    private BeanFactory beans;

    @Autowired
    public DeviceEventProcessor(DeviceLogEventService deviceLogEventService,
                                EventRouteExecutor eventRouteExecutor,
                                DeviceRegisterService deviceRegisterService,
                                JsonParsingService jsonParsingService,
                                BeanFactory beans) {
        this.deviceLogEventService = deviceLogEventService;
        this.eventRouteExecutor = eventRouteExecutor;
        this.deviceRegisterService = deviceRegisterService;
        this.jsonParsingService = jsonParsingService;
        this.beans = beans;
    }

    public void process(String apiKey, String channel, String payload) throws BusinessException {
        process(apiKey, channel,  payload, Instant.now());
    }

    /**
     * Processes a data bytes content. The bytes will be converted to a JSON according to the device model.
     *
     * @param apiKey
     * @param channel
     * @param bytesPayload
     * @param timestamp
     * @throws BusinessException
     */
    public void process(String apiKey, String channel, byte bytesPayload[], Instant timestamp) throws BusinessException {
        Optional.ofNullable(apiKey).filter(s -> !s.isEmpty())
                .orElseThrow(() -> new BusinessException(Messages.APIKEY_MISSING.getCode()));

        Device device = Optional.ofNullable(deviceRegisterService.findByApiKey(apiKey))
                .orElseThrow(() -> new BusinessException(Messages.DEVICE_NOT_FOUND.getCode()));

        String jsonPayload = getJsonPayload(device, bytesPayload);

        process(device, channel, jsonPayload, timestamp, timestamp);
    }

    /**
     * Processes an event data (with JSON format)
     *
     * @param apiKey
     * @param channel
     * @param jsonPayload
     * @param timestamp
     * @throws BusinessException
     */
    public void process(String apiKey, String channel, String jsonPayload, Instant timestamp) throws BusinessException {
        Optional.ofNullable(apiKey).filter(s -> !s.isEmpty())
                .orElseThrow(() -> new BusinessException(Messages.APIKEY_MISSING.getCode()));

        Device device = Optional.ofNullable(deviceRegisterService.findByApiKey(apiKey))
                .orElseThrow(() -> new BusinessException(Messages.DEVICE_NOT_FOUND.getCode()));
        
        process(device, channel, jsonPayload, timestamp, timestamp);
    }
    
    private Boolean isValidAuthority(Gateway gateway, Device device) throws BusinessException {
        return LocationTreeUtils.isSublocationOf(gateway.getLocation(), device.getLocation());
    }
    
    @SuppressWarnings("unchecked")
	public void proccess(Gateway gateway, String payloadList) throws BusinessException, JsonProcessingException {
    	List<Map<String, Object>> payloadsGateway = jsonParsingService.toListMap(payloadList);
    	
    	for (Map<String, Object> payloadGateway : payloadsGateway) {
    		ServiceResponse<Device> result = deviceRegisterService.findByDeviceId(
    				gateway.getTenant(), 
    				gateway.getApplication(), 
    				payloadGateway.get("deviceId").toString());
    		
    		if (result.isOk() && Optional.ofNullable(result.getResult()).isPresent()) {
    			Device device = result.getResult();
    			
    			if (isValidAuthority(gateway, device)) {
    				Map<String, Object> devicePayload = (Map<String, Object>) payloadGateway.get("payload");
    				
    				Instant ingestedTimestamp = Instant.now();
					Instant creationTimestamp = payloadGateway.containsKey("ts") 
    						&& integerPattern.matcher(payloadGateway.get("ts").toString()).matches() ? 
    								Instant.ofEpochMilli(new Long(payloadGateway.get("ts").toString())) : 
    								ingestedTimestamp;
    				
    				process(
    						device, 
    						payloadGateway.get("channel").toString(), 
    						jsonParsingService.toJsonString(devicePayload),
    						ingestedTimestamp,
    						creationTimestamp);
    			} else {
    				LOGGER.debug(MessageFormat.format(Messages.INVALID_GATEWAY_LOCATION.getCode(),
                            gateway.toURI(),
                            payloadList),
                    		gateway.toURI(),
                    		gateway.getTenant().getLogLevel());
                }
    		} else {
                LOGGER.debug(MessageFormat.format(GATEWAY_EVENT_DROPPED,
                        gateway.toURI(),
                        payloadList),
                		gateway.toURI(),
                		gateway.getTenant().getLogLevel());
            }
    		
		}
    	
    }
    
    public void process(Device device, String channel, String jsonPayload, Instant ingestedTimestamp, Instant creationTimestamp) throws BusinessException {

        Optional.ofNullable(channel).filter(s -> !s.isEmpty())
                .orElseThrow(() -> new BusinessException(Messages.CHANNEL_MISSING.getCode()));

        Event event = Event.builder()
                .incoming(
                        Event.EventActor.builder()
                                .deviceGuid(device.getGuid())
                                .channel(channel)
                                .deviceId(device.getDeviceId())
                                .tenantDomain(Optional.ofNullable(device.getTenant()).isPresent()
                                        ? device.getTenant().getDomainName() : null)
                                .applicationName(Optional.ofNullable(device.getApplication()).isPresent()
                                        ? device.getApplication().getName(): null)
                                .build()
                )
                .creationTimestamp(creationTimestamp)
                .ingestedTimestamp(ingestedTimestamp)
                .payload(jsonPayload)
                .build();
        if (device.isActive()) {

            ServiceResponse<Event> logResponse = deviceLogEventService.logIncomingEvent(device, event);
            if (logResponse.isOk()) {
                eventRouteExecutor.execute(event, device);
            } else {
                LOGGER.error(MessageFormat.format("Could not log incoming message. Probably invalid payload.: [Device: {0}] - [Payload: {1}]",
                        device.toURI(),
                        jsonPayload),
                		event.getIncoming().toURI(),
                		device.getLogLevel()
                );
                throw new BusinessException(Messages.INVALID_PAYLOAD.getCode());
            }

        } else {
            LOGGER.debug(MessageFormat.format(EVENT_DROPPED,
                    device.toURI(),
                    jsonPayload),
            		event.getIncoming().toURI(),
            		device.getLogLevel());
        }

    }

    private String getJsonPayload(Device device, byte[] payloadBytes) throws BusinessException {

        DeviceModel.ContentType contentType = device.getDeviceModel().getContentType();
        if (contentType == null) {
            contentType = DeviceModel.ContentType.APPLICATION_JSON;
        }

        JsonConverter jsonConverter = BeanFactoryAnnotationUtils.qualifiedBeanOfType(beans, JsonConverter.class, contentType.getValue());
        ServiceResponse<String> jsonConverterResponse = jsonConverter.toJson(payloadBytes);

        if (jsonConverterResponse.isOk()) {
            return jsonConverterResponse.getResult();
        } else {
            throw new BusinessException(Messages.INVALID_PAYLOAD.getCode());
        }

    }

}
