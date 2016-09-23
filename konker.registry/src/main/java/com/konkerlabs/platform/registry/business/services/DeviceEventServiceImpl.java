package com.konkerlabs.platform.registry.business.services;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.repositories.events.EventRepository;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.NewServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DeviceEventServiceImpl implements DeviceEventService {

    @Autowired
    @Qualifier("mongoEvents")
    private EventRepository eventRepository;
    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private DeviceRegisterService deviceRegisterService;

    @Override
    public void logEvent(Device device, String channel, Event event) throws BusinessException {
        Optional.ofNullable(device)
            .orElseThrow(() -> new BusinessException("Device cannot be null"));

        if (event == null)
            throw new BusinessException("Event cannot be null");
        if (event.getPayload() == null || event.getPayload().isEmpty())
            throw new BusinessException("Event payload cannot be null or empty");

        if (!Optional.ofNullable(event.getTimestamp()).isPresent())
            event.setTimestamp(Instant.now());

        event.setChannel(channel);

        eventRepository.push(device.getTenant(), event);
    }

    @Override
    public NewServiceResponse<List<Event>> findEventsBy(Tenant tenant, String deviceId,
                                                        Instant startTimestamp,
                                                        Instant endTimestamp, Integer limit) {
        if (!Optional.ofNullable(tenant).isPresent())
            return ServiceResponseBuilder.<List<Event>>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode(), null)
                    .build();

        if (!Optional.ofNullable(deviceId).isPresent())
            return ServiceResponseBuilder.<List<Event>>error()
                    .withMessage(DeviceRegisterService.Validations.DEVICE_ID_NULL.getCode(), null)
                    .build();

        if (!Optional.ofNullable(startTimestamp).isPresent() &&
            !Optional.ofNullable(limit).isPresent())
            return ServiceResponseBuilder.<List<Event>>error()
                    .withMessage(Validations.LIMIT_NULL.getCode(), null)
                    .build();

        return ServiceResponseBuilder.<List<Event>>ok()
                .withResult(eventRepository.findBy(tenant, deviceId, startTimestamp, endTimestamp, limit)).build();
    }
}
