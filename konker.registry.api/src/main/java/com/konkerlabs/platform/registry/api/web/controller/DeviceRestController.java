package com.konkerlabs.platform.registry.api.web.controller;

import com.konkerlabs.platform.registry.api.exceptions.BadServiceResponseException;
import com.konkerlabs.platform.registry.api.exceptions.NotAuthorizedResponseException;
import com.konkerlabs.platform.registry.api.exceptions.NotFoundResponseException;
import com.konkerlabs.platform.registry.api.model.ApplicationDestinationVO;
import com.konkerlabs.platform.registry.api.model.DeviceInputVO;
import com.konkerlabs.platform.registry.api.model.DeviceVO;
import com.konkerlabs.platform.registry.api.model.RestResponse;
import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService.Validations;
import com.konkerlabs.platform.registry.business.services.api.GatewayService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@Scope("request")
@RequestMapping(
        value = "/{application}/devices"
)
@Api(tags = "devices")
public class DeviceRestController extends AbstractRestController implements InitializingBean {

    @Autowired
    private DeviceRegisterService deviceRegisterService;
    @Autowired
    private OauthClientDetails oauthClientDetails;
    @Autowired
    private GatewayService gatewayService;

    private Set<String> validationsCode = new HashSet<>();

    @GetMapping(path = "/")
    @PreAuthorize("hasAuthority('LIST_DEVICES')")
    @ApiOperation(
            value = "List all devices by application",
            response = DeviceVO.class)
    public List<DeviceVO> list(
            @PathVariable("application") String applicationId,
            @ApiParam(value = "Location")
            @RequestParam(required = false) String locationName,
            @ApiParam(value = "Tag filter")
            @RequestParam(required = false) String tag,
            @ApiParam(value = "Page number")
            @RequestParam(required = false, defaultValue = "0") int page,
            @ApiParam(value = "Number of elements per page")
            @RequestParam(required = false, defaultValue = "500") int size) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);
        User userLogged = Optional.ofNullable(user.getParentUser())
                .orElseGet(() -> User.builder()
                        .application(user.getParentGateway().getApplication())
                        .location(user.getParentGateway().getLocation())
                        .build());

        ServiceResponse<Page<Device>> deviceResponse = deviceRegisterService.search(tenant,
                application,
                userLogged,
                locationName,
                tag,
                page,
                size);

        if (!deviceResponse.isOk()) {
            throw new BadServiceResponseException( deviceResponse, validationsCode);
        } else {
            return new DeviceVO().apply(deviceResponse.getResult().getContent());
        }

    }

    @GetMapping(path = "/{deviceGuid}")
    @ApiOperation(
            value = "Get a device by guid",
            response = RestResponse.class
    )
    @PreAuthorize("hasAuthority('SHOW_DEVICE')")
    public DeviceVO read(
            @PathVariable("application") String applicationId,
            @PathVariable("deviceGuid") String deviceGuid) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        ServiceResponse<Device> deviceResponse = deviceRegisterService.getByDeviceGuid(tenant, application, deviceGuid);

        if (!deviceResponse.isOk()) {
            throw new NotFoundResponseException(deviceResponse);
        } else {
            return new DeviceVO().apply(deviceResponse.getResult());
        }

    }

    @PostMapping
    @ApiOperation(value = "Create a device")
    @PreAuthorize("hasAuthority('ADD_DEVICE')")
    public DeviceVO create(
            @PathVariable("application") String applicationId,
            @ApiParam(name = "body", required = true)
            @RequestBody DeviceInputVO deviceForm)
            throws BadServiceResponseException, NotFoundResponseException, NotAuthorizedResponseException {


        Tenant tenant = null;
        Gateway gateway = null;
        if (oauthClientDetails != null) {
            tenant = oauthClientDetails.getTenant();
            if (oauthClientDetails.getParentGateway() != null) {
                gateway = oauthClientDetails.getParentGateway();
            }
        } else if (user != null) {
            tenant = user.getTenant();
        }

        Application application = getApplication(applicationId);
        Location location = getLocation(tenant, application, deviceForm.getLocationName());
        DeviceModel deviceModel = getDeviceModel(tenant, application, deviceForm.getDeviceModelName());

        if (Optional.ofNullable(gateway).isPresent() &&
                !Optional.ofNullable(deviceForm.getLocationName()).isPresent()) {
            location = gateway.getLocation();
        }

        Device device = Device.builder()
                .name(deviceForm.getName())
                .deviceId(deviceForm.getId())
                .description(deviceForm.getDescription())
                .location(location)
                .deviceModel(deviceModel)
                .active(true)
                .build();


        if (Optional.ofNullable(gateway).isPresent()) {
            ServiceResponse<Boolean> validationResult =
                    gatewayService.validateGatewayAuthorization(
                            gateway,
                            device.getLocation()
                    );

            if (!validationResult.isOk()) {
                throw new NotAuthorizedResponseException(
                        validationResult,
                        validationsCode
                );
            }
        }
        ServiceResponse<Device> deviceResponse = deviceRegisterService.register(tenant, application, device);

        if (!deviceResponse.isOk()) {
            throw new BadServiceResponseException( deviceResponse, validationsCode);
        } else {
            return new DeviceVO().apply(deviceResponse.getResult());
        }

    }

    @PutMapping(path = "/{deviceGuid}")
    @ApiOperation(value = "Update a device")
    @PreAuthorize("hasAuthority('EDIT_DEVICE')")
    public void update(
            @PathVariable("application") String applicationId,
            @PathVariable("deviceGuid") String deviceGuid,
            @ApiParam(name = "body", required = true)
            @RequestBody DeviceInputVO deviceForm) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);
        Location location = getLocation(tenant, application, deviceForm.getLocationName());
        DeviceModel deviceModel = getDeviceModel(tenant, application, deviceForm.getDeviceModelName());

        Device deviceFromDB;
        ServiceResponse<Device> deviceResponse = deviceRegisterService.getByDeviceGuid(tenant, application, deviceGuid);

        if (!deviceResponse.isOk()) {
            throw new BadServiceResponseException( deviceResponse, validationsCode);
        } else {
            deviceFromDB = deviceResponse.getResult();
        }

        // update fields
        deviceFromDB.setName(deviceForm.getName());
        deviceFromDB.setDescription(deviceForm.getDescription());
        deviceFromDB.setTags(deviceForm.getTags());
        deviceFromDB.setLocation(location);
        deviceFromDB.setDeviceModel(deviceModel);
        deviceFromDB.setActive(deviceForm.isActive());
        deviceFromDB.setDebug(deviceForm.isDebug());

        ServiceResponse<Device> updateResponse = deviceRegisterService.update(tenant, application, deviceGuid, deviceFromDB);

        if (!updateResponse.isOk()) {
            throw new BadServiceResponseException( updateResponse, validationsCode);

        }

    }

    @PutMapping(path = "/{deviceGuid}/application")
    @ApiOperation(value = "Move device to another application")
    @PreAuthorize("hasAuthority('EDIT_DEVICE')")
    public DeviceVO move(
            @PathVariable("application") String applicationId,
            @PathVariable("deviceGuid") String deviceGuid,
            @ApiParam(name = "body", required = true)
            @RequestBody ApplicationDestinationVO applicationDestinationVO)
            throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);
        Application destApplication = getApplication(applicationDestinationVO.getDestinationApplicationName());

        ServiceResponse<Device> deviceResponse = deviceRegisterService.move(tenant, application, deviceGuid, destApplication);

        if (!deviceResponse.isOk()) {
            throw new BadServiceResponseException( deviceResponse, validationsCode);
        }

        return new DeviceVO().apply(deviceResponse.getResult());

    }

    @DeleteMapping(path = "/{deviceGuid}")
    @ApiOperation(value = "Delete a device")
    @PreAuthorize("hasAuthority('REMOVE_DEVICE')")
    public void delete(
            @PathVariable("application") String applicationId,
            @PathVariable("deviceGuid") String deviceGuid) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        ServiceResponse<Device> deviceResponse = deviceRegisterService.remove(tenant, application, deviceGuid);

        if (!deviceResponse.isOk()) {
            if (deviceResponse.getResponseMessages().containsKey(Validations.DEVICE_GUID_DOES_NOT_EXIST.getCode())) {
                throw new NotFoundResponseException(deviceResponse);
            } else {
                throw new BadServiceResponseException( deviceResponse, validationsCode);
            }
        }

    }

    @Override
    public void afterPropertiesSet() {

        for (com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService.Validations value : DeviceRegisterService.Validations.values()) {
            validationsCode.add(value.getCode());
        }

        for (ApplicationService.Validations value : ApplicationService.Validations.values()) {
            validationsCode.add(value.getCode());
        }

        for (com.konkerlabs.platform.registry.business.model.Device.Validations value : Device.Validations.values()) {
            validationsCode.add(value.getCode());
        }

        for (CommonValidations value : CommonValidations.values()) {
            validationsCode.add(value.getCode());
        }

    }

}
