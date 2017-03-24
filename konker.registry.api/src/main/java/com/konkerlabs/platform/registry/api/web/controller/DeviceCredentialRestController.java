package com.konkerlabs.platform.registry.api.web.controller;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.konkerlabs.platform.registry.api.exceptions.BadServiceResponseException;
import com.konkerlabs.platform.registry.api.exceptions.NotFoundResponseException;
import com.konkerlabs.platform.registry.api.model.DeviceSecurityCredentialsVO;
import com.konkerlabs.platform.registry.api.model.RestResponse;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService.DeviceDataURLs;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@Scope("request")
@RequestMapping(
        value = "/deviceCredentials"
)
@Api(tags = "device credentials")
public class DeviceCredentialRestController implements InitializingBean {

    @Autowired
    private DeviceRegisterService deviceRegisterService;

    @Autowired
    private User user;

    private Set<String> validationsCode = new HashSet<>();

    @GetMapping(path = "/{deviceGuid}")
    @ApiOperation(
            value = "Get device username and connections URLs",
            response = RestResponse.class
    )
    @PreAuthorize("hasAuthority('SHOW_DEVICE')")
    public DeviceSecurityCredentialsVO read(
            @ApiParam(required = true)
            @PathVariable("deviceGuid") String deviceGuid) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();

        ServiceResponse<Device> deviceResponse = deviceRegisterService.getByDeviceGuid(tenant, deviceGuid);

        if (!deviceResponse.isOk()) {
            throw new NotFoundResponseException(user, deviceResponse);
        } else {

            Device device = deviceResponse.getResult();

            ServiceResponse<DeviceDataURLs> deviceURLResponse = deviceRegisterService
                    .getDeviceDataURLs(tenant, device, user.getLanguage().getLocale());

            return DeviceSecurityCredentialsVO.builder()
                    .username(deviceResponse.getResult().getApiKey())
                    .build()
                    .setDeviceDataURLs(deviceURLResponse.getResult());
        }

    }

    @PostMapping(path = "/{deviceGuid}")
    @ApiOperation(value = "Create a new device username and password. It will not be possible to recover the generated password again, so store it safely.")
    @PreAuthorize("hasAuthority('CREATE_DEVICE_KEYS')")
    public DeviceSecurityCredentialsVO create(
            @ApiParam(required = true)
            @PathVariable("deviceGuid") String deviceGuid) throws BadServiceResponseException {

        Tenant tenant = user.getTenant();

        ServiceResponse<DeviceRegisterService.DeviceSecurityCredentials> deviceResponse = deviceRegisterService
                .generateSecurityPassword(tenant, deviceGuid);

        if (!deviceResponse.isOk()) {
            throw new BadServiceResponseException(user, deviceResponse, validationsCode);
        } else {

            Device device = deviceResponse.getResult().getDevice();

            ServiceResponse<DeviceDataURLs> deviceURLResponse = deviceRegisterService
                    .getDeviceDataURLs(tenant, device, user.getLanguage().getLocale());

            return new DeviceSecurityCredentialsVO(deviceResponse.getResult(), deviceURLResponse.getResult());
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
