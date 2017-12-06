package com.konkerlabs.platform.registry.business.services;

import java.util.*;

import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.services.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.AlertTriggerRepository;

@Service
public class AlertTriggerServiceImpl implements AlertTriggerService {

    @Autowired
    private AlertTriggerRepository alertTriggerRepository;

    @Autowired
    private HealthAlertService healthAlertService;

    @Override
    public ServiceResponse<List<AlertTrigger>> listByTenantAndApplication(Tenant tenant, Application application) {

        ServiceResponse<List<AlertTrigger>> validationsResponse = validate(tenant, application);
        if (validationsResponse != null && !validationsResponse.isOk()) {
            return validationsResponse;
        }

        List<AlertTrigger> triggers = alertTriggerRepository.listByTenantIdAndApplicationName(tenant.getId(),
                application.getName());

        return ServiceResponseBuilder.<List<AlertTrigger>>ok().withResult(triggers).build();

    }

    @Override
    public ServiceResponse<AlertTrigger> findByTenantAndApplicationAndGuid(Tenant tenant, Application application,
            String triggerGuid) {

        ServiceResponse<AlertTrigger> validationsResponse = validate(tenant, application);
        if (validationsResponse != null && !validationsResponse.isOk()) {
            return validationsResponse;
        }

        AlertTrigger trigger = alertTriggerRepository.findByTenantIdAndApplicationNameAndGuid(tenant.getId(), application.getName(), triggerGuid);

        if (trigger != null) {
            return ServiceResponseBuilder.<AlertTrigger>ok().withResult(trigger).build();
        } else {
            return ServiceResponseBuilder.<AlertTrigger>error().withMessage(Validations.ALERT_TRIGGER_NOT_FOUND.getCode()).build();
        }

    }

    @Override
    public ServiceResponse<AlertTrigger> findByTenantAndApplicationAndName(Tenant tenant, Application application, String triggerName) {

        ServiceResponse<AlertTrigger> validationsResponse = validate(tenant, application);
        if (validationsResponse != null && !validationsResponse.isOk()) {
            return validationsResponse;
        }

        AlertTrigger trigger = alertTriggerRepository.findByTenantIdAndApplicationNameAndName(tenant.getId(), application.getName(), triggerName);

        if (trigger != null) {
            return ServiceResponseBuilder.<AlertTrigger>ok().withResult(trigger).build();
        } else {
            return ServiceResponseBuilder.<AlertTrigger>error().withMessage(Validations.ALERT_TRIGGER_NOT_FOUND.getCode()).build();
        }

    }

    @Override
    public ServiceResponse<AlertTrigger> save(Tenant tenant, Application application, AlertTrigger trigger) {

        ServiceResponse<AlertTrigger> validationsResponse = validate(tenant, application);
        if (validationsResponse != null && !validationsResponse.isOk()) {
            return validationsResponse;
        }

        Map<String, Object[]> validations = trigger.applyValidations();
        if (!validations.isEmpty()) {
            return ServiceResponseBuilder.<AlertTrigger>error()
                    .withMessages(validations)
                    .build();
        }

        AlertTrigger existing = alertTriggerRepository.findByTenantIdAndApplicationNameAndName(
                tenant.getId(), application.getName(), trigger.getName());
        if (existing != null) {
            return ServiceResponseBuilder.<AlertTrigger>error()
                    .withMessage(Validations.ALERT_TRIGGER_ALREADY_EXISTS.getCode()).build();
        }

        trigger.setTenant(tenant);
        trigger.setApplication(application);
        trigger.setGuid(UUID.randomUUID().toString());

        AlertTrigger saved = alertTriggerRepository.save(trigger);

        return ServiceResponseBuilder.<AlertTrigger>ok().withResult(saved).build();

    }

    @Override
    public ServiceResponse<AlertTrigger> remove(Tenant tenant, Application application, String guid) {

        ServiceResponse<AlertTrigger> validationsResponse = validate(tenant, application);
        if (validationsResponse != null && !validationsResponse.isOk()) {
            return validationsResponse;
        }

        if (guid == null) {
            return ServiceResponseBuilder.<AlertTrigger>error()
                    .withMessage(Validations.ALERT_TRIGGER_GUID_NULL.getCode()).build();
        }

        AlertTrigger fromDb = alertTriggerRepository.findByTenantIdAndApplicationNameAndGuid(
                tenant.getId(),
                application.getName(),
                guid);

        if (!Optional.ofNullable(fromDb).isPresent()) {
            return ServiceResponseBuilder.<AlertTrigger>error()
                    .withMessage(Validations.ALERT_TRIGGER_NOT_FOUND.getCode()).build();
        }

        healthAlertService.removeAlertsFromTrigger(tenant, application, guid);
        alertTriggerRepository.delete(fromDb);

        return ServiceResponseBuilder.<AlertTrigger>ok().withResult(fromDb).build();

    }

    private <T> ServiceResponse<T> validate(Tenant tenant, Application application) {

        if (!Optional.ofNullable(tenant).isPresent()) {
            return ServiceResponseBuilder.<T>error().withMessage(CommonValidations.TENANT_NULL.getCode()).build();
        }

        if (!Optional.ofNullable(application).isPresent()) {
            return ServiceResponseBuilder.<T>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()).build();
        }

        return null;
    }

    @Override
    public ServiceResponse<AlertTrigger> update(Tenant tenant, Application application, String guid,
                                                AlertTrigger trigger) {

        ServiceResponse<AlertTrigger> validationsResponse = validate(tenant, application);
        if (validationsResponse != null && !validationsResponse.isOk()) {
            return validationsResponse;
        }

        if (guid == null) {
            return ServiceResponseBuilder.<AlertTrigger>error()
                    .withMessage(Validations.ALERT_TRIGGER_GUID_NULL.getCode()).build();
        }

        Map<String, Object[]> validations = trigger.applyValidations();
        if (!validations.isEmpty()) {
            return ServiceResponseBuilder.<AlertTrigger>error().withMessages(validations).build();
        }

        AlertTrigger fromDb = alertTriggerRepository.findByTenantIdAndApplicationNameAndGuid(
                tenant.getId(), application.getName(), guid);

        if (!Optional.ofNullable(fromDb).isPresent()) {
            return ServiceResponseBuilder.<AlertTrigger>error()
                    .withMessage(Validations.ALERT_TRIGGER_NOT_FOUND.getCode()).build();
        }

        fromDb.setDescription(trigger.getDescription());
        fromDb.setMinutes(trigger.getMinutes());

        AlertTrigger saved = alertTriggerRepository.save(fromDb);

        return ServiceResponseBuilder.<AlertTrigger>ok().withResult(saved).build();

    }

}
