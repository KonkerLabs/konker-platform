package com.konkerlabs.platform.registry.api.web.controller;

import com.konkerlabs.platform.registry.api.exceptions.BadServiceResponseException;
import com.konkerlabs.platform.registry.api.exceptions.NotFoundResponseException;
import com.konkerlabs.platform.registry.api.model.HealthAlertVO;
import com.konkerlabs.platform.registry.api.model.DeviceHealthVO;
import com.konkerlabs.platform.registry.api.model.DeviceStatsVO;
import com.konkerlabs.platform.registry.api.model.RestResponse;
import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.HealthAlertService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@RestController
@Scope("request")
@RequestMapping(
        value = "/{application}/devices"
)
@Api(tags = "device status")
public class DeviceStatusRestController extends AbstractRestController implements InitializingBean {

    @Autowired
    private DeviceRegisterService deviceRegisterService;

    @Autowired
    private HealthAlertService healthAlertService;

    @Autowired
    private DeviceEventService deviceEventService;

    private Set<String> validationsCode = new HashSet<>();

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

        ServiceResponse<HealthAlert> deviceResponse = healthAlertService.getLastHighestSeverityByDeviceGuid(
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
    public List<HealthAlertVO> alerts(
            @PathVariable("application") String applicationId,
            @PathVariable("deviceGuid") String deviceGuid) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        ServiceResponse<List<HealthAlert>> deviceResponse = healthAlertService.findAllByTenantApplicationAndDeviceGuid(
                tenant,
                application,
                deviceGuid);

        if (deviceResponse.isOk()) {
            List<HealthAlertVO> healthAlertsVO = new LinkedList<>();

            for (HealthAlert healthAlert: deviceResponse.getResult()) {
                HealthAlertVO healthAlertVO = new HealthAlertVO();
                healthAlertVO = healthAlertVO.apply(healthAlert);
                healthAlertVO.setDescription(healthAlert.getDescription());

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
        ServiceResponse<List<Event>> incomingResponse = deviceEventService.findIncomingBy(tenant, application, deviceGuid, null, null, null, false, 1);

        if (!deviceResponse.isOk()) {
            throw new NotFoundResponseException(user, deviceResponse);
        } else {
            String lastDataReceivedDate = "";
            if (incomingResponse.isOk()) {
                List<Event> result = incomingResponse.getResult();
                lastDataReceivedDate = result.isEmpty() ? "" : result.get(0).getCreationTimestamp().toString();
            }

            DeviceStatsVO vo = new DeviceStatsVO().apply(deviceResponse.getResult());
            vo.setLastDataReceivedDate(lastDataReceivedDate);
            return vo;
        }

    }


    @Override
    public void afterPropertiesSet() {

        for (com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService.Validations value : DeviceRegisterService.Validations.values()) {
            validationsCode.add(value.getCode());
        }

        for (com.konkerlabs.platform.registry.business.model.Device.Validations value : Device.Validations.values()) {
            validationsCode.add(value.getCode());
        }

    }

}
