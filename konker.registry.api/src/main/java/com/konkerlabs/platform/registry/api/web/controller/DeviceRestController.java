package com.konkerlabs.platform.registry.api.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.konkerlabs.platform.registry.api.model.DeviceRest;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;

@RestController
@Scope("request")
@RequestMapping(value = "/devices")
public class DeviceRestController {

    @Autowired
    private DeviceRegisterService deviceRegisterService;

    @Autowired
    private User user;

    @GetMapping(path = "/{deviceGuid}")
    public ResponseEntity read(@PathVariable("deviceGuid") String deviceGuid) {

        Tenant tenant = user.getTenant();

        ServiceResponse<Device> deviceResponse = deviceRegisterService.getByDeviceGuid(tenant, deviceGuid);

        if (!deviceResponse.isOk()) {
            return new ResponseEntity(deviceResponse.getResponseMessages(), HttpStatus.NOT_FOUND);
        } else {
            DeviceRest obj = new DeviceRest(deviceResponse.getResult());
            return new ResponseEntity(obj, HttpStatus.OK);
        }

    }

    @PostMapping
    public ResponseEntity create(@RequestBody DeviceRest deviceForm) {

        Tenant tenant = user.getTenant();

        Device device = Device.builder().name(deviceForm.getName()).deviceId(deviceForm.getId())
                .description(deviceForm.getDescription()).build();

        ServiceResponse<Device> deviceResponse = deviceRegisterService.register(tenant, device);

        if (!deviceResponse.isOk()) {
            return new ResponseEntity(deviceResponse.getResponseMessages(), HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity(new DeviceRest(deviceResponse.getResult()), HttpStatus.OK);
        }

    }

    @PutMapping(path = "/{deviceGuid}")
    public ResponseEntity update(@PathVariable("deviceGuid") String deviceGuid, @RequestBody DeviceRest deviceForm) {

        Tenant tenant = user.getTenant();

        Device deviceFromDB = null;
        ServiceResponse<Device> deviceResponse = deviceRegisterService.getByDeviceGuid(tenant, deviceGuid);

        if (!deviceResponse.isOk()) {
            return new ResponseEntity(deviceResponse.getResponseMessages(), HttpStatus.NOT_FOUND);
        } else {
            deviceFromDB = deviceResponse.getResult();
        }

        if (StringUtils.isNotBlank(deviceForm.getName())) {
            deviceFromDB.setName(deviceForm.getName());
        }

        ServiceResponse<Device> updateResponse = deviceRegisterService.update(tenant, deviceGuid, deviceFromDB);

        if (!updateResponse.isOk()) {
            return new ResponseEntity(updateResponse.getResponseMessages(), HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity(new DeviceRest(deviceResponse.getResult()), HttpStatus.OK);
        }

    }

    @DeleteMapping(path = "/{deviceGuid}")
    public ResponseEntity delete(@PathVariable("deviceGuid") String deviceGuid) {

        Tenant tenant = user.getTenant();

        ServiceResponse<Device> deviceResponse = deviceRegisterService.remove(tenant, deviceGuid);

        if (!deviceResponse.isOk()) {
            return new ResponseEntity(deviceResponse.getResponseMessages(), HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity(HttpStatus.OK);
        }

    }

}
