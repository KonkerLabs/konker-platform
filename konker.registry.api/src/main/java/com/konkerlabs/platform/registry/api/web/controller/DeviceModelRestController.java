package com.konkerlabs.platform.registry.api.web.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.konkerlabs.platform.registry.api.exceptions.BadServiceResponseException;
import com.konkerlabs.platform.registry.api.exceptions.NotFoundResponseException;
import com.konkerlabs.platform.registry.api.model.DeviceModelInputVO;
import com.konkerlabs.platform.registry.api.model.DeviceModelVO;
import com.konkerlabs.platform.registry.api.model.DeviceVO;
import com.konkerlabs.platform.registry.api.model.RestResponse;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.DeviceModel;
import com.konkerlabs.platform.registry.business.model.DeviceModel.Validations;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.DeviceModelService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@Scope("request")
@RequestMapping(value = "/{application}/deviceModels")
@Api(tags = "device models")
public class DeviceModelRestController extends AbstractRestController implements InitializingBean {

    private final DeviceModelService deviceModelService;

    private Set<String> validationsCode = new HashSet<>();

    @Autowired
    public DeviceModelRestController(DeviceModelService deviceModelService) {
        this.deviceModelService = deviceModelService;
    }

    @GetMapping(path = "/")
    @PreAuthorize("hasAuthority('LIST_DEVICE_MODEL')")
    @ApiOperation(
            value = "List all device models by application",
            response = DeviceModelVO.class)
    public List<DeviceModelVO> list(@PathVariable("application") String applicationId,
                                    @ApiParam(value = "Page number")
                                    @RequestParam(required = false, defaultValue = "0") int page,
                                    @ApiParam(value = "Number of elements per page")
                                    @RequestParam(required = false, defaultValue = "500") int size) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        ServiceResponse<Page<DeviceModel>> deviceModelResponse = deviceModelService.findAll(tenant, application, page, size);

        if (!deviceModelResponse.isOk()) {
            throw new BadServiceResponseException( deviceModelResponse, validationsCode);
        } else {
            return new DeviceModelVO().apply(deviceModelResponse.getResult().getContent());
        }

    }

    @GetMapping(path = "/{deviceModelName}")
    @ApiOperation(
            value = "Get a device model by name",
            response = RestResponse.class
    )
    @PreAuthorize("hasAuthority('SHOW_DEVICE_MODEL')")
    public DeviceModelVO read(
            @PathVariable("application") String applicationId,
            @PathVariable("deviceModelName") String deviceModelName) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        ServiceResponse<DeviceModel> deviceModelResponse = deviceModelService.getByTenantApplicationAndName(tenant, application, deviceModelName);

        if (!deviceModelResponse.isOk()) {
            throw new NotFoundResponseException(deviceModelResponse);
        } else {
            return new DeviceModelVO().apply(deviceModelResponse.getResult());
        }

    }

    @GetMapping(path = "/{deviceModelName}/devices")
    @ApiOperation(
            value = "List all devices of a device model",
            response = RestResponse.class
    )

    @PreAuthorize("hasAuthority('SHOW_DEVICE_MODEL')")
    public List<DeviceVO> devices(
            @PathVariable("application") String applicationId,
            @PathVariable("deviceModelName") String deviceModelName) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        ServiceResponse<List<Device>> deviceModelResponse = deviceModelService.listDevicesByDeviceModelName(tenant, application, deviceModelName);

        if (!deviceModelResponse.isOk()) {
            throw new BadServiceResponseException( deviceModelResponse, validationsCode);
        } else {
            return new DeviceVO().apply(deviceModelResponse.getResult());
        }

    }

    @PostMapping
    @ApiOperation(value = "Create a device model")
    @PreAuthorize("hasAuthority('CREATE_DEVICE_MODEL')")
    public DeviceModelVO create(
            @PathVariable("application") String applicationId,
            @ApiParam(
            		name = "body",
            		value = "JSON filled with the fields described in Model and Example Value beside",
            		required = true)
            @RequestBody DeviceModelInputVO deviceModelForm) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        DeviceModel deviceModel =
                DeviceModel.builder()
                .name(deviceModelForm.getName())
                .description(deviceModelForm.getDescription())
                .contentType(DeviceModel.ContentType.getByValue(deviceModelForm.getContentType()))
                .defaultModel(deviceModelForm.isDefaultModel())
                .build();

        ServiceResponse<DeviceModel> deviceModelResponse = deviceModelService.register(tenant, application, deviceModel);

        if (!deviceModelResponse.isOk()) {
            throw new BadServiceResponseException( deviceModelResponse, validationsCode);
        } else {
            return new DeviceModelVO().apply(deviceModelResponse.getResult());
        }

    }

    @PutMapping(path = "/{deviceModelName}")
    @ApiOperation(value = "Update a device model")
    @PreAuthorize("hasAuthority('EDIT_DEVICE_MODEL')")
    public void update(
            @PathVariable("application") String applicationId,
            @PathVariable("deviceModelName") String deviceModelName,
            @ApiParam(
            		name = "body",
            		value = "JSON filled with the fields described in Model and Example Value beside",
            		required = true)
            @RequestBody DeviceModelInputVO deviceModelForm) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        DeviceModel deviceModelFromDB;
        ServiceResponse<DeviceModel> deviceModelResponse = deviceModelService.getByTenantApplicationAndName(tenant, application, deviceModelName);

        if (!deviceModelResponse.isOk()) {
            throw new BadServiceResponseException( deviceModelResponse, validationsCode);
        } else {
            deviceModelFromDB = deviceModelResponse.getResult();
        }

        // update fields
        deviceModelFromDB.setName(deviceModelForm.getName());
        deviceModelFromDB.setDescription(deviceModelForm.getDescription());
        deviceModelFromDB.setContentType(DeviceModel.ContentType.getByValue(deviceModelForm.getContentType()));
        deviceModelFromDB.setDefaultModel(deviceModelForm.isDefaultModel());

        ServiceResponse<DeviceModel> updateResponse = deviceModelService.update(tenant, application, deviceModelName, deviceModelFromDB);

        if (!updateResponse.isOk()) {
            throw new BadServiceResponseException( updateResponse, validationsCode);
        }

    }

    @DeleteMapping(path = "/{deviceModelName}")
    @ApiOperation(value = "Delete a device model")
    @PreAuthorize("hasAuthority('REMOVE_DEVICE_MODEL')")
    public void delete(
            @PathVariable("application") String applicationId,
            @PathVariable("deviceModelName") String deviceModelName) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        ServiceResponse<DeviceModel> deviceModelResponse = deviceModelService.remove(tenant, application, deviceModelName);

        if (!deviceModelResponse.isOk()) {
            if (deviceModelResponse.getResponseMessages().containsKey(DeviceModelService.Validations.DEVICE_MODEL_NOT_FOUND.getCode())) {
                throw new NotFoundResponseException(deviceModelResponse);
            } else {
                throw new BadServiceResponseException( deviceModelResponse, validationsCode);
            }
        }

    }

    @Override
    public void afterPropertiesSet() {
        for (Validations value : Validations.values()) {
            validationsCode.add(value.getCode());
        }

        for (DeviceModelService.Validations value : DeviceModelService.Validations.values()) {
            validationsCode.add(value.getCode());
        }

        for (CommonValidations value : CommonValidations.values()) {
            validationsCode.add(value.getCode());
        }
    }

}
