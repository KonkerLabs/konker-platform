package com.konkerlabs.platform.registry.integration.processors;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.EnrichmentExecutor;
import com.konkerlabs.platform.registry.business.services.api.NewServiceResponse;
import com.konkerlabs.platform.registry.business.services.routes.api.EventRouteExecutor;
import com.konkerlabs.platform.registry.web.controllers.JedisClientBuilder;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DeviceEventProcessor {

    public enum Messages {
        APIKEY_MISSING("integration.event_processor.api_key.missing"),
        CHANNEL_MISSING("integration.event_processor.channel.missing"),
        DEVICE_NOT_FOUND("integration.event_processor.channel.not_found");

        private String code;

        public String getCode() {
            return code;
        }

        Messages(String code) {
            this.code = code;
        }
    }

    private static final String EVENT_DROPPED = "Incoming event has been dropped: [Device: {0}] - [Payload: {1}]";

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceEventProcessor.class);

    private EventRouteExecutor eventRouteExecutor;
    private DeviceRegisterService deviceRegisterService;
    private DeviceEventService deviceEventService;
    private EnrichmentExecutor enrichmentExecutor;
    private RedisConnectionFactory redisConnectionFactory;
    private ApplicationContext applicationContext;

    @Autowired
    public DeviceEventProcessor(DeviceEventService deviceEventService,
                                EventRouteExecutor eventRouteExecutor,
                                DeviceRegisterService deviceRegisterService,
                                EnrichmentExecutor enrichmentExecutor,
                                RedisConnectionFactory redisConnectionFactory,
                                ApplicationContext applicationContext) {
        this.deviceEventService = deviceEventService;
        this.eventRouteExecutor = eventRouteExecutor;
        this.deviceRegisterService = deviceRegisterService;
        this.enrichmentExecutor = enrichmentExecutor;
        this.redisConnectionFactory = redisConnectionFactory;
        this.applicationContext = applicationContext;
    }

    public void process(String apiKey, String channel, String payload) throws BusinessException {

        Optional.ofNullable(apiKey).filter(s -> !s.isEmpty())
                .orElseThrow(() -> new BusinessException(Messages.APIKEY_MISSING.getCode()));

        Optional.ofNullable(channel).filter(s -> !s.isEmpty())
                .orElseThrow(() -> new BusinessException(Messages.CHANNEL_MISSING.getCode()));

        Device device = Optional.ofNullable(deviceRegisterService.findByApiKey(apiKey))
                .orElseThrow(() -> new BusinessException(Messages.DEVICE_NOT_FOUND.getCode()));

        if (device.isActive()) {
            Event event = Event.builder()
                    .channel(channel)
                    .payload(payload)
                    .deviceGuid(device.getGuid())
                    .build();

            NewServiceResponse<Event> serviceResponse = enrichmentExecutor.enrich(event, device);
            switch (serviceResponse.getStatus()) {
                case ERROR: {
                    LOGGER.error(MessageFormat.format("Enrichment failed: [Device: {0}] - [Payload: {1}]", device.toURI(), payload));
                    break;
                }
                case OK: {
                    event.setPayload(serviceResponse.getResult().getPayload());
                    break;
                }
                default: break;
            }

            deviceEventService.logEvent(device, channel, event);

            eventRouteExecutor.execute(event,device.toURI());
        } else {
            LOGGER.debug(MessageFormat.format(EVENT_DROPPED,
                device.toURI(),
                payload));
        }
    }

    public void process(String apiKey, String channel, Instant offset, Long waitTime, DeferredResult<List<Event>> deferredResult) throws BusinessException {
    	Optional.ofNullable(apiKey).filter(s -> !s.isEmpty())
    			.orElseThrow(() -> new BusinessException(Messages.APIKEY_MISSING.getCode()));

    	Optional.ofNullable(channel).filter(s -> !s.isEmpty())
    			.orElseThrow(() -> new BusinessException(Messages.CHANNEL_MISSING.getCode()));

    	Device device = Optional.ofNullable(deviceRegisterService.findByApiKey(apiKey))
    			.orElseThrow(() -> new BusinessException(Messages.DEVICE_NOT_FOUND.getCode()));
    	
    	NewServiceResponse<List<Event>> response = deviceEventService.findEventsBy(device.getTenant(), device.getDeviceId(), 
    			offset, null, null);
    	
    	if (!response.getResult().isEmpty() || waitTime == null || (waitTime != null && waitTime.equals(new Long("0")))) {
    		deferredResult.setResult(response.getResult());
    	} else {
    		
    		CompletableFuture.runAsync(() ->  {
    			JedisClientBuilder.sub(deferredResult, apiKey, channel, offset);
//    			applicationContext.getBean(RedisMessageListenerContainer.class, redisConnectionFactory, apiKey+"."+channel, new MessageListener() {
//    				
//    				@Override
//    				public void onMessage(Message message, byte[] pattern) {
//    					deferredResult.setResult(new ArrayList<>());
//    				}
//    			});
    		});
    	}
    }
}
