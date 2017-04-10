package com.konkerlabs.platform.registry.data.services;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.EventSchema;
import com.konkerlabs.platform.registry.business.repositories.events.api.EventRepository;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService.Validations;
import com.konkerlabs.platform.registry.business.services.api.EventSchemaService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.config.EventStorageConfig;
import com.konkerlabs.platform.registry.data.services.api.DeviceLogEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Supplier;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DeviceLogEventServiceImpl implements DeviceLogEventService {

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private EventStorageConfig eventStorageConfig;
    private EventRepository eventRepository;
    @Autowired
    private EventSchemaService eventSchemaService;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;


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
    public ServiceResponse<Event> logIncomingEvent(Device device, Event event) {
        return doLog(device,event,() -> {
            try {
                ServiceResponse<EventSchema> schemaResponse = eventSchemaService.appendIncomingSchema(event);

                if (schemaResponse.isOk()) {
                    return ServiceResponseBuilder.<Event>ok()
                            .withResult(eventRepository.saveIncoming(device.getTenant(), device.getApplication(),event)).build();
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
                Event saved = eventRepository.saveOutgoing(device.getTenant(), device.getApplication(), event);
                redisTemplate.convertAndSend(
                        device.getApiKey(),
                        device.getGuid());

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


}
