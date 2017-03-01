package com.konkerlabs.platform.registry.api.web.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.konkerlabs.platform.registry.api.model.DeviceVO;
import com.konkerlabs.platform.registry.api.model.RestResponse;
import com.konkerlabs.platform.registry.api.model.RestResponseBuilder;
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
        value = "/devices",
        consumes = {MediaType.APPLICATION_JSON_VALUE},
        produces = {MediaType.APPLICATION_JSON_VALUE}
)
public class DeviceRestController {

    @Autowired
    private DeviceRegisterService deviceRegisterService;

    @Autowired
    private User user;

    @Autowired
    private MessageSource messageSource;

    @GetMapping(path = "/")
    @PreAuthorize("hasAuthority('LIST_DEVICES')")
    @ApiOperation(
            value = "List all devices by tenant",
            response = DeviceVO.class)
    public ResponseEntity<?> list() {

        Tenant tenant = user.getTenant();

        ServiceResponse<List<Device>> deviceResponse = deviceRegisterService.findAll(tenant);

        if (!deviceResponse.isOk()) {
            return createErrorResponse(deviceResponse);
        } else {
            List<DeviceVO> listVO = new ArrayList<>();
            for (Device device: deviceResponse.getResult()) {
                listVO.add(new DeviceVO(device));
            }
            return RestResponseBuilder.ok()
                    .withHttpStatus(HttpStatus.OK)
                    .withResult(listVO)
                    .build();
        }

    }

    @GetMapping(path = "/{deviceGuid}")
    @ApiOperation(
            value = "Get  a device by guid",
            response = RestResponse.class
    )
    @PreAuthorize("hasAuthority('SHOW_DEVICE')")
    public ResponseEntity<?> read(@PathVariable("deviceGuid") String deviceGuid) {

        Tenant tenant = user.getTenant();

        ServiceResponse<Device> deviceResponse = deviceRegisterService.getByDeviceGuid(tenant, deviceGuid);

        if (!deviceResponse.isOk()) {
            return createErrorResponse(deviceResponse);
        } else {
            DeviceVO obj = new DeviceVO(deviceResponse.getResult());
            return RestResponseBuilder.ok()
                    .withHttpStatus(HttpStatus.OK)
                    .withResult(obj)
                    .build();
        }

    }

    @PostMapping
    @ApiOperation(value = "Create a device")
    @PreAuthorize("hasAuthority('ADD_DEVICE')")
    public ResponseEntity<?> create(@RequestBody DeviceVO deviceForm) {

        Tenant tenant = user.getTenant();

        Device device = Device.builder()
                .name(deviceForm.getName())
                .deviceId(deviceForm.getId())
                .description(deviceForm.getDescription())
                .active(true)
                .build();

        ServiceResponse<Device> deviceResponse = deviceRegisterService.register(tenant, device);

        if (!deviceResponse.isOk()) {
            return createErrorResponse(deviceResponse);
        } else {
            return RestResponseBuilder.<DeviceVO>ok()
                    .withHttpStatus(HttpStatus.CREATED)
                    .withMessages(getMessages(deviceResponse))
                    .withResult(new DeviceVO(deviceResponse.getResult()))
                    .build();
        }

    }

    @PutMapping(path = "/{deviceGuid}")
    @ApiOperation(value = "Update a device")
    @PreAuthorize("hasAuthority('EDIT_DEVICE')")
    public ResponseEntity<?> update(@PathVariable("deviceGuid") String deviceGuid, @RequestBody DeviceVO deviceForm) {

        Tenant tenant = user.getTenant();

        Device deviceFromDB = null;
        ServiceResponse<Device> deviceResponse = deviceRegisterService.getByDeviceGuid(tenant, deviceGuid);

        if (!deviceResponse.isOk()) {
            return createErrorResponse(deviceResponse);
        } else {
            deviceFromDB = deviceResponse.getResult();
        }

        // update fields
        deviceFromDB.setName(deviceForm.getName());
        deviceFromDB.setDescription(deviceForm.getDescription());
        deviceFromDB.setActive(deviceForm.isActive());

        ServiceResponse<Device> updateResponse = deviceRegisterService.update(tenant, deviceGuid, deviceFromDB);

        if (!updateResponse.isOk()) {
            return createErrorResponse(deviceResponse);

        } else {
            return RestResponseBuilder.ok()
                    .withHttpStatus(HttpStatus.OK)
                    .withMessages(getMessages(deviceResponse))
                    .build();
        }

    }

    @DeleteMapping(path = "/{deviceGuid}")
    @ApiOperation(value = "Delete a device")
    @PreAuthorize("hasAuthority('REMOVE_DEVICE')")
    public ResponseEntity<?> delete(@PathVariable("deviceGuid") String deviceGuid) {

        Tenant tenant = user.getTenant();

        ServiceResponse<Device> deviceResponse = deviceRegisterService.remove(tenant, deviceGuid);

        if (!deviceResponse.isOk()) {
            return createErrorResponse(deviceResponse);
        } else {
            return RestResponseBuilder.ok()
                    .withHttpStatus(HttpStatus.NO_CONTENT)
                    .withMessages(getMessages(deviceResponse))
                    .build();
        }

    }

    private List<String> getMessages(ServiceResponse<?> serviceResponse) {
        List<String> messages = serviceResponse.getResponseMessages().entrySet().stream()
                .map(v -> messageSource.getMessage(v.getKey(), v.getValue(), user.getLanguage().getLocale()))
                .collect(Collectors.toList());

        return messages;
    }

    private ResponseEntity<?> createErrorResponse(ServiceResponse<?> serviceResponse) {

        if (containsValidations(serviceResponse)) {

            return RestResponseBuilder.error()
                    .withHttpStatus(HttpStatus.BAD_REQUEST)
                    .withMessages(getMessages(serviceResponse))
                    .build();
        } else {

            return RestResponseBuilder.error()
                                      .withHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                                      .withMessages(getMessages(serviceResponse))
                                      .build();
        }

    }

    private boolean containsValidations(ServiceResponse<?> deviceResponse) {

        Map<String, Object[]> responseMessages = deviceResponse.getResponseMessages();

        for (Validations value : DeviceRegisterService.Validations.values()) {
            if (responseMessages.containsKey(value.getCode())) {
                return true;
            }
        }

        return false;
    }

}
