package com.konkerlabs.platform.registry.api.web.controller;

import java.util.ArrayList;
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
import com.konkerlabs.platform.registry.api.model.DeviceVO;
import com.konkerlabs.platform.registry.api.model.RestDestinationInputVO;
import com.konkerlabs.platform.registry.api.model.RestDestinationVO;
import com.konkerlabs.platform.registry.api.model.RestResponse;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.RestDestination;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService.Validations;
import com.konkerlabs.platform.registry.business.services.api.RestDestinationService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@Scope("request")
@RequestMapping(value = "/restDestinations")
@Api(tags = "restDestinations")
public class RestDestinationController implements InitializingBean {

    @Autowired
    private RestDestinationService restDestinationService;

    @Autowired
    private User user;

    private Set<String> validationsCode = new HashSet<>();

    @GetMapping(path = "/")
    @PreAuthorize("hasAuthority('LIST_DEVICES')")
    @ApiOperation(
            value = "List all devices by organization",
            response = DeviceVO.class)
    public List<RestDestinationVO> list() throws BadServiceResponseException {

        Tenant tenant = user.getTenant();

        ServiceResponse<List<RestDestination>> deviceResponse = restDestinationService.findAll(tenant);

        if (!deviceResponse.isOk()) {
            throw new BadServiceResponseException(user, deviceResponse, validationsCode);
        } else {
            List<RestDestinationVO> listVO = new ArrayList<>();
            for (RestDestination device: deviceResponse.getResult()) {
                listVO.add(new RestDestinationVO(device));
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
    public RestDestinationVO read(@PathVariable("deviceGuid") String deviceGuid) throws BadServiceResponseException {

        Tenant tenant = user.getTenant();

        ServiceResponse<RestDestination> deviceResponse = restDestinationService.getByGUID(tenant, deviceGuid);

        if (!deviceResponse.isOk()) {
            return null;
        } else {
            return new RestDestinationVO(deviceResponse.getResult());
        }

    }

    @PostMapping
    @ApiOperation(value = "Create a device")
    @PreAuthorize("hasAuthority('ADD_DEVICE')")
    public RestDestinationVO create(
            @ApiParam(name = "body", required = true)
            @RequestBody RestDestinationInputVO restDestinationForm) throws BadServiceResponseException {

        Tenant tenant = user.getTenant();

        RestDestination restDestination = RestDestination.builder()
                .name(restDestinationForm.getName())
                .method(restDestinationForm.getMethod())
                .headers(restDestinationForm.getHeaders())
                .serviceURI(restDestinationForm.getServiceURI())
                .serviceUsername(restDestinationForm.getServiceUserName())
                .servicePassword(restDestinationForm.getServicePassword())
                .active(true)
                .build();

        ServiceResponse<RestDestination> restDestinationResponse = restDestinationService.register(tenant, restDestination);

        if (!restDestinationResponse.isOk()) {
            throw new BadServiceResponseException(user, restDestinationResponse, validationsCode);
        } else {
            return new RestDestinationVO(restDestinationResponse.getResult());
        }

    }

    @PutMapping(path = "/{deviceGuid}")
    @ApiOperation(value = "Update a device")
    @PreAuthorize("hasAuthority('EDIT_DEVICE')")
    public void update(
            @PathVariable("deviceGuid") String deviceGuid,
            @ApiParam(name = "body", required = true)
            @RequestBody RestDestinationInputVO deviceForm) throws BadServiceResponseException {

        Tenant tenant = user.getTenant();

        RestDestination restDestinationFromDB = null;
        ServiceResponse<RestDestination> deviceResponse = restDestinationService.getByGUID(tenant, deviceGuid);

        if (!deviceResponse.isOk()) {
            throw new BadServiceResponseException(user, deviceResponse, validationsCode);
        } else {
            restDestinationFromDB = deviceResponse.getResult();
        }

        // update fields
        restDestinationFromDB.setName(deviceForm.getName());
        restDestinationFromDB.setMethod(deviceForm.getMethod());
        restDestinationFromDB.setHeaders(deviceForm.getHeaders());
        restDestinationFromDB.setServiceURI(deviceForm.getServiceURI());
        restDestinationFromDB.setServiceUsername(deviceForm.getServiceUserName());
        restDestinationFromDB.setServicePassword(deviceForm.getServicePassword());
        restDestinationFromDB.setActive(deviceForm.isActive());

        ServiceResponse<RestDestination> updateResponse = restDestinationService.update(tenant, deviceGuid, restDestinationFromDB);

        if (!updateResponse.isOk()) {
            throw new BadServiceResponseException(user, deviceResponse, validationsCode);

        }

    }

    @DeleteMapping(path = "/{deviceGuid}")
    @ApiOperation(value = "Delete a device")
    @PreAuthorize("hasAuthority('REMOVE_DEVICE')")
    public void delete(@PathVariable("deviceGuid") String deviceGuid) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();

        ServiceResponse<RestDestination> restDestinationResponse = restDestinationService.remove(tenant, deviceGuid);

        if (!restDestinationResponse.isOk()) {
            if (restDestinationResponse.getResponseMessages().containsKey(Validations.DEVICE_GUID_DOES_NOT_EXIST.getCode())) {
                throw new NotFoundResponseException(user, restDestinationResponse);
            } else {
                throw new BadServiceResponseException(user, restDestinationResponse, validationsCode);
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
