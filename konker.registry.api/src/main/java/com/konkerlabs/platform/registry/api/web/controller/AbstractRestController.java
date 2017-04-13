package com.konkerlabs.platform.registry.api.web.controller;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import com.konkerlabs.platform.registry.api.exceptions.BadServiceResponseException;
import com.konkerlabs.platform.registry.api.exceptions.NotFoundResponseException;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;

public abstract class AbstractRestController {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    protected User user;

    protected Application getApplication(String applicationId) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        ServiceResponse<Application> applicationResponse = applicationService.getByApplicationName(tenant, applicationId);
        if (!applicationResponse.isOk()) {
            if (applicationResponse.getResponseMessages().containsKey(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode())) {
                throw new NotFoundResponseException(user, applicationResponse);

            } else {
                Set<String> validationsCode = new HashSet<>();
                for (ApplicationService.Validations value : ApplicationService.Validations.values()) {
                    validationsCode.add(value.getCode());
                }

                throw new BadServiceResponseException(user, applicationResponse, validationsCode);
            }
        }

        return applicationResponse.getResult();

    }

}
