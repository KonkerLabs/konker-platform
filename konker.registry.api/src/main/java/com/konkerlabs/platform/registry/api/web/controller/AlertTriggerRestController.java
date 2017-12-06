package com.konkerlabs.platform.registry.api.web.controller;

import com.konkerlabs.platform.registry.api.exceptions.BadRequestResponseException;
import com.konkerlabs.platform.registry.api.exceptions.BadServiceResponseException;
import com.konkerlabs.platform.registry.api.exceptions.NotFoundResponseException;
import com.konkerlabs.platform.registry.api.model.AlertTriggerInputVO;
import com.konkerlabs.platform.registry.api.model.AlertTriggerVO;
import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.model.RestDestination.Validations;
import com.konkerlabs.platform.registry.business.services.api.AlertTriggerService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@Scope("request")
@RequestMapping(value = "/{application}/triggers")
@Api(tags = "alert triggers")
public class AlertTriggerRestController extends AbstractRestController implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlertTriggerRestController.class);

    @Autowired
    private AlertTriggerService alertTriggerService;

    private Set<String> validationsCode = new HashSet<>();

    @GetMapping(path = "/")
    @PreAuthorize("hasAuthority('LIST_ALERT_TRIGGERS')")
    @ApiOperation(
            value = "List all triggers by application",
            response = AlertTriggerVO.class)
    public List<AlertTriggerVO> list(@PathVariable("application") String applicationId) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        ServiceResponse<List<AlertTrigger>> restDestinationResponse = alertTriggerService.listByTenantAndApplication(tenant, application);

        if (!restDestinationResponse.isOk()) {
            throw new BadServiceResponseException(user, restDestinationResponse, validationsCode);
        }

        List<AlertTriggerVO> alertTriggersVO = new ArrayList<>(restDestinationResponse.getResult().size());

        for (AlertTrigger alertTrigger: restDestinationResponse.getResult()) {
            alertTriggersVO.add(new AlertTriggerVO(alertTrigger));
        }

        return alertTriggersVO;

    }

    @PostMapping
    @ApiOperation(value = "Create a trigger",
                  response = AlertTriggerVO.class)
    @PreAuthorize("hasAuthority('CREATE_ALERT_TRIGGER')")
    public AlertTriggerVO create(
        @PathVariable("application") String applicationId,
        @ApiParam(
                name = "body",
                value = "JSON filled with the fields described in Model and Example Value beside",
                required = true)
        @RequestBody AlertTriggerInputVO form) throws BadServiceResponseException, NotFoundResponseException, BadRequestResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        if (form.getType() == null) {
            throw new BadRequestResponseException("Trigger type is null");
        }

        AlertTrigger triggerForm = getAlertTriggerFromForm(tenant, application, form);

        ServiceResponse<AlertTrigger> response = alertTriggerService.save(tenant, application, triggerForm);

        if (!response.isOk()) {
            throw new BadServiceResponseException(user, response, validationsCode);
        } else {
            return new AlertTriggerVO(response.getResult());
        }


    }

    @GetMapping(path = "/{triggerName}")
    @PreAuthorize("hasAuthority('LIST_ALERT_TRIGGERS')")
    @ApiOperation(
            value = "Get trigger by name",
            response = AlertTriggerVO.class)
    public List<AlertTriggerVO> readTrigger(
            @PathVariable("application") String applicationId,
            @PathVariable("triggerName") String triggerName)
            throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        ServiceResponse<List<AlertTrigger>> restDestinationResponse = alertTriggerService.listByTenantAndApplication(tenant, application);

        if (!restDestinationResponse.isOk()) {
            throw new BadServiceResponseException(user, restDestinationResponse, validationsCode);
        } else {
            List<AlertTriggerVO> alertTriggersVO = new ArrayList<>(restDestinationResponse.getResult().size());

            for (AlertTrigger alertTrigger: restDestinationResponse.getResult()) {
                alertTriggersVO.add(new AlertTriggerVO(alertTrigger));
            }

            return alertTriggersVO;
        }

    }

    @DeleteMapping(path = "/{triggerName}")
    @PreAuthorize("hasAuthority('REMOVE_ALERT_TRIGGER')")
    @ApiOperation(
            value = "Delete trigger by name",
            response = AlertTriggerVO.class)
    public void deleteTrigger(
            @PathVariable("application") String applicationId,
            @PathVariable("triggerName") String triggerName)
            throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);
        AlertTrigger alertTrigger = getAlertTrigger(tenant, application, triggerName);

        ServiceResponse<AlertTrigger> restDestinationResponse = alertTriggerService.remove(tenant, application, alertTrigger.getGuid());

        if (!restDestinationResponse.isOk()) {
            throw new BadServiceResponseException(user, restDestinationResponse, validationsCode);
        }

    }

    @PutMapping(path = "/{triggerName}")
    @PreAuthorize("hasAuthority('EDIT_ALERT_TRIGGER')")
    @ApiOperation(
            value = "Update trigger by name",
            response = AlertTriggerVO.class)
    public void updateTrigger(
            @PathVariable("application") String applicationId,
            @PathVariable("triggerName") String triggerName,
            @ApiParam(
                name = "body",
                value = "JSON filled with the fields described in Model and Example Value beside",
                required = true)
            @RequestBody AlertTriggerInputVO form)
            throws BadServiceResponseException, NotFoundResponseException, BadRequestResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);
        AlertTrigger alertTrigger = getAlertTrigger(tenant, application, triggerName);
        AlertTrigger triggerForm = getAlertTriggerFromForm(tenant, application, form);

        ServiceResponse<AlertTrigger> restDestinationResponse = alertTriggerService.update(tenant, application, alertTrigger.getGuid(), triggerForm);

        if (!restDestinationResponse.isOk()) {
            throw new BadServiceResponseException(user, restDestinationResponse, validationsCode);
        }

    }

    private AlertTrigger getAlertTriggerFromForm(
                Tenant tenant,
                Application application,
                AlertTriggerInputVO form)
                    throws NotFoundResponseException, BadServiceResponseException, BadRequestResponseException {

        AlertTrigger trigger;
        switch (form.getType().toUpperCase()) {
            case "SILENCE":
                trigger = createSilenceAlertTriggerService(tenant, application, form);
                break;
            case "CUSTOM":
                trigger = createCustomAlertTrigger(tenant, application, form);
                break;
            default:
                throw new BadRequestResponseException("Invalid trigger type");
        }

        return trigger;
    }

    private AlertTrigger createCustomAlertTrigger(Tenant tenant, Application application, AlertTriggerInputVO form) throws BadServiceResponseException {

        AlertTrigger trigger = new AlertTrigger();
        trigger.setType(AlertTrigger.AlertTriggerType.CUSTOM);
        trigger.setTenant(tenant);
        trigger.setApplication(application);
        trigger.setName(form.getName());
        trigger.setDescription(form.getDescription());

        return trigger;

    }

    private AlertTrigger createSilenceAlertTriggerService(Tenant tenant, Application application, AlertTriggerInputVO form) throws NotFoundResponseException, BadServiceResponseException {

        String deviceModelName = form.getDeviceModelName();
        String locationName = form.getLocationName();

        DeviceModel deviceModel = getDeviceModel(tenant, application, deviceModelName);
        Location location = getLocation(tenant, application, locationName);

        AlertTrigger trigger = new AlertTrigger();
        trigger.setType(AlertTrigger.AlertTriggerType.SILENCE);
        trigger.setTenant(tenant);
        trigger.setApplication(application);
        trigger.setName(form.getName());
        trigger.setDescription(form.getDescription());

        trigger.setDeviceModel(deviceModel);
        trigger.setLocation(location);
        trigger.setMinutes(form.getMinutes());

        return trigger;

    }

    private AlertTrigger getAlertTrigger(Tenant tenant, Application application, String triggerName) throws BadServiceResponseException {
        ServiceResponse<AlertTrigger> serviceResponse = alertTriggerService.findByTenantAndApplicationAndName(tenant, application, triggerName);
        if (!serviceResponse.isOk()) {
            throw new BadServiceResponseException(user, serviceResponse, validationsCode);
        }

        return serviceResponse.getResult();
    }

    @Override
    public void afterPropertiesSet(){
        for (Validations value : Validations.values()) {
            validationsCode.add(value.getCode());
        }
    }

}
