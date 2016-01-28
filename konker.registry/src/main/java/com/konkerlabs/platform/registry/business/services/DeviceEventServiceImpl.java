package com.konkerlabs.platform.registry.business.services;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.repositories.DeviceRepository;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DeviceEventServiceImpl implements DeviceEventService {

    private DeviceRepository deviceRepository;

    @Autowired
    public DeviceEventServiceImpl(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @Override
    public void logEvent(Event event, String deviceId) throws BusinessException {
        if (event == null)
            throw new BusinessException("Event cannot be null");
        if (event.getPayload() == null || event.getPayload().isEmpty())
            throw new BusinessException("Event payload cannot be null or empty");
        if (event.getTimestamp() != null)
            throw new BusinessException("Event timestamp cannot be already set!");

        Device device = deviceRepository.findByDeviceId(deviceId);

        if (device == null)
            throw new BusinessException(MessageFormat.format("Device ID [{0}] does not exist",deviceId));

        if (device.getEvents() == null)
            device.setEvents(new ArrayList<>());

        event.setTimestamp(Instant.now());

        device.getEvents().add(event);

        deviceRepository.save(device);
    }
}
