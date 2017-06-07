package com.konkerlabs.platform.registry.api.web.controller;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
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
import com.konkerlabs.platform.registry.api.model.DeviceHealthAlertVO;
import com.konkerlabs.platform.registry.api.model.DeviceHealthVO;
import com.konkerlabs.platform.registry.api.model.DeviceInputVO;
import com.konkerlabs.platform.registry.api.model.DeviceStatsVO;
import com.konkerlabs.platform.registry.api.model.DeviceVO;
import com.konkerlabs.platform.registry.api.model.RestResponse;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.DeviceModel;
import com.konkerlabs.platform.registry.business.model.HealthAlert;
import com.konkerlabs.platform.registry.business.model.Location;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.DeviceModelService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService.Validations;
import com.konkerlabs.platform.registry.business.services.api.HealthAlertService;
import com.konkerlabs.platform.registry.business.services.api.LocationSearchService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

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
    private LocationSearchService locationSearchService;

    @Autowired
    private DeviceModelService deviceModelService;

    @Autowired
    private HealthAlertService healthAlertService;

    @Autowired
    private MessageSource messageSource;

    private Set<String> validationsCode = new HashSet<>();

    @GetMapping(path = "/")
    @PreAuthorize("hasAuthority('LIST_DEVICES')")
    @ApiOperation(
            value = "List all devices by application",
            response = DeviceVO.class)
    public List<DeviceVO> list(@PathVariable("application") String applicationId) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        ServiceResponse<List<Device>> deviceResponse = deviceRegisterService.findAll(tenant, application);

        if (!deviceResponse.isOk()) {
            throw new BadServiceResponseException(user, deviceResponse, validationsCode);
        } else {
            return new DeviceVO().apply(deviceResponse.getResult());
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
            throw new NotFoundResponseException(user, deviceResponse);
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
            @RequestBody DeviceInputVO deviceForm) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);
        Location location = getLocation(tenant, application, deviceForm);
        DeviceModel deviceModel = getDeviceModel(tenant, application, deviceForm);

        Device device = Device.builder()
                .name(deviceForm.getName())
                .deviceId(deviceForm.getId())
                .description(deviceForm.getDescription())
                .location(location)
                .deviceModel(deviceModel)
                .active(true)
                .build();

        ServiceResponse<Device> deviceResponse = deviceRegisterService.register(tenant, application, device);

        if (!deviceResponse.isOk()) {
            throw new BadServiceResponseException(user, deviceResponse, validationsCode);
        } else {
            return new DeviceVO().apply(deviceResponse.getResult());
        }

    }

    private DeviceModel getDeviceModel(Tenant tenant, Application application, DeviceInputVO deviceForm) throws BadServiceResponseException {
    	if (deviceForm == null || StringUtils.isBlank(deviceForm.getDeviceModelName())) {
            return null;
        }

        ServiceResponse<DeviceModel> deviceModelResponse = deviceModelService
        		.getByTenantApplicationAndName(tenant, application, deviceForm.getDeviceModelName());
        if (deviceModelResponse.isOk()) {
        	DeviceModel deviceModel = deviceModelResponse.getResult();
            return deviceModel;
        } else {
            throw new BadServiceResponseException(user, deviceModelResponse, validationsCode);
        }
	}

	private Location getLocation(Tenant tenant, Application application, DeviceInputVO deviceForm) throws BadServiceResponseException {

        if (deviceForm == null || StringUtils.isBlank(deviceForm.getLocationName())) {
            return null;
        }

        ServiceResponse<Location> locationResponse = locationSearchService.findByName(tenant, application, deviceForm.getLocationName(), false);
        if (locationResponse.isOk()) {
            Location location = locationResponse.getResult();
            return location;
        } else {
            throw new BadServiceResponseException(user, locationResponse, validationsCode);
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
        Location location = getLocation(tenant, application, deviceForm);
        DeviceModel deviceModel = getDeviceModel(tenant, application, deviceForm);

        Device deviceFromDB = null;
        ServiceResponse<Device> deviceResponse = deviceRegisterService.getByDeviceGuid(tenant, application, deviceGuid);

        if (!deviceResponse.isOk()) {
            throw new BadServiceResponseException(user, deviceResponse, validationsCode);
        } else {
            deviceFromDB = deviceResponse.getResult();
        }

        // update fields
        deviceFromDB.setName(deviceForm.getName());
        deviceFromDB.setDescription(deviceForm.getDescription());
        deviceFromDB.setLocation(location);
        deviceFromDB.setDeviceModel(deviceModel);
        deviceFromDB.setActive(deviceForm.isActive());

        ServiceResponse<Device> updateResponse = deviceRegisterService.update(tenant, application, deviceGuid, deviceFromDB);

        if (!updateResponse.isOk()) {
            throw new BadServiceResponseException(user, updateResponse, validationsCode);

        }

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
                throw new NotFoundResponseException(user, deviceResponse);
            } else {
                throw new BadServiceResponseException(user, deviceResponse, validationsCode);
            }
        }

    }

    @GetMapping(path = "/{deviceGuid}/health")
    @ApiOperation(
            value = "Get a device health by device guid",
            response = RestResponse.class
    )
    @PreAuthorize("hasAuthority('SHOW_DEVICE')")
    public DeviceHealthVO health(
    		@PathVariable("application") String applicationId,
    		@PathVariable("deviceGuid") String deviceGuid) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        ServiceResponse<HealthAlert> deviceResponse = healthAlertService.getLastHightServerityByDeviceGuid(
        		tenant,
        		application,
        		deviceGuid);

        if (deviceResponse.isOk()) {
			return new DeviceHealthVO().apply(deviceResponse.getResult());
        } else {
        	throw new NotFoundResponseException(user, deviceResponse);
        }

    }

    @GetMapping(path = "/{deviceGuid}/health/alerts")
    @ApiOperation(
            value = "List all device health alerts by device guid",
            response = RestResponse.class
    )
    @PreAuthorize("hasAuthority('SHOW_DEVICE')")
    public List<DeviceHealthAlertVO> alerts(
    		@PathVariable("application") String applicationId,
    		@PathVariable("deviceGuid") String deviceGuid) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        ServiceResponse<List<HealthAlert>> deviceResponse = healthAlertService.findAllByTenantApplicationAndDeviceGuid(
        		tenant,
        		application,
        		deviceGuid);

        if (deviceResponse.isOk()) {
            List<DeviceHealthAlertVO> healthAlertsVO = new LinkedList<>();

            for (HealthAlert healthAlert: deviceResponse.getResult()) {
                DeviceHealthAlertVO healthAlertVO = new DeviceHealthAlertVO();
                healthAlertVO = healthAlertVO.apply(healthAlert);
                healthAlertVO.setDescription(messageSource.getMessage(healthAlert.getDescription().getCode(), null, user.getLanguage().getLocale()));

                healthAlertsVO.add(healthAlertVO);
            }
            return healthAlertsVO;
        } else {
        	throw new NotFoundResponseException(user, deviceResponse);
        }

    }
    
    @GetMapping(path = "/{deviceGuid}/stats")
    @ApiOperation(
            value = "Get a device stats by guid",
            response = RestResponse.class
    )
    @PreAuthorize("hasAuthority('SHOW_DEVICE')")
    public DeviceStatsVO stats(
    		@PathVariable("application") String applicationId,
    		@PathVariable("deviceGuid") String deviceGuid) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        ServiceResponse<Device> deviceResponse = deviceRegisterService.getByDeviceGuid(tenant, application, deviceGuid);

        if (!deviceResponse.isOk()) {
            throw new NotFoundResponseException(user, deviceResponse);
        } else {
            return new DeviceStatsVO().apply(deviceResponse.getResult());
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
