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
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

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
    public NewServiceResponse<Event> logIncomingEvent(Device device, Event event) {
        return doLog(device,event,() -> {
            try {
                eventRepository.saveIncoming(device.getTenant(), event);
            } catch (BusinessException e) {
                return ServiceResponseBuilder.<Event>error()
                        .withMessage(e.getMessage()).build();
            }

            return ServiceResponseBuilder.<Event>ok().build();
        });
    }

    @Override
    public NewServiceResponse<Event> logOutgoingEvent(Device device, Event event) {
        return doLog(device,event,() -> {
            try {
                eventRepository.saveOutgoing(device.getTenant(), event);
            } catch (BusinessException e) {
                return ServiceResponseBuilder.<Event>error()
                        .withMessage(e.getMessage()).build();
            }

            return ServiceResponseBuilder.<Event>ok().build();
        });
    }

    private NewServiceResponse<Event> doLog(Device device, Event event, Supplier<NewServiceResponse<Event>> callable) {
        if (!Optional.ofNullable(device).isPresent())
            return ServiceResponseBuilder.<Event>error()
                    .withMessage(Validations.DEVICE_NULL.getCode()).build();

        if (!Optional.ofNullable(event).isPresent())
            return ServiceResponseBuilder.<Event>error()
                    .withMessage(Validations.EVENT_NULL.getCode()).build();
        if (!Optional.ofNullable(event.getPayload()).filter(s -> !s.isEmpty()).isPresent())
            return ServiceResponseBuilder.<Event>error()
                    .withMessage(Validations.EVENT_PAYLOAD_NULL.getCode()).build();

        if (!Optional.ofNullable(event.getTimestamp()).isPresent())
            event.setTimestamp(Instant.now());

        return callable.get();
    }

    @Override
    public NewServiceResponse<List<Event>> findIncomingBy(Tenant tenant, String deviceGuid,
                                                          Instant startTimestamp,
                                                          Instant endTimestamp, Integer limit) {
        if (!Optional.ofNullable(tenant).isPresent())
            return ServiceResponseBuilder.<List<Event>>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode(), null)
                    .build();

        if (!Optional.ofNullable(deviceGuid).isPresent())
            return ServiceResponseBuilder.<List<Event>>error()
                    .withMessage(DeviceRegisterService.Validations.DEVICE_GUID_NULL.getCode(), null)
                    .build();

        if (!Optional.ofNullable(startTimestamp).isPresent() &&
            !Optional.ofNullable(limit).isPresent())
            return ServiceResponseBuilder.<List<Event>>error()
                    .withMessage(Validations.LIMIT_NULL.getCode(), null)
                    .build();

        try {
            return ServiceResponseBuilder.<List<Event>>ok()
                    .withResult(eventRepository.findIncomingBy(tenant, deviceGuid, startTimestamp, endTimestamp, limit)).build();
        } catch (BusinessException e) {
            return ServiceResponseBuilder.<List<Event>>error()
                    .withMessage(e.getMessage())
                    .build();
        }
    }

    @Override
    public NewServiceResponse<List<Event>> findOutgoingBy(Tenant tenant, String deviceGuid, Instant startingTimestamp, Instant endTimestamp, Integer limit) {
        if (!Optional.ofNullable(tenant).isPresent())
            return ServiceResponseBuilder.<List<Event>>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode(), null)
                    .build();

        if (!Optional.ofNullable(deviceGuid).isPresent())
            return ServiceResponseBuilder.<List<Event>>error()
                    .withMessage(DeviceRegisterService.Validations.DEVICE_GUID_NULL.getCode(), null)
                    .build();

        if (!Optional.ofNullable(startingTimestamp).isPresent() &&
                !Optional.ofNullable(limit).isPresent())
            return ServiceResponseBuilder.<List<Event>>error()
                    .withMessage(Validations.LIMIT_NULL.getCode(), null)
                    .build();

        try {
            return ServiceResponseBuilder.<List<Event>>ok()
                    .withResult(eventRepository.findOutgoingBy(tenant, deviceGuid, startingTimestamp, endTimestamp, limit)).build();
        } catch (BusinessException e) {
            return ServiceResponseBuilder.<List<Event>>error()
                    .withMessage(Validations.LIMIT_NULL.getCode())
                    .build();
        }
    }
}
