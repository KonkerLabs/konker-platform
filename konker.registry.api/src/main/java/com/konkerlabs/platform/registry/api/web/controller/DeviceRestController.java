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
import com.konkerlabs.platform.registry.api.model.DeviceInputVO;
import com.konkerlabs.platform.registry.api.model.DeviceVO;
import com.konkerlabs.platform.registry.api.model.RestResponse;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService.Validations;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@Scope("request")
@RequestMapping(
        value = "/{application}/devices"
)
@Api(tags = "devices")
public class DeviceRestController extends AbstractRestController implements InitializingBean {

    @Autowired
    private DeviceRegisterService deviceRegisterService;

    private Set<String> validationsCode = new HashSet<>();

    @GetMapping(path = "/")
    @PreAuthorize("hasAuthority('LIST_DEVICES')")
    @ApiOperation(
            value = "List all devices by application",
            response = DeviceVO.class)
    public List<DeviceVO> list(@PathVariable("application") String applicationId) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        ServiceResponse<List<Device>> deviceResponse = deviceRegisterService.findAll(tenant, application);

        if (!deviceResponse.isOk()) {
            throw new BadServiceResponseException(user, deviceResponse, validationsCode);
        } else {
            return new DeviceVO().apply(deviceResponse.getResult());
        }

    }

    @GetMapping(path = "/{deviceGuid}")
    @ApiOperation(
            value = "Get a device by guid",
            response = RestResponse.class
    )
    @PreAuthorize("hasAuthority('SHOW_DEVICE')")
    public DeviceVO read(
    		@PathVariable("application") String applicationId,
    		@PathVariable("deviceGuid") String deviceGuid) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        ServiceResponse<Device> deviceResponse = deviceRegisterService.getByDeviceGuid(tenant, application, deviceGuid);

        if (!deviceResponse.isOk()) {
            throw new NotFoundResponseException(user, deviceResponse);
        } else {
            return new DeviceVO().apply(deviceResponse.getResult());
        }

    }

    @PostMapping
    @ApiOperation(value = "Create a device")
    @PreAuthorize("hasAuthority('ADD_DEVICE')")
    public DeviceVO create(
    		@PathVariable("application") String applicationId,
            @ApiParam(name = "body", required = true)
            @RequestBody DeviceInputVO deviceForm) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        Device device = Device.builder()
                .name(deviceForm.getName())
                .deviceId(deviceForm.getId())
                .description(deviceForm.getDescription())
                .active(true)
                .build();

        ServiceResponse<Device> deviceResponse = deviceRegisterService.register(tenant, application, device);

        if (!deviceResponse.isOk()) {
            throw new BadServiceResponseException(user, deviceResponse, validationsCode);
        } else {
            return new DeviceVO().apply(deviceResponse.getResult());
        }

    }

    @PutMapping(path = "/{deviceGuid}")
    @ApiOperation(value = "Update a device")
    @PreAuthorize("hasAuthority('EDIT_DEVICE')")
    public void update(
    		@PathVariable("application") String applicationId,
            @PathVariable("deviceGuid") String deviceGuid,
            @ApiParam(name = "body", required = true)
            @RequestBody DeviceInputVO deviceForm) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        Device deviceFromDB = null;
        ServiceResponse<Device> deviceResponse = deviceRegisterService.getByDeviceGuid(tenant, application, deviceGuid);

        if (!deviceResponse.isOk()) {
            throw new BadServiceResponseException(user, deviceResponse, validationsCode);
        } else {
            deviceFromDB = deviceResponse.getResult();
        }

        // update fields
        deviceFromDB.setName(deviceForm.getName());
        deviceFromDB.setDescription(deviceForm.getDescription());
        deviceFromDB.setActive(deviceForm.isActive());

        ServiceResponse<Device> updateResponse = deviceRegisterService.update(tenant, application, deviceGuid, deviceFromDB);

        if (!updateResponse.isOk()) {
            throw new BadServiceResponseException(user, deviceResponse, validationsCode);

        }

    }

    @DeleteMapping(path = "/{deviceGuid}")
    @ApiOperation(value = "Delete a device")
    @PreAuthorize("hasAuthority('REMOVE_DEVICE')")
    public void delete(
    		@PathVariable("application") String applicationId,
    		@PathVariable("deviceGuid") String deviceGuid) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        ServiceResponse<Device> deviceResponse = deviceRegisterService.remove(tenant, application, deviceGuid);

        if (!deviceResponse.isOk()) {
            if (deviceResponse.getResponseMessages().containsKey(Validations.DEVICE_GUID_DOES_NOT_EXIST.getCode())) {
                throw new NotFoundResponseException(user, deviceResponse);
            } else {
                throw new BadServiceResponseException(user, deviceResponse, validationsCode);
            }
        }

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
