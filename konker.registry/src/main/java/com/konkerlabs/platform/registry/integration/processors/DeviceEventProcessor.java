package com.konkerlabs.platform.registry.integration.processors;

import java.text.MessageFormat;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.EnrichmentExecutor;
import com.konkerlabs.platform.registry.business.services.api.NewServiceResponse;
import com.konkerlabs.platform.registry.business.services.routes.api.EventRouteExecutor;

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

    @Autowired
    public DeviceEventProcessor(DeviceEventService deviceEventService,
                                EventRouteExecutor eventRouteExecutor,
                                DeviceRegisterService deviceRegisterService,
                                EnrichmentExecutor enrichmentExecutor) {
        this.deviceEventService = deviceEventService;
        this.eventRouteExecutor = eventRouteExecutor;
        this.deviceRegisterService = deviceRegisterService;
        this.enrichmentExecutor = enrichmentExecutor;
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
                    .incoming(
                        Event.EventActor.builder()
                            .deviceGuid(device.getGuid())
                            .channel(channel)
                            .build()
                    )
                    .payload(payload)
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

            deviceEventService.logIncomingEvent(device, event);

            eventRouteExecutor.execute(event,device.toURI());
        } else {
            LOGGER.debug(MessageFormat.format(EVENT_DROPPED,
                device.toURI(),
                payload));
        }
    }

//    public void process(String apiKey, String channel, Optional<Long> offset, Optional<Long> waitTime,
//    		DeferredResult<List<Event>> deferredResult, JedisPubSub jedisPubSub) throws BusinessException {
//    	Optional.ofNullable(apiKey).filter(s -> !s.isEmpty())
//    			.orElseThrow(() -> new BusinessException(Messages.APIKEY_MISSING.getCode()));
//
//    	Optional.ofNullable(channel).filter(s -> !s.isEmpty())
//    			.orElseThrow(() -> new BusinessException(Messages.CHANNEL_MISSING.getCode()));
//
//    	Device device = Optional.ofNullable(deviceRegisterService.findByApiKey(apiKey))
//    			.orElseThrow(() -> new BusinessException(Messages.DEVICE_NOT_FOUND.getCode()));
//
//    	if (offset.isPresent()) {
//    		Instant startTimestamp = Instant.ofEpochMilli(offset.get());
//
//			NewServiceResponse<List<Event>> response = deviceEventService.findEventsBy(device.getTenant(), device.getDeviceId(),
//    				startTimestamp, null, 50);
//
//            if (!response.getResult().isEmpty() || !waitTime.isPresent() || (waitTime.isPresent() && waitTime.get().equals(new Long("0")))) {
//                response.getResult().sort((e1, e2) -> e1.getTimestamp().compareTo(e2.getTimestamp()));
//                deferredResult.setResult(response.getResult());
//
//            } else {
//                CompletableFuture.runAsync(() ->  {
//                    Jedis jedis = (Jedis) redisTemplate.getConnectionFactory().getConnection().getNativeConnection();
//                    jedis.subscribe(jedisPubSub, apiKey+"."+channel);
//                });
//            }
//        } else {
//            NewServiceResponse<List<Event>> response = deviceEventService.findLastEventBy(device.getTenant(), device.getDeviceId());
//            response.getResult().sort((e1, e2) -> e1.getTimestamp().compareTo(e2.getTimestamp()));
//    		deferredResult.setResult(response.getResult());
//    	}
//    }
}
