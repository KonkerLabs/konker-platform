package com.konkerlabs.platform.registry.api.web.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.konkerlabs.platform.registry.api.model.RestResponse;

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
import com.konkerlabs.platform.registry.api.model.DeviceVO;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService.Validations;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;

import io.swagger.annotations.ApiOperation;

@RestController
@Scope("request")
@RequestMapping(
        value = "/devices"
)
public class DeviceRestController implements InitializingBean {

    @Autowired
    private DeviceRegisterService deviceRegisterService;

    @Autowired
    private User user;

    private Set<String> validationsCode = new HashSet<>();

    @GetMapping(path = "/")
    @PreAuthorize("hasAuthority('LIST_DEVICES')")
    @ApiOperation(
            value = "List all devices by organization",
            response = DeviceVO.class)
    public List<DeviceVO> list() throws BadServiceResponseException {

        Tenant tenant = user.getTenant();

        ServiceResponse<List<Device>> deviceResponse = deviceRegisterService.findAll(tenant);

        if (!deviceResponse.isOk()) {
            throw new BadServiceResponseException(user, deviceResponse, validationsCode);
        } else {
            List<DeviceVO> listVO = new ArrayList<>();
            for (Device device: deviceResponse.getResult()) {
                listVO.add(new DeviceVO(device));
            }
            return listVO;
        }

    }

    @GetMapping(path = "/{deviceGuid}")
    @ApiOperation(
            value = "Get a device by guid",
            response = RestResponse.class
    )
    @PreAuthorize("hasAuthority('SHOW_DEVICE')")
    public DeviceVO read(@PathVariable("deviceGuid") String deviceGuid) throws BadServiceResponseException {

        Tenant tenant = user.getTenant();

        ServiceResponse<Device> deviceResponse = deviceRegisterService.getByDeviceGuid(tenant, deviceGuid);

        if (!deviceResponse.isOk()) {
            throw new BadServiceResponseException(user, deviceResponse, validationsCode);
        } else {
            return new DeviceVO(deviceResponse.getResult());
        }

    }

    @PostMapping
    @ApiOperation(value = "Create a device")
    @PreAuthorize("hasAuthority('ADD_DEVICE')")
    public DeviceVO create(@RequestBody DeviceVO deviceForm) throws BadServiceResponseException {

        Tenant tenant = user.getTenant();

        Device device = Device.builder()
                .name(deviceForm.getName())
                .deviceId(deviceForm.getId())
                .description(deviceForm.getDescription())
                .active(true)
                .build();

        ServiceResponse<Device> deviceResponse = deviceRegisterService.register(tenant, device);

        if (!deviceResponse.isOk()) {
            throw new BadServiceResponseException(user, deviceResponse, validationsCode);
        } else {
            return new DeviceVO(deviceResponse.getResult());
        }

    }

    @PutMapping(path = "/{deviceGuid}")
    @ApiOperation(value = "Update a device")
    @PreAuthorize("hasAuthority('EDIT_DEVICE')")
    public void update(@PathVariable("deviceGuid") String deviceGuid, @RequestBody DeviceVO deviceForm) throws BadServiceResponseException {

        Tenant tenant = user.getTenant();

        Device deviceFromDB = null;
        ServiceResponse<Device> deviceResponse = deviceRegisterService.getByDeviceGuid(tenant, deviceGuid);

        if (!deviceResponse.isOk()) {
            throw new BadServiceResponseException(user, deviceResponse, validationsCode);
        } else {
            deviceFromDB = deviceResponse.getResult();
        }

        // update fields
        deviceFromDB.setName(deviceForm.getName());
        deviceFromDB.setDescription(deviceForm.getDescription());
        deviceFromDB.setActive(deviceForm.isActive());

        ServiceResponse<Device> updateResponse = deviceRegisterService.update(tenant, deviceGuid, deviceFromDB);

        if (!updateResponse.isOk()) {
            throw new BadServiceResponseException(user, deviceResponse, validationsCode);

        }

    }

    @DeleteMapping(path = "/{deviceGuid}")
    @ApiOperation(value = "Delete a device")
    @PreAuthorize("hasAuthority('REMOVE_DEVICE')")
    public void delete(@PathVariable("deviceGuid") String deviceGuid) throws BadServiceResponseException {

        Tenant tenant = user.getTenant();

        ServiceResponse<Device> deviceResponse = deviceRegisterService.remove(tenant, deviceGuid);

        if (!deviceResponse.isOk()) {
            throw new BadServiceResponseException(user, deviceResponse, validationsCode);
        }

    }

    @Override
    public void afterPropertiesSet() throws Exception {

        for (Validations value : DeviceRegisterService.Validations.values()) {
            validationsCode.add(value.getCode());
        }

    }

}
