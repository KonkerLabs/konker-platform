package com.konkerlabs.platform.registry.api.web.controller;

import com.konkerlabs.platform.registry.api.exceptions.BadServiceResponseException;
import com.konkerlabs.platform.registry.api.exceptions.NotFoundResponseException;
import com.konkerlabs.platform.registry.api.model.DeviceConfigVO;
import com.konkerlabs.platform.registry.api.model.DeviceFirmwareUpdateInputVO;
import com.konkerlabs.platform.registry.api.model.DeviceFirmwareUpdateSuspendInputVO;
import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.services.api.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@Scope("request")
@RequestMapping(value = "/{application}/firmwareupdates/")
@Api(tags = "device firmwares")
public class DeviceFirmwareUpdateRestController extends AbstractRestController implements InitializingBean {

    @Autowired
    private DeviceFirmwareService deviceFirmwareService;

    @Autowired
    private DeviceFirmwareUpdateService deviceFirmwareUpdateService;

    @Autowired
    private DeviceRegisterService deviceRegisterService;

    @Autowired
    private DeviceModelService deviceModelService;

    private Set<String> validationsCode = new HashSet<>();

    @PostMapping
    @ApiOperation(value = "Create a device firmware update")
    @PreAuthorize("hasAuthority('CREATE_DEVICE_CONFIG')")
    public DeviceFirmwareUpdateInputVO create(
            @PathVariable("application") String applicationId,
            @RequestBody DeviceFirmwareUpdateInputVO deviceFirmwareUpdateForm
            ) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        ServiceResponse<Device> deviceServiceResponse = deviceRegisterService.getByDeviceGuid(tenant, application, deviceFirmwareUpdateForm.getDeviceGuid());
        if (!deviceServiceResponse.isOk()) {
            throw new NotFoundResponseException(deviceServiceResponse);
        }
        Device device = deviceServiceResponse.getResult();
        String firmwareVersion = deviceFirmwareUpdateForm.getVersion();

        ServiceResponse<DeviceFirmware> serviceFirmwareResponse = deviceFirmwareService.findByVersion(tenant, application, device.getDeviceModel(), firmwareVersion);
        if (!serviceFirmwareResponse.isOk()) {
            throw new BadServiceResponseException(serviceFirmwareResponse, validationsCode);
        }
        DeviceFirmware deviceFirmware = serviceFirmwareResponse.getResult();

        ServiceResponse<DeviceFwUpdate> serviceResponse = deviceFirmwareUpdateService.save(tenant, application, device, deviceFirmware);

        if (!serviceResponse.isOk()) {
            throw new BadServiceResponseException( serviceResponse, validationsCode);
        } else {
            return new DeviceFirmwareUpdateInputVO().apply(serviceResponse.getResult());
        }

    }


    @PutMapping(path = "/suspend")
    @ApiOperation(value = "Suspend a device firmware update")
    @PreAuthorize("hasAuthority('CREATE_DEVICE_CONFIG')")
    public DeviceFirmwareUpdateInputVO suspend(
            @PathVariable("application") String applicationId,
            @RequestParam(value = "deviceGuid", required = true) String deviceGuid,
            @RequestParam(value = "version", required = true) String version
    ) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        ServiceResponse<Device> deviceServiceResponse = deviceRegisterService.getByDeviceGuid(tenant, application, deviceGuid);
        if (!deviceServiceResponse.isOk()) {
            throw new NotFoundResponseException(deviceServiceResponse);
        }
        Device device = deviceServiceResponse.getResult();

        ServiceResponse<DeviceFirmware> serviceFirmwareResponse = deviceFirmwareService.findByVersion(tenant, application, device.getDeviceModel(), version);
        if (!serviceFirmwareResponse.isOk()) {
            throw new BadServiceResponseException(serviceFirmwareResponse, validationsCode);
        }

        ServiceResponse<DeviceFwUpdate> serviceResponse = deviceFirmwareUpdateService.setDeviceAsSuspended(tenant, application, device);

        if (!serviceResponse.isOk()) {
            throw new BadServiceResponseException( serviceResponse, validationsCode);
        } else {
            return new DeviceFirmwareUpdateInputVO().apply(serviceResponse.getResult());
        }

    }


    @GetMapping
    @ApiOperation(value = "List all device firmware updates of a version")
    @PreAuthorize("hasAuthority('CREATE_DEVICE_CONFIG')")
    public List<DeviceFirmwareUpdateInputVO> list(
            @PathVariable("application") String applicationId,
            @RequestParam(value = "deviceGuid", required = true) String deviceGuid,
            @RequestParam(value = "version", required = true) String version
    ) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        ServiceResponse<Device> deviceServiceResponse = deviceRegisterService.getByDeviceGuid(tenant, application, deviceGuid);
        if (!deviceServiceResponse.isOk()) {
            throw new NotFoundResponseException(deviceServiceResponse);
        }
        Device device = deviceServiceResponse.getResult();

        ServiceResponse<DeviceFirmware> serviceFirmwareResponse = deviceFirmwareService.findByVersion(tenant, application, device.getDeviceModel(), version);
        if (!serviceFirmwareResponse.isOk()) {
            throw new BadServiceResponseException(serviceFirmwareResponse, validationsCode);
        }

        DeviceFirmware deviceFirmware = serviceFirmwareResponse.getResult();

        ServiceResponse<List<DeviceFwUpdate>> serviceResponse = deviceFirmwareUpdateService.findByDeviceFirmware(tenant, application, deviceFirmware);

        if (!serviceResponse.isOk()) {
            throw new BadServiceResponseException( serviceResponse, validationsCode);
        } else {
            return new DeviceFirmwareUpdateInputVO().apply(serviceResponse.getResult());
        }

    }

    @Override
    public void afterPropertiesSet() {

        for (DeviceFirmware.Validations value : DeviceFirmware.Validations.values()) {
            validationsCode.add(value.getCode());
        }
        for (DeviceFirmwareService.Validations value : DeviceFirmwareService.Validations.values()) {
            validationsCode.add(value.getCode());
        }

    }

}
