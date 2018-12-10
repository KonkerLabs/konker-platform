package com.konkerlabs.platform.registry.business.services;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.DeviceRepository;
import com.konkerlabs.platform.registry.business.repositories.events.api.EventRepository;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.config.EventStorageConfig;
import com.konkerlabs.platform.registry.type.EventStorageConfigType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DeviceEventServiceImpl implements DeviceEventService {

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private EventStorageConfig eventStorageConfig;
    @Autowired
    private DeviceRepository deviceRepository;

    private EventRepository eventRepository;

    @PostConstruct
    public void init() {
        try {
            eventRepository =
                    (EventRepository) applicationContext.getBean(
                            eventStorageConfig.getEventRepositoryBean()
                    );
        } catch (Exception e) {
            eventRepository =
                    (EventRepository) applicationContext.getBean(
                            EventStorageConfigType.MONGODB.bean()
                    );
        }
    }


    @Override
    public ServiceResponse<List<Event>> findIncomingBy(Tenant tenant,
                                                       Application application,
                                                       User user,
                                                       String deviceGuid,
                                                       String channel,
                                                       Instant startingTimestamp,
                                                       Instant endTimestamp,
                                                       boolean ascending,
                                                       Integer limit) {
        if (Optional.ofNullable(user.getApplication()).isPresent()
                && !application.equals(user.getApplication())) {

            return ServiceResponseBuilder.<List<Event>>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_HAS_NO_PERMISSION.getCode())
                    .build();
        }

        ServiceResponse<List<Event>> serviceResponse = findIncomingBy(tenant, application, deviceGuid, channel, startingTimestamp, endTimestamp, ascending, limit);

        if (serviceResponse.isOk() &&
                Optional.ofNullable(user.getLocation()).isPresent()) {
            List<Event> eventList = serviceResponse.getResult();
            List<Event> eventsFiltered = eventList.stream()
                .filter(event -> user.getLocation().equals(
                        deviceRepository.findByTenantAndGuid(
                                tenant.getId(),
                                event.getIncoming().getDeviceGuid()).getLocation()))
                .collect(Collectors.toList());
            serviceResponse.setResult(eventsFiltered);
        }

        return serviceResponse;
    }

    @Override
    public ServiceResponse<List<Event>> findIncomingBy(Tenant tenant,
                                                       Application application,
                                                       String deviceGuid,
                                                       String channel,
                                                       Instant startTimestamp,
                                                       Instant endTimestamp,
                                                       boolean ascending,
                                                       Integer limit) {
        if (!Optional.ofNullable(tenant).isPresent())
            return ServiceResponseBuilder.<List<Event>>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode())
                    .build();

        if (!Optional.ofNullable(application).isPresent())
        	return ServiceResponseBuilder.<List<Event>>error()
        			.withMessage(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode())
        			.build();


        if (!Optional.ofNullable(startTimestamp).isPresent() &&
                !Optional.ofNullable(limit).isPresent())
            return ServiceResponseBuilder.<List<Event>>error()
                    .withMessage(Validations.LIMIT_NULL.getCode())
                    .build();

        try {
            return ServiceResponseBuilder.<List<Event>>ok()
                    .withResult(eventRepository.findIncomingBy(tenant,
                            application,
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
                                                       Application application,
                                                       User user,
                                                       String deviceGuid,
                                                       String channel,
                                                       Instant startingTimestamp,
                                                       Instant endTimestamp,
                                                       boolean ascending,
                                                       Integer limit) {
        if (Optional.ofNullable(user.getApplication()).isPresent()
                && !application.equals(user.getApplication())) {

            return ServiceResponseBuilder.<List<Event>>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_HAS_NO_PERMISSION.getCode())
                    .build();
        }

        ServiceResponse<List<Event>> serviceResponse = findOutgoingBy(tenant, application, deviceGuid, channel, startingTimestamp, endTimestamp, ascending, limit);

        if (serviceResponse.isOk() &&
                Optional.ofNullable(user.getLocation()).isPresent()) {
            List<Event> eventList = serviceResponse.getResult();
            List<Event> eventsFiltered = eventList.stream()
                    .filter(event -> user.getLocation().equals(
                            deviceRepository.findByTenantAndGuid(
                                    tenant.getId(),
                                    event.getOutgoing().getDeviceGuid()).getLocation()))
                    .collect(Collectors.toList());
            serviceResponse.setResult(eventsFiltered);
        }

        return serviceResponse;
    }

    @Override
    public ServiceResponse<List<Event>> findOutgoingBy(Tenant tenant,
                                                       Application application,
                                                       String deviceGuid,
                                                       String channel,
                                                       Instant startingTimestamp,
                                                       Instant endTimestamp,
                                                       boolean ascending,
                                                       Integer limit) {
        if (!Optional.ofNullable(tenant).isPresent())
            return ServiceResponseBuilder.<List<Event>>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode())
                    .build();

        if (!Optional.ofNullable(application).isPresent())
        	return ServiceResponseBuilder.<List<Event>>error()
        			.withMessage(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode())
        			.build();

//        if (!Optional.ofNullable(channel).filter(s -> !s.isEmpty()).isPresent())
//            return ServiceResponseBuilder.<List<Event>>error()
//                    .withMessage(Validations.CHANNEL_NULL.getCode(), null)
//                    .build();

        if (!Optional.ofNullable(startingTimestamp).isPresent() &&
                !Optional.ofNullable(limit).isPresent())
            return ServiceResponseBuilder.<List<Event>>error()
                    .withMessage(Validations.LIMIT_NULL.getCode())
                    .build();

        try {
            return ServiceResponseBuilder.<List<Event>>ok()
                    .withResult(eventRepository.findOutgoingBy(tenant,
                            application,
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
