package com.konkerlabs.platform.registry.business.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.konkerlabs.platform.registry.business.model.AlertTrigger;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.AlertTriggerRepository;
import com.konkerlabs.platform.registry.business.services.api.AlertTriggerService;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;

@Service
public class AlertTriggerServiceImpl implements AlertTriggerService {

    @Autowired
    private AlertTriggerRepository alertTriggerRepository;

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

}
