package com.konkerlabs.platform.registry.api.web.controller;

import java.util.HashSet;
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
import com.konkerlabs.platform.registry.api.model.RestResponse;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.ApplicationDocumentStore;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.ApplicationDocumentStoreService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.mongodb.util.JSON;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@Scope("request")
@RequestMapping(
        value = "/{application}/documentStore/{collection}/{key}"
)
@Api(tags = "application document store")
public class ApplicationDocumentStoreRestController extends AbstractRestController implements InitializingBean {

    private final ApplicationDocumentStoreService applicationDocumentStoreService;

    private Set<String> validationsCode = new HashSet<>();

    @Autowired
    public ApplicationDocumentStoreRestController(ApplicationDocumentStoreService applicationDocumentStoreService) {
        this.applicationDocumentStoreService = applicationDocumentStoreService;
    }

    @GetMapping
    @ApiOperation(
            value = "Get a application document by collection and key",
            response = RestResponse.class
    )
    @PreAuthorize("hasAuthority('SHOW_APPLICATION')")
    public Object read(
    		@PathVariable("application") String applicationId,
    		@PathVariable("collection") String collection,
    		@PathVariable("key") String key) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        ServiceResponse<ApplicationDocumentStore> deviceResponse = applicationDocumentStoreService.findUniqueByTenantApplication(tenant, application, collection, key);

        if (!deviceResponse.isOk()) {
            throw new NotFoundResponseException(deviceResponse);
        } else {
            return JSON.parse(deviceResponse.getResult().getJson());
        }

    }

    @PostMapping
    @ApiOperation(value = "Create a application document")
    @PreAuthorize("hasAuthority('ADD_APPLICATION')")
    public Object create(
    		@PathVariable("application") String applicationId,
            @PathVariable("collection") String collection,
            @PathVariable("key") String key,
            @ApiParam(name = "body", required = true)
    		@RequestBody String jsonCustomData) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        ServiceResponse<ApplicationDocumentStore> deviceResponse = applicationDocumentStoreService.save(tenant, application, collection, key, jsonCustomData);

        if (!deviceResponse.isOk()) {
            throw new BadServiceResponseException( deviceResponse, validationsCode);
        } else {
            return JSON.parse(deviceResponse.getResult().getJson());
        }

    }

    @PutMapping
    @ApiOperation(value = "Update a application document")
    @PreAuthorize("hasAuthority('EDIT_APPLICATION')")
    public void update(
    		@PathVariable("application") String applicationId,
            @PathVariable("collection") String collection,
            @PathVariable("key") String key,
            @ApiParam(name = "body", required = true)
            @RequestBody String jsonCustomData) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        ServiceResponse<ApplicationDocumentStore> updateResponse = applicationDocumentStoreService.update(tenant, application, collection, key, jsonCustomData);

        if (!updateResponse.isOk()) {
            throw new BadServiceResponseException( updateResponse, validationsCode);

        }

    }

    @DeleteMapping
    @ApiOperation(value = "Delete a application document")
    @PreAuthorize("hasAuthority('REMOVE_APPLICATION')")
    public void delete(
    		@PathVariable("application") String applicationId,
            @PathVariable("collection") String collection,
            @PathVariable("key") String key) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        ServiceResponse<ApplicationDocumentStore> deviceResponse = applicationDocumentStoreService.remove(tenant, application, collection, key);

        if (!deviceResponse.isOk()) {
            if (deviceResponse.getResponseMessages().containsKey(ApplicationDocumentStoreService.Validations.APP_DOCUMENT_DOES_NOT_EXIST.getCode())) {
                throw new NotFoundResponseException(deviceResponse);
            } else {
                throw new BadServiceResponseException( deviceResponse, validationsCode);
            }
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
