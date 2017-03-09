package com.konkerlabs.platform.registry.business.services;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.events.EventRepository;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DeviceEventServiceImpl implements DeviceEventService {

    @Autowired
    @Qualifier("mongoEvents")
    private EventRepository eventRepository;

    @Override
    public ServiceResponse<List<Event>> findIncomingBy(Tenant tenant,
                                                       String deviceGuid,
                                                       String channel,
                                                       Instant startTimestamp,
                                                       Instant endTimestamp,
                                                       boolean ascending,
                                                       Integer limit) {
        if (!Optional.ofNullable(tenant).isPresent())
            return ServiceResponseBuilder.<List<Event>>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode(), null)
                    .build();

        if (!Optional.ofNullable(deviceGuid).filter(s -> !s.isEmpty()).isPresent())
            return ServiceResponseBuilder.<List<Event>>error()
                    .withMessage(DeviceRegisterService.Validations.DEVICE_GUID_NULL.getCode(), null)
                    .build();

//        if (!Optional.ofNullable(channel).filter(s -> !s.isEmpty()).isPresent())
//            return ServiceResponseBuilder.<List<Event>>error()
//                    .withMessage(Validations.CHANNEL_NULL.getCode(), null)
//                    .build();

        if (!Optional.ofNullable(startTimestamp).isPresent() &&
            !Optional.ofNullable(limit).isPresent())
            return ServiceResponseBuilder.<List<Event>>error()
                    .withMessage(Validations.LIMIT_NULL.getCode(), null)
                    .build();

        try {
            return ServiceResponseBuilder.<List<Event>>ok()
                    .withResult(eventRepository.findIncomingBy(tenant,
                            deviceGuid,
                            channel,
                            startTimestamp,
                            endTimestamp,
                            ascending,
                            limit)).build();
        } catch (BusinessException e) {
            return ServiceResponseBuilder.<List<Event>>error()
                    .withMessage(e.getMessage())
                    .build();
        }
    }

    @Override
    public ServiceResponse<List<Event>> findOutgoingBy(Tenant tenant,
                                                       String deviceGuid,
                                                       String channel,
                                                       Instant startingTimestamp,
                                                       Instant endTimestamp,
                                                       boolean ascending,
                                                       Integer limit) {
        if (!Optional.ofNullable(tenant).isPresent())
            return ServiceResponseBuilder.<List<Event>>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode(), null)
                    .build();

        if (!Optional.ofNullable(deviceGuid).filter(s -> !s.isEmpty()).isPresent())
            return ServiceResponseBuilder.<List<Event>>error()
                    .withMessage(DeviceRegisterService.Validations.DEVICE_GUID_NULL.getCode(), null)
                    .build();

//        if (!Optional.ofNullable(channel).filter(s -> !s.isEmpty()).isPresent())
//            return ServiceResponseBuilder.<List<Event>>error()
//                    .withMessage(Validations.CHANNEL_NULL.getCode(), null)
//                    .build();

        if (!Optional.ofNullable(startingTimestamp).isPresent() &&
                !Optional.ofNullable(limit).isPresent())
            return ServiceResponseBuilder.<List<Event>>error()
                    .withMessage(Validations.LIMIT_NULL.getCode(), null)
                    .build();

        try {
            return ServiceResponseBuilder.<List<Event>>ok()
                    .withResult(eventRepository.findOutgoingBy(tenant,
                            deviceGuid,
                            channel,
                            startingTimestamp,
                            endTimestamp,
                            ascending,
                            limit)).build();
        } catch (BusinessException e) {
            return ServiceResponseBuilder.<List<Event>>error()
                    .withMessage(Validations.LIMIT_NULL.getCode())
                    .build();
        }
    }
}
