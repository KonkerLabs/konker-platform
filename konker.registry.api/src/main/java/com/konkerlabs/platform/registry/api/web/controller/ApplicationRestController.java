package com.konkerlabs.platform.registry.api.web.controller;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.konkerlabs.platform.registry.api.exceptions.BadServiceResponseException;
import com.konkerlabs.platform.registry.api.exceptions.NotFoundResponseException;
import com.konkerlabs.platform.registry.api.model.ApplicationInputVO;
import com.konkerlabs.platform.registry.api.model.ApplicationVO;
import com.konkerlabs.platform.registry.api.model.HealthAlertVO;
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

    private Set<String> validationsCode = new HashSet<>();

    @GetMapping(path = "/")
    @PreAuthorize("hasAuthority('LIST_APPLICATION')")
    @ApiOperation(
            value = "List all applications by organization",
            response = ApplicationVO.class)
    public List<ApplicationVO> list(
            @ApiParam(value = "Page number")
            @RequestParam(required = false, defaultValue = "0") int page,
            @ApiParam(value = "Number of elements per page")
            @RequestParam(required = false, defaultValue = "500") int size) throws BadServiceResponseException {

        Tenant tenant = user.getTenant();

        ServiceResponse<Page<Application>> applicationResponse = applicationService.findAll(tenant, page, size);

        if (!applicationResponse.isOk()) {
            throw new BadServiceResponseException( applicationResponse, validationsCode);
        } else {
    		for (Application applic : applicationResponse.getResult()) {
    			if(applicationService.isDefaultApplication(applic,tenant)) {
    				applic.setName(ApplicationService.DEFAULT_APPLICATION_ALIAS);
    				break;
    			}
    		}
        	
            return new ApplicationVO().apply(applicationResponse.getResult().getContent());
        }

    }

    @GetMapping(path = "/{applicationName}")
    @ApiOperation(
            value = "Get a application by name",
            response = RestResponse.class
    )
    @PreAuthorize("hasAuthority('SHOW_APPLICATION')")
    public ApplicationVO read(@PathVariable("applicationName") String applicationName) throws NotFoundResponseException {

        Tenant tenant = user.getTenant();

        ServiceResponse<Application> applicationResponse = applicationService.getByApplicationName(tenant, applicationName);

        if (!applicationResponse.isOk()) {
            throw new NotFoundResponseException(applicationResponse);
        } else {
			if(applicationService.isDefaultApplication(applicationResponse.getResult(),tenant)) {
				applicationResponse.getResult().setName(ApplicationService.DEFAULT_APPLICATION_ALIAS);
   			}
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
            throw new BadServiceResponseException( applicationResponse, validationsCode);
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

        Application applicationFromDB;
        ServiceResponse<Application> applicationResponse = applicationService.getByApplicationName(tenant, applicationName);

        if (!applicationResponse.isOk()) {
            throw new BadServiceResponseException( applicationResponse, validationsCode);
        } else {
            applicationFromDB = applicationResponse.getResult();
        }

        // update fields
        applicationFromDB.setFriendlyName(applicationForm.getFriendlyName());
        applicationFromDB.setDescription(applicationForm.getDescription());

        ServiceResponse<Application> updateResponse = applicationService.update(tenant, applicationName, applicationFromDB);

        if (!updateResponse.isOk()) {
            throw new BadServiceResponseException( updateResponse, validationsCode);

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
                throw new NotFoundResponseException(applicationResponse);
            } else {
                throw new BadServiceResponseException( applicationResponse, validationsCode);
            }
        }

    }

    @GetMapping(path = "/{applicationName}/health/alerts")
    @ApiOperation(
            value = "List all health alerts from this application",
            response = RestResponse.class
    )
    @PreAuthorize("hasAuthority('SHOW_APPLICATION')")
    public List<HealthAlertVO> alerts(@PathVariable("applicationName") String applicationName) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationName);

        ServiceResponse<List<HealthAlert>> serviceResponse = healthAlertService.findAllByTenantAndApplication(tenant, application);

        if (!serviceResponse.isOk()) {
            throw new NotFoundResponseException(serviceResponse);
        } else {
            List<HealthAlertVO> healthAlertsVO = new LinkedList<>();

            for (HealthAlert healthAlert: serviceResponse.getResult()) {
                HealthAlertVO healthAlertVO = new HealthAlertVO();
                healthAlertVO = healthAlertVO.apply(healthAlert);
                healthAlertVO.setDescription(healthAlert.getDescription());

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
            throw new NotFoundResponseException(serviceResponse);
        }

    }

    @Override
    public void afterPropertiesSet() {
        for (Validations value : Validations.values()) {
            validationsCode.add(value.getCode());
        }

        for (ApplicationService.Validations value : ApplicationService.Validations.values()) {
            validationsCode.add(value.getCode());
        }

    }

}
