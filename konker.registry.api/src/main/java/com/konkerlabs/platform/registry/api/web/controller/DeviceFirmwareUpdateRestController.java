package com.konkerlabs.platform.registry.api.web.controller;

import com.konkerlabs.platform.registry.api.exceptions.BadServiceResponseException;
import com.konkerlabs.platform.registry.api.exceptions.NotFoundResponseException;
import com.konkerlabs.platform.registry.api.model.*;
import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.services.api.DeviceFirmwareService;
import com.konkerlabs.platform.registry.business.services.api.DeviceFirmwareUpdateService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.codec.digest.DigestUtils;
import org.bson.types.Binary;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

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

    private Set<String> validationsCode = new HashSet<>();

    @PostMapping(path = "/")
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

    @GetMapping(path = "/{version}/")
    @PreAuthorize("hasAuthority('SHOW_DEVICE_CONFIG')")
    @ApiOperation(
            value = "List all device firmware updates of a version",
            response = DeviceConfigVO.class)
    public List<DeviceFirmwareUpdateInputVO> list(
            @PathVariable("application") String applicationId,
            @PathVariable("version") String version
    ) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        ServiceResponse<List<DeviceFwUpdate>> serviceResponse = deviceFirmwareUpdateService.findByVersion(tenant, application, version);

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
