package com.konkerlabs.platform.registry.api.web.controller;

import java.util.HashSet;
import java.util.List;
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
import com.konkerlabs.platform.registry.api.model.DeviceConfigVO;
import com.konkerlabs.platform.registry.api.model.RestResponse;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.DeviceConfig;
import com.konkerlabs.platform.registry.business.model.DeviceConfigSetup;
import com.konkerlabs.platform.registry.business.model.DeviceModel;
import com.konkerlabs.platform.registry.business.model.Location;
import com.konkerlabs.platform.registry.business.model.RestDestination.Validations;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.DeviceConfigSetupService;
import com.konkerlabs.platform.registry.business.services.api.DeviceModelService;
import com.konkerlabs.platform.registry.business.services.api.LocationService;
import com.konkerlabs.platform.registry.business.services.api.RestDestinationService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@Scope("request")
@RequestMapping(value = "/{application}/deviceConfigs")
@Api(tags = "device configs")
public class DeviceConfigController extends AbstractRestController implements InitializingBean {

    @Autowired
    private DeviceConfigSetupService deviceConfigSetupService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private DeviceModelService deviceModelService;

    private Set<String> validationsCode = new HashSet<>();

    @GetMapping(path = "/")
    // @PreAuthorize("hasAuthority('LIST_DEVICE_CONFIGS')")
    @ApiOperation(
            value = "List all device configs by application",
            response = DeviceConfigVO.class)
    public List<DeviceConfigVO> list(@PathVariable("application") String applicationId) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        ServiceResponse<List<DeviceConfig>> restDestinationResponse = deviceConfigSetupService.findAll(tenant, application);

        if (!restDestinationResponse.isOk()) {
            throw new BadServiceResponseException(user, restDestinationResponse, validationsCode);
        } else {
            return new DeviceConfigVO().apply(restDestinationResponse.getResult());
        }

    }

    @GetMapping(path = "/{deviceModelName}/{locationName}")
    @ApiOperation(
            value = "Get a device config by guid",
            response = RestResponse.class
    )
    // @PreAuthorize("hasAuthority('SHOW_DEVICE_CONFIG')")
    public Object read(
            @PathVariable("application") String applicationId,
            @PathVariable("deviceModelName") String deviceModelName,
            @PathVariable("locationName") String locationName,
            @PathVariable("restDestinationGuid") String restDestinationGuid) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);
        DeviceModel deviceModel = getDeviceModel(tenant, application, deviceModelName);
        Location location = getLocation(tenant, application, locationName);

        ServiceResponse<String> restDestinationResponse = deviceConfigSetupService.findByModelAndLocation(tenant, application, deviceModel, location);

        if (!restDestinationResponse.isOk()) {
            throw new NotFoundResponseException(user, restDestinationResponse);
        } else {
            return restDestinationResponse.getResult();
        }

    }

    @PostMapping(path = "/{deviceModelName}/{locationName}")
    @ApiOperation(value = "Create a device config")
    // @PreAuthorize("hasAuthority('CREATE_DEVICE_CONFIG')")
    public DeviceConfigVO create(
            @PathVariable("application") String applicationId,
            @PathVariable("deviceModelName") String deviceModelName,
            @PathVariable("locationName") String locationName,
            @ApiParam(
            		name = "body",
            		value = "JSON filled with the fields described in Model and Example Value beside",
            		required = true)
            @RequestBody String json) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);
        DeviceModel deviceModel = getDeviceModel(tenant, application, deviceModelName);
        Location location = getLocation(tenant, application, locationName);

        ServiceResponse<DeviceConfig> restDestinationResponse = deviceConfigSetupService.save(tenant, application, deviceModel, location, json);

        if (!restDestinationResponse.isOk()) {
            throw new BadServiceResponseException(user, restDestinationResponse, validationsCode);
        } else {
            return new DeviceConfigVO().apply(restDestinationResponse.getResult());
        }

    }

    @PutMapping(path = "/{deviceModelName}/{locationName}")
    @ApiOperation(value = "Update a device config")
    // @PreAuthorize("hasAuthority('EDIT_DEVICE_CONFIG')")
    public void update(
            @PathVariable("application") String applicationId,
            @PathVariable("deviceModelName") String deviceModelName,
            @PathVariable("locationName") String locationName,
            @ApiParam(
            		name = "body",
            		value = "JSON filled with the fields described in Model and Example Value beside",
            		required = true)
            @RequestBody String json) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);
        DeviceModel deviceModel = getDeviceModel(tenant, application, deviceModelName);
        Location location = getLocation(tenant, application, locationName);

        ServiceResponse<DeviceConfig> updateResponse = deviceConfigSetupService.update(tenant, application, deviceModel, location, json);

        if (!updateResponse.isOk()) {
            throw new BadServiceResponseException(user, updateResponse, validationsCode);
        }

    }

    @DeleteMapping(path = "/{deviceModelName}/{locationName}")
    @ApiOperation(value = "Delete a device config")
    // @PreAuthorize("hasAuthority('REMOVE_DEVICE_CONFIG')")
    public void delete(
            @PathVariable("application") String applicationId,
            @PathVariable("deviceModelName") String deviceModelName,
            @PathVariable("locationName") String locationName) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);
        DeviceModel deviceModel = getDeviceModel(tenant, application, deviceModelName);
        Location location = getLocation(tenant, application, locationName);

        ServiceResponse<DeviceConfigSetup> restDestinationResponse = deviceConfigSetupService.remove(tenant, application, deviceModel, location);

        if (!restDestinationResponse.isOk()) {
            if (restDestinationResponse.getResponseMessages().containsKey(RestDestinationService.Validations.DESTINATION_NOT_FOUND.getCode())) {
                throw new NotFoundResponseException(user, restDestinationResponse);
            } else {
                throw new BadServiceResponseException(user, restDestinationResponse, validationsCode);
            }
        }

    }

    private Location getLocation(Tenant tenant, Application application, String locationName) throws BadServiceResponseException, NotFoundResponseException {

        ServiceResponse<Location> applicationResponse = locationService.findByName(tenant, application, locationName, false);
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

    private DeviceModel getDeviceModel(Tenant tenant, Application application, String deviceModelName) throws BadServiceResponseException, NotFoundResponseException {

        ServiceResponse<DeviceModel> applicationResponse = deviceModelService.getByTenantIdApplicationNameAndName(tenant, application, deviceModelName);
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

        for (RestDestinationService.Validations value : RestDestinationService.Validations.values()) {
            validationsCode.add(value.getCode());
        }

    }

}
