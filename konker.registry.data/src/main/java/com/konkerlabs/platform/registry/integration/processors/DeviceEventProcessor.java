package com.konkerlabs.platform.registry.integration.processors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Gateway;
import com.konkerlabs.platform.registry.business.services.LocationTreeUtils;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.data.services.api.DeviceLogEventService;
import com.konkerlabs.platform.registry.data.services.routes.api.EventRouteExecutor;
import com.konkerlabs.platform.utilities.parsers.json.JsonParsingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceEventProcessor.class);

    private EventRouteExecutor eventRouteExecutor;
    private DeviceRegisterService deviceRegisterService;
    private DeviceLogEventService deviceLogEventService;
    private JsonParsingService jsonParsingService;

    @Autowired
    public DeviceEventProcessor(DeviceLogEventService deviceLogEventService,
                                EventRouteExecutor eventRouteExecutor,
                                DeviceRegisterService deviceRegisterService,
                                JsonParsingService jsonParsingService) {
        this.deviceLogEventService = deviceLogEventService;
        this.eventRouteExecutor = eventRouteExecutor;
        this.deviceRegisterService = deviceRegisterService;
        this.jsonParsingService = jsonParsingService;
    }

    public void process(String apiKey, String channel, String payload) throws BusinessException {
        process(apiKey, channel,  payload, Instant.now());
    }
    
    public void process(String apiKey, String channel, String payload, Instant timestamp) throws BusinessException {
        Optional.ofNullable(apiKey).filter(s -> !s.isEmpty())
                .orElseThrow(() -> new BusinessException(Messages.APIKEY_MISSING.getCode()));

        Device device = Optional.ofNullable(deviceRegisterService.findByApiKey(apiKey))
                .orElseThrow(() -> new BusinessException(Messages.DEVICE_NOT_FOUND.getCode()));
        
        process(device, channel, payload, timestamp);
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
    				devicePayload.putIfAbsent("_ts", payloadGateway.get("ts"));
    				
    				process(
    						device, 
    						payloadGateway.get("channel").toString(), 
    						jsonParsingService.toJsonString(devicePayload), 
    						Instant.now());
    			} else {
    			    throw new BusinessException(Messages.INVALID_GATEWAY_LOCATION.getCode());
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
    
    public void process(Device device, String channel, String payload, Instant timestamp) throws BusinessException {
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
                .creationTimestamp(timestamp)
                .ingestedTimestamp(timestamp)
                .payload(payload)
                .build();
        if (device.isActive()) {

            ServiceResponse<Event> logResponse = deviceLogEventService.logIncomingEvent(device, event);
            if (logResponse.isOk()) {
                eventRouteExecutor.execute(event, device);
            } else {
                LOGGER.error(MessageFormat.format("Could not log incoming message. Probably invalid payload.: [Device: {0}] - [Payload: {1}]",
                        device.toURI(),
                        payload),
                		event.getIncoming().toURI(),
                		device.getLogLevel()
                );
                throw new BusinessException(Messages.INVALID_PAYLOAD.getCode());
            }

        } else {
            LOGGER.debug(MessageFormat.format(EVENT_DROPPED,
                    device.toURI(),
                    payload),
            		event.getIncoming().toURI(),
            		device.getLogLevel());
        }

    }
}
