package com.konkerlabs.platform.registry.api.web.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;
import org.bson.types.Binary;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.konkerlabs.platform.registry.api.exceptions.BadServiceResponseException;
import com.konkerlabs.platform.registry.api.exceptions.NotFoundResponseException;
import com.konkerlabs.platform.registry.api.model.DeviceFirmwareVO;
import com.konkerlabs.platform.registry.api.model.RestResponse;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.DeviceFirmware;
import com.konkerlabs.platform.registry.business.model.DeviceModel;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.DeviceFirmwareService;
import com.konkerlabs.platform.registry.business.services.api.DeviceModelService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@Scope("request")
@RequestMapping(value = "/{application}/firmwares/")
@Api(tags = "device firmwares")
public class DeviceFirmwareRestController extends AbstractRestController implements InitializingBean {

    @Autowired
    private DeviceModelService deviceModelService;

    @Autowired
    private DeviceFirmwareService deviceFirmwareService;

    private Set<String> validationsCode = new HashSet<>();

    @GetMapping(path = "/{deviceModelName}/")
    @ApiOperation(
            value = "List device model firmwares",
            response = RestResponse.class
    )
    @PreAuthorize("hasAuthority('SHOW_DEVICE_CONFIG')")
    public List<DeviceFirmwareVO> list(
            @PathVariable("application") String applicationId,
            @PathVariable("deviceModelName") String deviceModelName) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);
        DeviceModel deviceModel = getDeviceModel(tenant, application, deviceModelName);

        ServiceResponse<List<DeviceFirmware>> serviceResponse = deviceFirmwareService.listByDeviceModel(tenant, application, deviceModel);

        if (!serviceResponse.isOk()) {
            throw new BadServiceResponseException(user, serviceResponse, validationsCode);
        } else {
            return new DeviceFirmwareVO().apply(serviceResponse.getResult());
        }

    }

    @PostMapping(path = "/{deviceModelName}", consumes = "multipart/form-data")
    @ApiOperation(value = "Create a device firmware")
    @PreAuthorize("hasAuthority('CREATE_DEVICE_CONFIG')")
    public DeviceFirmwareVO create(
            @PathVariable("application") String applicationId,
            @PathVariable("deviceModelName") String deviceModelName,
            @RequestParam(value = "firmware", required = true) MultipartFile firmwareFile,
            @RequestParam(value = "checksum", required = true) MultipartFile checksumFile,
            @RequestParam(value = "version", required = true) String version
            ) throws BadServiceResponseException, NotFoundResponseException, IOException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);
        DeviceModel deviceModel = getDeviceModel(tenant, application, deviceModelName);

        String checksum = getChecksum(checksumFile);
        validateChecksum(firmwareFile.getBytes(), checksum);

        DeviceFirmware deviceFirmware = DeviceFirmware.builder()
                .tenant(tenant)
                .application(application)
                .deviceModel(deviceModel)
                .firmware(new Binary(firmwareFile.getBytes()))
                .version(version)
                .build();

        ServiceResponse<DeviceFirmware> serviceResponse = deviceFirmwareService.save(tenant, application, deviceFirmware);

        if (!serviceResponse.isOk()) {
            throw new BadServiceResponseException(user, serviceResponse, validationsCode);
        } else {
            return new DeviceFirmwareVO().apply(serviceResponse.getResult());
        }

    }

    private String getChecksum(MultipartFile checksumFile) throws IOException {

        String checksum = "";

        if (checksumFile != null && checksumFile.getBytes() != null) {
            checksum = new String(checksumFile.getBytes());
            // read until first space
            int idx = checksum.indexOf(' ');
            if (idx > 0) {
                checksum = checksum.substring(0, idx);
            }
        }

        return checksum;

    }

    private DeviceModel getDeviceModel(Tenant tenant, Application application, String deviceModelName) throws BadServiceResponseException, NotFoundResponseException {

        ServiceResponse<DeviceModel> applicationResponse = deviceModelService.getByTenantApplicationAndName(tenant, application, deviceModelName);
        if (!applicationResponse.isOk()) {
            if (applicationResponse.getResponseMessages().containsKey(DeviceModelService.Validations.DEVICE_MODEL_NOT_FOUND.getCode())) {
                throw new NotFoundResponseException(user, applicationResponse);

            } else {
                Set<String> validationsCode = new HashSet<>();
                for (DeviceModelService.Validations value : DeviceModelService.Validations.values()) {
                    validationsCode.add(value.getCode());
                }

                throw new BadServiceResponseException(user, applicationResponse, validationsCode);
            }
        }

        return applicationResponse.getResult();

    }

    @Override
    public void afterPropertiesSet() throws Exception {

        for (DeviceFirmware.Validations value : DeviceFirmware.Validations.values()) {
            validationsCode.add(value.getCode());
        }
        for (DeviceFirmwareService.Validations value : DeviceFirmwareService.Validations.values()) {
            validationsCode.add(value.getCode());
        }

    }

    private void validateChecksum(byte firmware[], String checksum) throws BadServiceResponseException {
        if (isValidSHA1(firmware, checksum)) {
            return;
        }
        if (isValidMD5(firmware, checksum)) {
            return;
        }

        Map<String, Object[]> responseMessages = new HashMap<>();
        responseMessages.put(DeviceFirmware.Validations.INVALID_CHECKSUM.getCode(), null);

        throw new BadServiceResponseException(user, responseMessages, validationsCode);
    }

    private boolean isValidMD5(byte[] firmware, String checksum) {
        String hash = DigestUtils.md5Hex(firmware);
        return hash.equals(checksum);
    }

    private boolean isValidSHA1(byte[] firmware, String checksum) {
        String hash = DigestUtils.sha1Hex(firmware);
        return hash.equals(checksum);
    }

}
