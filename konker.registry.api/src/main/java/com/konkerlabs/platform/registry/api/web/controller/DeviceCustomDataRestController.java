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
import com.konkerlabs.platform.registry.api.model.RestResponse;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.DeviceCustomData;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.DeviceCustomDataService;
import com.konkerlabs.platform.registry.business.services.api.DeviceModelService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.mongodb.util.JSON;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@Scope("request")
@RequestMapping(
        value = "/{application}/devices/{deviceGuid}/customData"
)
@Api(tags = "devices custom data")
public class DeviceCustomDataRestController extends AbstractRestController implements InitializingBean {

    @Autowired
    private DeviceRegisterService deviceRegisterService;

    @Autowired
    private DeviceCustomDataService deviceCustomDataService;

    private Set<String> validationsCode = new HashSet<>();

    @GetMapping
    @ApiOperation(
            value = "Get a custom data by device guid",
            response = RestResponse.class
    )
    @PreAuthorize("hasAuthority('SHOW_DEVICE')")
    public Object read(
    		@PathVariable("application") String applicationId,
    		@PathVariable("deviceGuid") String deviceGuid) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);
        Device device = getDevice(tenant, application, deviceGuid);

        ServiceResponse<DeviceCustomData> deviceResponse = deviceCustomDataService.getByTenantApplicationAndDevice(tenant, application, device);

        if (!deviceResponse.isOk()) {
            throw new NotFoundResponseException(deviceResponse);
        } else {
            return JSON.parse(deviceResponse.getResult().getJson());
        }

    }

    @PostMapping
    @ApiOperation(value = "Create a device custom data")
    @PreAuthorize("hasAuthority('ADD_DEVICE')")
    public Object create(
    		@PathVariable("application") String applicationId,
    		@PathVariable("deviceGuid") String deviceGuid,
            @ApiParam(name = "body", required = true)
    		@RequestBody String jsonCustomData) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);
        Device device = getDevice(tenant, application, deviceGuid);

        ServiceResponse<DeviceCustomData> deviceResponse = deviceCustomDataService.save(tenant, application, device, jsonCustomData);

        if (!deviceResponse.isOk()) {
            throw new BadServiceResponseException( deviceResponse, validationsCode);
        } else {
            return JSON.parse(deviceResponse.getResult().getJson());
        }

    }

    @PutMapping
    @ApiOperation(value = "Update a device custom data")
    @PreAuthorize("hasAuthority('EDIT_DEVICE')")
    public void update(
    		@PathVariable("application") String applicationId,
            @PathVariable("deviceGuid") String deviceGuid,
            @ApiParam(name = "body", required = true)
            @RequestBody String jsonCustomData) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);
        Device device = getDevice(tenant, application, deviceGuid);

        ServiceResponse<DeviceCustomData> updateResponse = deviceCustomDataService.update(tenant, application, device, jsonCustomData);

        if (!updateResponse.isOk()) {
            throw new BadServiceResponseException( updateResponse, validationsCode);

        }

    }

    @DeleteMapping
    @ApiOperation(value = "Delete a device custom data")
    @PreAuthorize("hasAuthority('REMOVE_DEVICE')")
    public void delete(
    		@PathVariable("application") String applicationId,
    		@PathVariable("deviceGuid") String deviceGuid) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);
        Device device = getDevice(tenant, application, deviceGuid);

        ServiceResponse<DeviceCustomData> deviceResponse = deviceCustomDataService.remove(tenant, application, device);

        if (!deviceResponse.isOk()) {
            if (deviceResponse.getResponseMessages().containsKey(DeviceCustomDataService.Validations.DEVICE_CUSTOM_DATA_DOES_NOT_EXIST.getCode())) {
                throw new NotFoundResponseException(deviceResponse);
            } else {
                throw new BadServiceResponseException( deviceResponse, validationsCode);
            }
        }

    }

    private Device getDevice(Tenant tenant, Application application, String deviceGuid) throws BadServiceResponseException, NotFoundResponseException {

        ServiceResponse<Device> applicationResponse = deviceRegisterService.getByDeviceGuid(tenant, application, deviceGuid);
        if (!applicationResponse.isOk()) {
            if (applicationResponse.getResponseMessages().containsKey(DeviceRegisterService.Validations.DEVICE_GUID_DOES_NOT_EXIST.getCode())) {
                throw new NotFoundResponseException(applicationResponse);

            } else {
                Set<String> validationsCode = new HashSet<>();
                for (DeviceModelService.Validations value : DeviceModelService.Validations.values()) {
                    validationsCode.add(value.getCode());
                }

                throw new BadServiceResponseException( applicationResponse, validationsCode);
            }
        }

        return applicationResponse.getResult();

    }

    @Override
    public void afterPropertiesSet() throws Exception {

        for (com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService.Validations value : DeviceRegisterService.Validations.values()) {
            validationsCode.add(value.getCode());
        }

        for (com.konkerlabs.platform.registry.business.model.Device.Validations value : Device.Validations.values()) {
            validationsCode.add(value.getCode());
        }

    }

}
