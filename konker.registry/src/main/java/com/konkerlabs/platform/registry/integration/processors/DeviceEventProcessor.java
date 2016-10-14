package com.konkerlabs.platform.registry.integration.processors;

import java.text.MessageFormat;
import java.util.Optional;

import com.konkerlabs.platform.utilities.parsers.json.JsonParsingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.EnrichmentExecutor;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.routes.api.EventRouteExecutor;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DeviceEventProcessor {

    public enum Messages {
        APIKEY_MISSING("integration.event_processor.api_key.missing"),
        CHANNEL_MISSING("integration.event_processor.channel.missing"),
        DEVICE_NOT_FOUND("integration.event_processor.channel.not_found"),
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
                                    .deviceId(device.getDeviceId())
                                    .build()
                    )
                    .payload(payload)
                    .build();

            ServiceResponse<Event> serviceResponse = enrichmentExecutor.enrich(event, device);
            switch (serviceResponse.getStatus()) {
                case ERROR: {
                    LOGGER.error(MessageFormat.format("Enrichment failed: [Device: {0}] - [Payload: {1}]", device.toURI(), payload));
                    break;
                }
                case OK: {
                    event.setPayload(serviceResponse.getResult().getPayload());
                    break;
                }
                default:
                    break;
            }

            ServiceResponse<Event> logResponse = deviceEventService.logIncomingEvent(device, event);
            if (logResponse.isOk()) {
                eventRouteExecutor.execute(event, device.toURI());
            } else {
                LOGGER.error(MessageFormat.format("Could not log incoming message. Probably invalid payload.: [Device: {0}] - [Payload: {1}]", device.toURI(), payload));
                throw new BusinessException(Messages.INVALID_PAYLOAD.getCode());
            }

        } else {
            LOGGER.debug(MessageFormat.format(EVENT_DROPPED,
                    device.toURI(),
                    payload));
        }


    }
}
