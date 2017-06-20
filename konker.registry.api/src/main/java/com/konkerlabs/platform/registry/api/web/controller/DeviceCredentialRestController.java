package com.konkerlabs.platform.registry.api.web.controller;

import com.konkerlabs.platform.registry.api.exceptions.BadServiceResponseException;
import com.konkerlabs.platform.registry.api.exceptions.NotFoundResponseException;
import com.konkerlabs.platform.registry.api.model.DeviceSecurityCredentialsPwdVO;
import com.konkerlabs.platform.registry.api.model.DeviceSecurityCredentialsVO;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService.DeviceDataURLs;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@RestController
@Scope("request")
@RequestMapping(
        value = "/{application}/deviceCredentials"
)
@Api(tags = "device credentials")
public class DeviceCredentialRestController extends AbstractRestController implements InitializingBean {

    @Autowired
    private DeviceRegisterService deviceRegisterService;

    private Set<String> validationsCode = new HashSet<>();

    @GetMapping(path = "/{deviceGuid}")
    @ApiOperation(
            value = "Get device username and connections URLs"
    )
    @PreAuthorize("hasAuthority('SHOW_DEVICE')")
    public DeviceSecurityCredentialsVO read(
            @PathVariable("application") String applicationId,
            @ApiParam(required = true)
            @PathVariable("deviceGuid") String deviceGuid) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        ServiceResponse<Device> deviceResponse = deviceRegisterService.getByDeviceGuid(tenant, application, deviceGuid);

        if (!deviceResponse.isOk()) {
            throw new NotFoundResponseException(user, deviceResponse);
        } else {

            Device device = deviceResponse.getResult();

            ServiceResponse<DeviceDataURLs> deviceURLResponse = deviceRegisterService
                    .getDeviceDataURLs(tenant, application, device, new Locale("en", "US"));

            return DeviceSecurityCredentialsVO.builder()
                    .username(deviceResponse.getResult().getApiKey())
                    .build()
                    .setDeviceDataURLs(deviceURLResponse.getResult());
        }

    }

    @PostMapping(path = "/{deviceGuid}")
    @ApiOperation(
            value = "Create a new device username and password. It will not be possible to recover the generated password again, so store it safely."
    )
    @PreAuthorize("hasAuthority('CREATE_DEVICE_KEYS')")
    public DeviceSecurityCredentialsPwdVO create(
            @PathVariable("application") String applicationId,
            @ApiParam(required = true)
            @PathVariable("deviceGuid") String deviceGuid) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        ServiceResponse<DeviceRegisterService.DeviceSecurityCredentials> deviceResponse = deviceRegisterService
                .generateSecurityPassword(tenant, application, deviceGuid);

        if (!deviceResponse.isOk()) {
            throw new BadServiceResponseException(user, deviceResponse, validationsCode);
        } else {

            Device device = deviceResponse.getResult().getDevice();

            ServiceResponse<DeviceDataURLs> deviceURLResponse = deviceRegisterService
                    .getDeviceDataURLs(tenant, application, device, new Locale("en", "US"));

            return new DeviceSecurityCredentialsPwdVO(deviceResponse.getResult(), deviceURLResponse.getResult());
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
