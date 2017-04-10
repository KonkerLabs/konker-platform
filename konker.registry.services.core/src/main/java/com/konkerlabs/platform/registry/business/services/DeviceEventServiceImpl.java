package com.konkerlabs.platform.registry.business.services;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.konkerlabs.platform.registry.business.repositories.events.api.EventRepository;
import com.konkerlabs.platform.registry.config.EventStorageConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;

import javax.annotation.PostConstruct;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DeviceEventServiceImpl implements DeviceEventService {

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private EventStorageConfig eventStorageConfig;

    private EventRepository eventRepository;


    @PostConstruct
    public void init(){
        try {
            eventRepository =
                    (EventRepository) applicationContext.getBean(
                            eventStorageConfig.getEventRepositoryBean()
                    );
        } catch (Exception e){
            eventRepository =
                    (EventRepository) applicationContext.getBean(
                            EventStorageConfig.EventStorageConfigType.MONGODB.bean()
                    );
        }

    }


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
                    .withMessage(CommonValidations.TENANT_NULL.getCode())
                    .build();

//        if (!Optional.ofNullable(channel).filter(s -> !s.isEmpty()).isPresent())
//            return ServiceResponseBuilder.<List<Event>>error()
//                    .withMessage(Validations.CHANNEL_NULL.getCode(), null)
//                    .build();

        if (!Optional.ofNullable(startTimestamp).isPresent() &&
                !Optional.ofNullable(limit).isPresent())
            return ServiceResponseBuilder.<List<Event>>error()
                    .withMessage(Validations.LIMIT_NULL.getCode())
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
                    .withMessage(CommonValidations.TENANT_NULL.getCode())
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
