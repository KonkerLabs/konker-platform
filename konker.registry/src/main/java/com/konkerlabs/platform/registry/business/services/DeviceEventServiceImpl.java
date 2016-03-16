package com.konkerlabs.platform.registry.business.services;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.repositories.DeviceRepository;
import com.konkerlabs.platform.registry.business.repositories.solr.EventRepository;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DeviceEventServiceImpl implements DeviceEventService {

    @Autowired
    private DeviceRepository deviceRepository;
    @Autowired
    private EventRepository eventRepository;

    @Override
    public void logEvent(Device device, Event event) throws BusinessException {
        Optional.ofNullable(device)
            .orElseThrow(() -> new BusinessException("Device cannot be null"));
        Optional.ofNullable(deviceRepository.findByApiKey(device.getApiKey()))
            .orElseThrow(() -> new BusinessException(MessageFormat.format("Device with API Key [{0}] does not exist",
                    device.getApiKey())));

        if (event == null)
            throw new BusinessException("Event cannot be null");
        if (event.getPayload() == null || event.getPayload().isEmpty())
            throw new BusinessException("Event payload cannot be null or empty");
        if (event.getTimestamp() != null)
            throw new BusinessException("Event timestamp cannot be already set!");

        if (device.getEvents() == null)
            device.setEvents(new ArrayList<>());

        if (!Optional.ofNullable(event.getTimestamp()).isPresent())
            event.setTimestamp(Instant.now());

        device.getEvents().add(event);

        deviceRepository.save(device);

        eventRepository.push(device.getTenant(), event);
    }
}
