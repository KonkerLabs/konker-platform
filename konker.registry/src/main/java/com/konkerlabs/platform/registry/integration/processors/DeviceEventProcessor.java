package com.konkerlabs.platform.registry.integration.processors;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.EnrichmentExecutor;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.routes.api.EventRouteExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Optional;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DeviceEventProcessor {

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

    public void process(String topic, String payload) throws BusinessException {

        String apiKey = extractFromTopicLevel(topic, 1);
        if (apiKey == null) {
            throw new BusinessException("Device API Key cannot be retrieved");
        }

        String incomingChannel = extractFromTopicLevel(topic, 2);
        if (incomingChannel == null) {
            throw new BusinessException("Event incoming channel cannot be retrieved");
        }

        Device device = Optional.ofNullable(deviceRegisterService.findByApiKey(apiKey))
            .orElseThrow(() -> new BusinessException("Incoming device does not exist"));

        if (device.isActive()) {
            Event event = Event.builder()
                    .channel(incomingChannel)
                    .payload(payload)
                    .build();

            ServiceResponse<Event> serviceResponse = enrichmentExecutor.enrich(event, device);
            switch (serviceResponse.getStatus()) {
                case ERROR: {
                    LOGGER.error(MessageFormat.format("Enrichment failed: [Device: {0}] - [Payload: {1}]", device.toURI(), payload));
                }
                default: {
                    event.setPayload(serviceResponse.getResult().getPayload());
                }

            }

            deviceEventService.logEvent(device, event);

            eventRouteExecutor.execute(event,device.toURI());
        } else {
            LOGGER.debug(MessageFormat.format(EVENT_DROPPED,
                device.toURI(),
                payload));
        }
    }

    private String extractFromTopicLevel(String channel, int index) {
        try {
            return channel.split("/")[index];
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }
}
