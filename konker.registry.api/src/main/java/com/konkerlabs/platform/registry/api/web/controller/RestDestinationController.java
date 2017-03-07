package com.konkerlabs.platform.registry.api.web.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.konkerlabs.platform.registry.api.model.RestDestinationInputVO;
import com.konkerlabs.platform.registry.api.model.RestDestinationVO;
import com.konkerlabs.platform.registry.api.model.RestResponse;
import com.konkerlabs.platform.registry.business.model.RestDestination;
import com.konkerlabs.platform.registry.business.model.RestDestination.Validations;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.services.api.RestDestinationService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@Scope("request")
@RequestMapping(value = "/restDestinations")
@Api(tags = "restDestinations")
public class RestDestinationController implements InitializingBean {

    @Autowired
    private RestDestinationService restDestinationService;

    @Autowired
    private User user;

    private Set<String> validationsCode = new HashSet<>();

    @GetMapping(path = "/")
    @PreAuthorize("hasAuthority('LIST_REST_DESTINATIONS')")
    @ApiOperation(
            value = "List all rest destinations by organization",
            response = RestDestinationVO.class)
    public List<RestDestinationVO> list() throws BadServiceResponseException {

        Tenant tenant = user.getTenant();

        ServiceResponse<List<RestDestination>> restDestinationResponse = restDestinationService.findAll(tenant);

        if (!restDestinationResponse.isOk()) {
            throw new BadServiceResponseException(user, restDestinationResponse, validationsCode);
        } else {
            return new RestDestinationVO().apply(restDestinationResponse.getResult());
        }

    }

    @GetMapping(path = "/{restDestinationGuid}")
    @ApiOperation(
            value = "Get a rest destination by guid",
            response = RestResponse.class
    )
    @PreAuthorize("hasAuthority('SHOW_REST_DESTINATION')")
    public RestDestinationVO read(@PathVariable("restDestinationGuid") String restDestinationGuid) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();

        ServiceResponse<RestDestination> restDestinationResponse = restDestinationService.getByGUID(tenant, restDestinationGuid);

        if (!restDestinationResponse.isOk()) {
            throw new NotFoundResponseException(user, restDestinationResponse);
        } else {
            return new RestDestinationVO().apply(restDestinationResponse.getResult());
        }

    }

    @PostMapping
    @ApiOperation(value = "Create a rest destination")
    @PreAuthorize("hasAuthority('CREATE_REST_DESTINATION')")
    public RestDestinationVO create(
            @ApiParam(name = "body", required = true)
            @RequestBody RestDestinationInputVO restDestinationForm) throws BadServiceResponseException {

        Tenant tenant = user.getTenant();

        RestDestination restDestination = RestDestination.builder()
                .name(restDestinationForm.getName())
                .method(restDestinationForm.getMethod())
                .headers(restDestinationForm.getHeaders())
                .serviceURI(restDestinationForm.getServiceURI())
                .serviceUsername(restDestinationForm.getServiceUsername())
                .servicePassword(restDestinationForm.getServicePassword())
                .active(true)
                .build();

        ServiceResponse<RestDestination> restDestinationResponse = restDestinationService.register(tenant, restDestination);

        if (!restDestinationResponse.isOk()) {
            throw new BadServiceResponseException(user, restDestinationResponse, validationsCode);
        } else {
            return new RestDestinationVO().apply(restDestinationResponse.getResult());
        }

    }

    @PutMapping(path = "/{restDestinationGuid}")
    @ApiOperation(value = "Update a rest destination")
    @PreAuthorize("hasAuthority('EDIT_REST_DESTINATION')")
    public void update(
            @PathVariable("restDestinationGuid") String restDestinationGuid,
            @ApiParam(name = "body", required = true)
            @RequestBody RestDestinationInputVO restDestinationForm) throws BadServiceResponseException {

        Tenant tenant = user.getTenant();

        RestDestination restDestinationFromDB = null;
        ServiceResponse<RestDestination> restDestinationResponse = restDestinationService.getByGUID(tenant, restDestinationGuid);

        if (!restDestinationResponse.isOk()) {
            throw new BadServiceResponseException(user, restDestinationResponse, validationsCode);
        } else {
            restDestinationFromDB = restDestinationResponse.getResult();
        }

        // update fields
        restDestinationFromDB.setName(restDestinationForm.getName());
        restDestinationFromDB.setMethod(restDestinationForm.getMethod());
        restDestinationFromDB.setHeaders(restDestinationForm.getHeaders());
        restDestinationFromDB.setServiceURI(restDestinationForm.getServiceURI());
        restDestinationFromDB.setServiceUsername(restDestinationForm.getServiceUsername());
        restDestinationFromDB.setServicePassword(restDestinationForm.getServicePassword());
        restDestinationFromDB.setActive(restDestinationForm.isActive());

        ServiceResponse<RestDestination> updateResponse = restDestinationService.update(tenant, restDestinationGuid, restDestinationFromDB);

        if (!updateResponse.isOk()) {
            throw new BadServiceResponseException(user, restDestinationResponse, validationsCode);

        }

    }

    @DeleteMapping(path = "/{restDestinationGuid}")
    @ApiOperation(value = "Delete a rest destination")
    @PreAuthorize("hasAuthority('REMOVE_REST_DESTINATION')")
    public void delete(@PathVariable("restDestinationGuid") String restDestinationGuid) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();

        ServiceResponse<RestDestination> restDestinationResponse = restDestinationService.remove(tenant, restDestinationGuid);

        if (!restDestinationResponse.isOk()) {
            if (restDestinationResponse.getResponseMessages().containsKey(RestDestinationService.Validations.DESTINATION_NOT_FOUND.getCode())) {
                throw new NotFoundResponseException(user, restDestinationResponse);
            } else {
                throw new BadServiceResponseException(user, restDestinationResponse, validationsCode);
            }
        }

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        for (Validations value : Validations.values()) {
            validationsCode.add(value.getCode());
        }

        for (RestDestinationService.Validations value : RestDestinationService.Validations.values()) {
            validationsCode.add(value.getCode());
        }

    }

}
