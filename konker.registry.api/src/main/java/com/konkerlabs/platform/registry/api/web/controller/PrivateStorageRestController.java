package com.konkerlabs.platform.registry.api.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.konkerlabs.platform.registry.api.exceptions.BadServiceResponseException;
import com.konkerlabs.platform.registry.api.exceptions.NotFoundResponseException;
import com.konkerlabs.platform.registry.api.model.RestResponse;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.PrivateStorageService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.storage.model.PrivateStorage;
import com.mongodb.util.JSON;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@Scope("request")
@RequestMapping(
        value = "/{application}/privateStorage"
)
@Api(tags = "private storage")
public class PrivateStorageRestController extends AbstractRestController implements InitializingBean {

    private final PrivateStorageService privateStorageService;

    private Set<String> validationsCode = new HashSet<>();

    public static final String NOTES =
            "### Post Example\n\n" +
                    "\t{\n" +
                    "\t \"_id\": \"818599ad-0000-0000-0000-000000000000\",\n" +
                    "\t \"customer\": \"Konker\",\n" +
                    "\t \"email\": \"konker@konkerlabs.com\",\n" +
                    "\t \"address\": \"Avenida Brigadeiro Faria Lima\"\n" +
                    "\t}\n";

    @Autowired
    public PrivateStorageRestController(PrivateStorageService privateStorageService) {
        this.privateStorageService = privateStorageService;
    }

    @GetMapping(path = "/collections")
    @ApiOperation(
            value = "List all collections name by application",
            response = RestResponse.class
    )
    @PreAuthorize("hasAuthority('SHOW_PRIVATE_STORAGE')")
    public Object listCollectionsName(
            @PathVariable("application") String applicationId) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        ServiceResponse<Set<String>> response = privateStorageService.listCollections(tenant, application);

        if (!response.isOk()) {
            throw new NotFoundResponseException(response);
        } else {
            return response.getResult();
        }

    }

    @GetMapping(path = "/{collectionName}")
    @ApiOperation(value = "List all data from collection")
    @PreAuthorize("hasAuthority('SHOW_PRIVATE_STORAGE')")
    public Object list(
            @PathVariable("application") String applicationId,
            @PathVariable("collectionName") String collection) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        ServiceResponse<List<PrivateStorage>> response = null;
        try {
            response = privateStorageService.findAll(tenant, application, collection);

            if (!response.isOk()) {
                throw new NotFoundResponseException(response);
            } else {
                return response.getResult()
                        .stream()
                        .map(p -> JSON.parse(p.getCollectionContent()))
                        .collect(Collectors.toList());
            }
        } catch (JsonProcessingException e) {
            throw new NotFoundResponseException(response);
        }
    }

    @GetMapping(path = "/{collectionName}/{key}")
    @ApiOperation(value = "Get a data by collection and key")
    @PreAuthorize("hasAuthority('SHOW_PRIVATE_STORAGE')")
    public Object read(
    		@PathVariable("application") String applicationId,
    		@PathVariable("collectionName") String collection,
    		@PathVariable("key") String key) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        ServiceResponse<PrivateStorage> response = null;
        try {
            response = privateStorageService.findById(tenant, application, collection, key);

            if (!response.isOk()) {
                throw new NotFoundResponseException(response);
            } else {
                return JSON.parse(
                        Optional.ofNullable(response.getResult())
                                .orElse(PrivateStorage.builder().build()).getCollectionContent());
            }
        } catch (JsonProcessingException e) {
            throw new NotFoundResponseException(response);
        }


    }

    @PostMapping(path = "/{collectionName}")
    @ApiOperation(value = "Create a data in collection", notes = NOTES)
    @PreAuthorize("hasAuthority('ADD_PRIVATE_STORAGE')")
    public Object create(
    		@PathVariable("application") String applicationId,
            @PathVariable("collectionName") String collection,
            @ApiParam(name = "body", required = true)
    		@RequestBody String collectionContent) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        ServiceResponse<PrivateStorage> response = null;
        try {
            response = privateStorageService.save(tenant, application, collection, collectionContent);

            if (!response.isOk()) {
                throw new BadServiceResponseException( response, validationsCode);
            } else {
                return JSON.parse(response.getResult().getCollectionContent());
            }
        } catch (JsonProcessingException e) {
            throw new NotFoundResponseException(response);
        }

    }

    @PutMapping(path = "/{collectionName}")
    @ApiOperation(value = "Update a data in collection", notes = NOTES)
    @PreAuthorize("hasAuthority('EDIT_PRIVATE_STORAGE')")
    public void update(
    		@PathVariable("application") String applicationId,
            @PathVariable("collectionName") String collection,
            @ApiParam(name = "body", required = true)
            @RequestBody String collectionContent) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        ServiceResponse<PrivateStorage> response = null;
        try {
            response = privateStorageService.update(tenant, application, collection, collectionContent);

            if (!response.isOk()) {
                throw new BadServiceResponseException( response, validationsCode);
            }
        } catch (JsonProcessingException e) {
            throw new NotFoundResponseException(response);
        }

    }

    @DeleteMapping(path = "/{collectionName}/{key}")
    @ApiOperation(value = "Delete a data in collection")
    @PreAuthorize("hasAuthority('REMOVE_PRIVATE_STORAGE')")
    public void delete(
    		@PathVariable("application") String applicationId,
            @PathVariable("collectionName") String collection,
            @PathVariable("key") String key) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        ServiceResponse<PrivateStorage> response = null;
        try {
            response = privateStorageService.remove(tenant, application, collection, key);

            if (!response.isOk()) {
                if (response.getResponseMessages().containsKey(PrivateStorageService.Validations.PRIVATE_STORAGE_COLLECTION_CONTENT_DOES_NOT_EXIST.getCode())) {
                    throw new NotFoundResponseException(response);
                } else {
                    throw new BadServiceResponseException( response, validationsCode);
                }
            }
        } catch (JsonProcessingException e) {
            throw new NotFoundResponseException(response);
        }
    }

    @Override
    public void afterPropertiesSet() {

        for (PrivateStorageService.Validations value : PrivateStorageService.Validations.values()) {
            validationsCode.add(value.getCode());
        }

        for (PrivateStorageService.Messages value : PrivateStorageService.Messages.values()) {
            validationsCode.add(value.getCode());
        }

    }

}
