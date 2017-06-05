package com.konkerlabs.platform.registry.api.web.controller;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.konkerlabs.platform.registry.api.exceptions.BadServiceResponseException;
import com.konkerlabs.platform.registry.api.exceptions.NotFoundResponseException;
import com.konkerlabs.platform.registry.api.model.SilenceTriggerInputVO;
import com.konkerlabs.platform.registry.api.model.SilenceTriggerVO;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.DeviceModel;
import com.konkerlabs.platform.registry.business.model.Location;
import com.konkerlabs.platform.registry.business.model.RestDestination.Validations;
import com.konkerlabs.platform.registry.business.model.SilenceTrigger;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.DeviceModelService;
import com.konkerlabs.platform.registry.business.services.api.LocationSearchService;
import com.konkerlabs.platform.registry.business.services.api.LocationService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.SilenceTriggerService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@Scope("request")
@RequestMapping(value = "/{application}/triggers/silence")
@Api(tags = "alert triggers")
public class SilenceTriggerRestController extends AbstractRestController implements InitializingBean {

    @Autowired
    private SilenceTriggerService silenceTriggerService;

    @Autowired
    private LocationSearchService locationSearchService;

    @Autowired
    private DeviceModelService deviceModelService;

    private Set<String> validationsCode = new HashSet<>();

    @GetMapping(path = "/{deviceModelName}/{locationName}")
    @ApiOperation(
            value = "Get a silence trigger by guid",
            response = SilenceTriggerVO.class
    )
    @PreAuthorize("hasAuthority('SHOW_ALERT_TRIGGER')")
    public SilenceTriggerVO read(
            @PathVariable("application") String applicationId,
            @PathVariable("deviceModelName") String deviceModelName,
            @PathVariable("locationName") String locationName) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);
        DeviceModel deviceModel = getDeviceModel(tenant, application, deviceModelName);
        Location location = getLocation(tenant, application, locationName);

        ServiceResponse<SilenceTrigger> restDestinationResponse = silenceTriggerService.findByTenantAndApplicationAndModelAndLocation(tenant, application, deviceModel, location);

        if (!restDestinationResponse.isOk()) {
            throw new NotFoundResponseException(user, restDestinationResponse);
        } else {
            return new SilenceTriggerVO(restDestinationResponse.getResult());
        }

    }

    @PostMapping(path = "/{deviceModelName}/{locationName}")
    @ApiOperation(value = "Create a silence trigger")
    @PreAuthorize("hasAuthority('CREATE_ALERT_TRIGGER')")
    public SilenceTriggerVO create(
            @PathVariable("application") String applicationId,
            @PathVariable("deviceModelName") String deviceModelName,
            @PathVariable("locationName") String locationName,
            @ApiParam(
            		name = "body",
            		value = "JSON filled with the fields described in Model and Example Value beside",
            		required = true)
            @RequestBody SilenceTriggerInputVO form) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);
        DeviceModel deviceModel = getDeviceModel(tenant, application, deviceModelName);
        Location location = getLocation(tenant, application, locationName);

        SilenceTrigger trigger = new SilenceTrigger();
        trigger.setTenant(tenant);
        trigger.setApplication(application);
        trigger.setDeviceModel(deviceModel);
        trigger.setLocation(location);
        trigger.setMinutes(form.getMinutes());
        trigger.setDescription(form.getDescription());

        ServiceResponse<SilenceTrigger> restDestinationResponse = silenceTriggerService.save(tenant, application, trigger);

        if (!restDestinationResponse.isOk()) {
            throw new BadServiceResponseException(user, restDestinationResponse, validationsCode);
        } else {
            return new SilenceTriggerVO().apply(restDestinationResponse.getResult());
        }

    }

    @PutMapping(path = "/{deviceModelName}/{locationName}")
    @ApiOperation(value = "Update a silence trigger")
    @PreAuthorize("hasAuthority('EDIT_ALERT_TRIGGER')")
    public void update(
            @PathVariable("application") String applicationId,
            @PathVariable("deviceModelName") String deviceModelName,
            @PathVariable("locationName") String locationName,
            @ApiParam(
            		name = "body",
            		value = "JSON filled with the fields described in Model and Example Value beside",
            		required = true)
            @RequestBody SilenceTriggerInputVO form) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);
        DeviceModel deviceModel = getDeviceModel(tenant, application, deviceModelName);
        Location location = getLocation(tenant, application, locationName);
        String guid = getSilenceTriggerGuid(tenant, application, deviceModel, location);

        SilenceTrigger trigger = new SilenceTrigger();
        trigger.setTenant(tenant);
        trigger.setApplication(application);
        trigger.setDeviceModel(deviceModel);
        trigger.setLocation(location);
        trigger.setMinutes(form.getMinutes());
        trigger.setDescription(form.getDescription());

        ServiceResponse<SilenceTrigger> updateResponse = silenceTriggerService.update(tenant, application, guid, trigger);

        if (!updateResponse.isOk()) {
            throw new BadServiceResponseException(user, updateResponse, validationsCode);
        }

    }

    @DeleteMapping(path = "/{deviceModelName}/{locationName}")
    @ApiOperation(value = "Delete a silence trigger")
    @PreAuthorize("hasAuthority('REMOVE_ALERT_TRIGGER')")
    public void delete(
            @PathVariable("application") String applicationId,
            @PathVariable("deviceModelName") String deviceModelName,
            @PathVariable("locationName") String locationName) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);
        DeviceModel deviceModel = getDeviceModel(tenant, application, deviceModelName);
        Location location = getLocation(tenant, application, locationName);
        String guid = getSilenceTriggerGuid(tenant, application, deviceModel, location);

        ServiceResponse<SilenceTrigger> restDestinationResponse = silenceTriggerService.remove(tenant, application, guid);

        if (!restDestinationResponse.isOk()) {
            throw new BadServiceResponseException(user, restDestinationResponse, validationsCode);
        }

    }

    private Location getLocation(Tenant tenant, Application application, String locationName) throws BadServiceResponseException, NotFoundResponseException {

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

    private String getSilenceTriggerGuid(Tenant tenant, Application application, DeviceModel deviceModel, Location location) throws BadServiceResponseException, NotFoundResponseException {

        ServiceResponse<SilenceTrigger> silenceTriggerResponse = silenceTriggerService.findByTenantAndApplicationAndModelAndLocation(tenant, application, deviceModel, location);

        if (!silenceTriggerResponse.isOk()) {
            if (silenceTriggerResponse.getResponseMessages().containsKey(SilenceTriggerService.Validations.SILENCE_TRIGGER_NOT_FOUND.getCode())) {
                throw new NotFoundResponseException(user, silenceTriggerResponse);

            } else {
                Set<String> validationsCode = new HashSet<>();
                for (LocationService.Validations value : LocationService.Validations.values()) {
                    validationsCode.add(value.getCode());
                }

                throw new BadServiceResponseException(user, silenceTriggerResponse, validationsCode);
            }
        }

        return silenceTriggerResponse.getResult().getGuid();

    }

    private DeviceModel getDeviceModel(Tenant tenant, Application application, String deviceModelName) throws BadServiceResponseException, NotFoundResponseException {

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

    @Override
    public void afterPropertiesSet() throws Exception {
        for (Validations value : Validations.values()) {
            validationsCode.add(value.getCode());
        }

        for (SilenceTriggerService.Validations value : SilenceTriggerService.Validations.values()) {
            validationsCode.add(value.getCode());
        }

        for (SilenceTrigger.Validations value : SilenceTrigger.Validations.values()) {
            validationsCode.add(value.getCode());
        }
    }

}
