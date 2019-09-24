package com.konkerlabs.platform.registry.business.services;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.EventRoute;
import com.konkerlabs.platform.registry.business.model.EventRouteCounter;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.ApplicationRepository;
import com.konkerlabs.platform.registry.business.repositories.EventRouteCounterRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.EventRouteCounterService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.Optional;
import java.util.UUID;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class EventRouteCounterServiceImpl implements EventRouteCounterService {

    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private EventRouteCounterRepository eventRouteCounterRepository;


    @Override
    public ServiceResponse<EventRouteCounter> save(Tenant tenant,
                                                   Application application,
                                                   EventRouteCounter eventRouteCounter) {
        ServiceResponse<EventRouteCounter> validationResponse = validateFull(tenant, application, eventRouteCounter);
        if (!validationResponse.isOk()) {
            return validationResponse;
        }

        eventRouteCounter.setCreationDate(Instant.now());
        eventRouteCounter.setGuid(UUID.randomUUID().toString());
        eventRouteCounter.setTenant(tenant);
        eventRouteCounter.setApplication(application);

        return ServiceResponseBuilder.<EventRouteCounter>ok()
                .withResult(eventRouteCounterRepository.save(eventRouteCounter))
                .build();
    }

    @Override
    public ServiceResponse<EventRouteCounter> getByEventRoute(Tenant tenant,
                                                              Application application,
                                                              EventRoute eventRoute) {
        ServiceResponse<EventRouteCounter> validationResponse = validate(tenant, application);
        if (!validationResponse.isOk()) {
            return validationResponse;
        }

        if (!Optional.ofNullable(eventRoute).isPresent()) {
            return ServiceResponseBuilder.<EventRouteCounter>error()
                    .withMessage(Validations.EVENT_ROUTE_NULL.getCode())
                    .build();
        }

        YearMonth ym = YearMonth.now();
        LocalDate firstDate = ym.atDay(1);
        LocalDate lastDate = ym.atEndOfMonth();

        LocalDateTime firstDateTime = firstDate.atStartOfDay();
        LocalDateTime lastDateTime = lastDate.atTime(23, 59, 59);

        Instant start = firstDateTime.toInstant(ZoneOffset.UTC);
        Instant end = lastDateTime.toInstant(ZoneOffset.UTC);

        EventRouteCounter eventRouteCounter = eventRouteCounterRepository.getByEventRouteAndCreationDate(tenant.getId(),
                application.getName(),
                eventRoute.getId(),
                start,
                end);

        return ServiceResponseBuilder.<EventRouteCounter>ok()
                .withResult(eventRouteCounter)
                .build();
    }


    private <T> ServiceResponse<T> validate(Tenant tenant, Application application) {
        if (!Optional.ofNullable(tenant).isPresent())
            return ServiceResponseBuilder.<T>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode()).build();

        Tenant existingTenant = tenantRepository.findByDomainName(tenant.getDomainName());

        if (!Optional.ofNullable(existingTenant).isPresent())
            return ServiceResponseBuilder.<T>error()
                    .withMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()).build();

        if (!Optional.ofNullable(application).isPresent())
            return ServiceResponseBuilder.<T>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()).build();

        Application existingApplication = applicationRepository.findByTenantAndName(tenant.getId(), application.getName());

        if (!Optional.ofNullable(existingApplication).isPresent())
            return ServiceResponseBuilder.<T>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_NOT_FOUND.getCode()).build();

        return ServiceResponseBuilder.<T>ok().build();
    }

    private <T> ServiceResponse<T> validateFull(Tenant tenant, Application application, EventRouteCounter eventRouteCounter) {
        ServiceResponse<T> validationResponse = validate(tenant, application);
        if (!validationResponse.isOk()) {
            return validationResponse;
        }

        if (!Optional.ofNullable(eventRouteCounter).isPresent()) {
            return ServiceResponseBuilder.<T>error()
                    .withMessage(Validations.EVENT_ROUTE_COUNTER_NULL.getCode()).build();
        }

        if (!Optional.ofNullable(eventRouteCounter.getEventRoute()).isPresent()) {
            return ServiceResponseBuilder.<T>error()
                    .withMessage(Validations.EVENT_ROUTE_NULL.getCode()).build();
        }

        return ServiceResponseBuilder.<T>ok().build();
    }
}
