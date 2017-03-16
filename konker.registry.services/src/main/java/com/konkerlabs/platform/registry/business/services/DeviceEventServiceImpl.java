package com.konkerlabs.platform.registry.business.services;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.EventSchema;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.repositories.events.api.EventRepository;
import com.konkerlabs.platform.registry.business.services.api.*;
import com.konkerlabs.platform.registry.config.EventStorageConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DeviceEventServiceImpl implements DeviceEventService {

    private EventRepository eventRepository;
    @Autowired
    private EventSchemaService eventSchemaService;
    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private DeviceRegisterService deviceRegisterService;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private EventStorageConfig eventStorageConfig;
    @Autowired
    private ApplicationContext applicationContext;


    @PostConstruct
    public void init(){
        eventRepository =
                (EventRepository) applicationContext.getBean(
                        eventStorageConfig.getEventRepositoryBean()
                );
    }


    @Override
    public ServiceResponse<Event> logIncomingEvent(Device device, Event event) {
        return doLog(device,event,() -> {
            try {
                ServiceResponse<EventSchema> schemaResponse = eventSchemaService.appendIncomingSchema(event);

                if (schemaResponse.isOk()) {
                    return ServiceResponseBuilder.<Event>ok()
                            .withResult(eventRepository.saveIncoming(device.getTenant(), event)).build();
                } else {
                    return ServiceResponseBuilder.<Event>error()
                        .withMessages(schemaResponse.getResponseMessages()).build();
                }
            } catch (BusinessException e) {
                return ServiceResponseBuilder.<Event>error()
                        .withMessage(e.getMessage()).build();
            }
        });
    }

    @Override
    public ServiceResponse<Event> logOutgoingEvent(Device device, Event event) {
        return doLog(device,event,() -> {
            try {
                Event saved = eventRepository.saveOutgoing(device.getTenant(), event);
                redisTemplate.convertAndSend(
                        device.getApiKey() + "." + event.getOutgoing().getChannel(),
                        device.getGuid());

                return ServiceResponseBuilder.<Event>ok().withResult(saved).build();
            } catch (BusinessException e) {
                return ServiceResponseBuilder.<Event>error()
                        .withMessage(e.getMessage()).build();
            }
        });
    }

    private ServiceResponse<Event> doLog(Device device, Event event, Supplier<ServiceResponse<Event>> callable) {
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
