package com.konkerlabs.platform.registry.api.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.UserService;

@RestController
@RequestMapping(value = "/devices")
public class DeviceRestController {

    @Autowired
    private DeviceRegisterService deviceRegisterService;

    @DeleteMapping(path = "/{deviceGuid}")
    public ResponseEntity delete(@PathVariable("deviceGuid") String deviceGuid) {

        ServiceResponse<Device> deviceResponse = deviceRegisterService.getByDeviceGuid(null, deviceGuid);

        if (!deviceResponse.isOk()) {
            return new ResponseEntity(deviceResponse.getResponseMessages(), HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity(deviceResponse.getResult(), HttpStatus.OK);
        }

    }

    @PostMapping
    public ResponseEntity create(@RequestBody Device device) {

        ServiceResponse<Device> deviceResponse = deviceRegisterService.register(null, device);

        if (!deviceResponse.isOk()) {
            return new ResponseEntity(deviceResponse.getResponseMessages(), HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity(deviceResponse.getResult(), HttpStatus.OK);
        }

    }

    @PutMapping(path = "/{deviceGuid}")
    public ResponseEntity update(@PathVariable("deviceGuid") String deviceGuid) {

        ServiceResponse<Device> deviceResponse = deviceRegisterService.getByDeviceGuid(null, deviceGuid);

        if (!deviceResponse.isOk()) {
            return new ResponseEntity(deviceResponse.getResponseMessages(), HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity(deviceResponse.getResult(), HttpStatus.OK);
        }

    }

    @GetMapping(path = "/{deviceGuid}")
    public ResponseEntity read(@PathVariable("deviceGuid") String deviceGuid) {

        Tenant tenant = null;
        
        ServiceResponse<Device> deviceResponse = deviceRegisterService.getByDeviceGuid(tenant, deviceGuid);

        if (!deviceResponse.isOk()) {
            return new ResponseEntity(deviceResponse.getResponseMessages(), HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity(deviceResponse.getResult(), HttpStatus.OK);
        }

    }

}
