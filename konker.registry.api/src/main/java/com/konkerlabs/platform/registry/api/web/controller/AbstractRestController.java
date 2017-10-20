package com.konkerlabs.platform.registry.api.web.controller;

import java.util.HashSet;
import java.util.Set;

import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.services.api.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.konkerlabs.platform.registry.api.exceptions.BadServiceResponseException;
import com.konkerlabs.platform.registry.api.exceptions.NotFoundResponseException;

public abstract class AbstractRestController {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private LocationSearchService locationSearchService;

    @Autowired
    private DeviceModelService deviceModelService;

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

    protected Location getLocation(Tenant tenant, Application application, String locationName) throws BadServiceResponseException, NotFoundResponseException {

        if (StringUtils.isBlank(locationName)) {
            return null;
        }

        ServiceResponse<Location> applicationResponse = locationSearchService.findByName(tenant, application, locationName, false);
        if (!applicationResponse.isOk()) {
            if (applicationResponse.getResponseMessages().containsKey(LocationService.Messages.LOCATION_NOT_FOUND.getCode())) {
                throw new NotFoundResponseException(user, applicationResponse);

            } else {
                Set<String> validationsCode = new HashSet<>();
                for (LocationService.Validations value : LocationService.Validations.values()) {
                    validationsCode.add(value.getCode());
                }

                throw new BadServiceResponseException(user, applicationResponse, validationsCode);
            }
        }

        return applicationResponse.getResult();

    }

    protected DeviceModel getDeviceModel(Tenant tenant, Application application, String deviceModelName) throws BadServiceResponseException, NotFoundResponseException {

        if (StringUtils.isBlank(deviceModelName)) {
            return null;
        }

        ServiceResponse<DeviceModel> applicationResponse = deviceModelService.getByTenantApplicationAndName(tenant, application, deviceModelName);
        if (!applicationResponse.isOk()) {
            if (applicationResponse.getResponseMessages().containsKey(DeviceModelService.Validations.DEVICE_MODEL_NOT_FOUND.getCode())) {
                throw new NotFoundResponseException(user, applicationResponse);

            } else {
                Set<String> validationsCode = new HashSet<>();
                for (DeviceModelService.Validations value : DeviceModelService.Validations.values()) {
                    validationsCode.add(value.getCode());
                }

                throw new BadServiceResponseException(user, applicationResponse, validationsCode);
            }
        }

        return applicationResponse.getResult();

    }


}
