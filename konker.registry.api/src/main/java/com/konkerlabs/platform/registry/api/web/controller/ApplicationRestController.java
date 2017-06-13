package com.konkerlabs.platform.registry.api.web.controller;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
import com.konkerlabs.platform.registry.api.model.ApplicationInputVO;
import com.konkerlabs.platform.registry.api.model.ApplicationVO;
import com.konkerlabs.platform.registry.api.model.DeviceHealthAlertVO;
import com.konkerlabs.platform.registry.api.model.RestResponse;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Application.Validations;
import com.konkerlabs.platform.registry.business.model.HealthAlert;
import com.konkerlabs.platform.registry.business.model.HealthAlert.Solution;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.HealthAlertService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@Scope("request")
@RequestMapping(value = "/applications")
@Api(tags = "applications")
public class ApplicationRestController extends AbstractRestController implements InitializingBean {
	
    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private HealthAlertService healthAlertService;

    @Autowired
    private MessageSource messageSource;

    private Set<String> validationsCode = new HashSet<>();

    @GetMapping(path = "/")
    @PreAuthorize("hasAuthority('LIST_APPLICATION')")
    @ApiOperation(
            value = "List all applications by organization",
            response = ApplicationVO.class)
    public List<ApplicationVO> list() throws BadServiceResponseException {

        Tenant tenant = user.getTenant();

        ServiceResponse<List<Application>> applicationResponse = applicationService.findAll(tenant);

        if (!applicationResponse.isOk()) {
            throw new BadServiceResponseException(user, applicationResponse, validationsCode);
        } else {
            return new ApplicationVO().apply(applicationResponse.getResult());
        }

    }

    @GetMapping(path = "/{applicationName}")
    @ApiOperation(
            value = "Get a application by name",
            response = RestResponse.class
    )
    @PreAuthorize("hasAuthority('SHOW_APPLICATION')")
    public ApplicationVO read(@PathVariable("applicationName") String applicationName) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();

        ServiceResponse<Application> applicationResponse = applicationService.getByApplicationName(tenant, applicationName);

        if (!applicationResponse.isOk()) {
            throw new NotFoundResponseException(user, applicationResponse);
        } else {
            return new ApplicationVO().apply(applicationResponse.getResult());
        }

    }

    @PostMapping
    @ApiOperation(value = "Create a application")
    @PreAuthorize("hasAuthority('ADD_APPLICATION')")
    public ApplicationVO create(
            @ApiParam(
            		name = "body",
            		value = "JSON filled with the fields described in Model and Example Value beside",
            		required = true)
            @RequestBody ApplicationVO applicationForm) throws BadServiceResponseException {

        Tenant tenant = user.getTenant();

        Application application = Application.builder()
                .name(applicationForm.getName())
                .friendlyName(applicationForm.getFriendlyName())
                .description(applicationForm.getDescription())
                .build();

        ServiceResponse<Application> applicationResponse = applicationService.register(tenant, application);

        if (!applicationResponse.isOk()) {
            throw new BadServiceResponseException(user, applicationResponse, validationsCode);
        } else {
            return new ApplicationVO().apply(applicationResponse.getResult());
        }

    }

    @PutMapping(path = "/{applicationName}")
    @ApiOperation(value = "Update a application")
    @PreAuthorize("hasAuthority('EDIT_APPLICATION')")
    public void update(
            @PathVariable("applicationName") String applicationName,
            @ApiParam(
            		name = "body",
            		value = "JSON filled with the fields described in Model and Example Value beside",
            		required = true)
            @RequestBody ApplicationInputVO applicationForm) throws BadServiceResponseException {

        Tenant tenant = user.getTenant();

        Application applicationFromDB = null;
        ServiceResponse<Application> applicationResponse = applicationService.getByApplicationName(tenant, applicationName);

        if (!applicationResponse.isOk()) {
            throw new BadServiceResponseException(user, applicationResponse, validationsCode);
        } else {
            applicationFromDB = applicationResponse.getResult();
        }

        // update fields
        applicationFromDB.setFriendlyName(applicationForm.getFriendlyName());
        applicationFromDB.setDescription(applicationForm.getDescription());

        ServiceResponse<Application> updateResponse = applicationService.update(tenant, applicationName, applicationFromDB);

        if (!updateResponse.isOk()) {
            throw new BadServiceResponseException(user, updateResponse, validationsCode);

        }

    }

    @DeleteMapping(path = "/{applicationName}")
    @ApiOperation(value = "Delete a application")
    @PreAuthorize("hasAuthority('REMOVE_APPLICATION')")
    public void delete(@PathVariable("applicationName") String applicationName) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();

        ServiceResponse<Application> applicationResponse = applicationService.remove(tenant, applicationName);

        if (!applicationResponse.isOk()) {
            if (applicationResponse.getResponseMessages().containsKey(ApplicationService.Validations.APPLICATION_NOT_FOUND.getCode())) {
                throw new NotFoundResponseException(user, applicationResponse);
            } else {
                throw new BadServiceResponseException(user, applicationResponse, validationsCode);
            }
        }

    }

    @GetMapping(path = "/{applicationName}/health/alerts")
    @ApiOperation(
            value = "List all health alerts from this application",
            response = RestResponse.class
    )
    @PreAuthorize("hasAuthority('SHOW_APPLICATION')")
    public List<DeviceHealthAlertVO> alerts(@PathVariable("applicationName") String applicationName) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationName);

        ServiceResponse<List<HealthAlert>> serviceResponse = healthAlertService.findAllByTenantAndApplication(tenant, application);

        if (!serviceResponse.isOk()) {
            throw new NotFoundResponseException(user, serviceResponse);
        } else {
            List<DeviceHealthAlertVO> healthAlertsVO = new LinkedList<>();

            for (HealthAlert healthAlert: serviceResponse.getResult()) {
                DeviceHealthAlertVO healthAlertVO = new DeviceHealthAlertVO();
                healthAlertVO = healthAlertVO.apply(healthAlert);
                healthAlertVO.setDescription(messageSource.getMessage(healthAlert.getDescription().getCode(), null, user.getLanguage().getLocale()));

                healthAlertsVO.add(healthAlertVO);
            }
            return healthAlertsVO;
        }

    }

    @DeleteMapping(path = "/{applicationName}/health/alerts/{alertGuid}")
    @ApiOperation(
            value = "Remove health alert from this application",
            response = RestResponse.class
    )
    @PreAuthorize("hasAuthority('SHOW_APPLICATION')")
    public void deleteAlert(
    		@PathVariable("applicationName") String applicationName,
    		@PathVariable("alertGuid") String alertGuid) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationName);

        ServiceResponse<HealthAlert> serviceResponse = healthAlertService.remove(tenant, application, alertGuid, Solution.ALERT_DELETED);

        if (!serviceResponse.isOk()) {
            throw new NotFoundResponseException(user, serviceResponse);
        }

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        for (Validations value : Validations.values()) {
            validationsCode.add(value.getCode());
        }

        for (ApplicationService.Validations value : ApplicationService.Validations.values()) {
            validationsCode.add(value.getCode());
        }

    }

}
