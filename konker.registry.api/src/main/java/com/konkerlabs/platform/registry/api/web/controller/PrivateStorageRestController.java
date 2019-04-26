package com.konkerlabs.platform.registry.api.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.konkerlabs.platform.registry.api.exceptions.BadServiceResponseException;
import com.konkerlabs.platform.registry.api.exceptions.NotFoundResponseException;
import com.konkerlabs.platform.registry.api.model.RestResponse;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
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

import java.util.*;
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

    public static final String SEARCH_NOTES =
            "### Query Search Example\n\n" +
                    "* _id:818599ad-0000-0000-0000-000000000000\n\n" +
                    "* customer:Konker\n\n" +
                    "* email:konker@konkerlabs.com\n\n" +
                    "* customer:Konker email:konker@konkerlabs.com\n\n";

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

        ServiceResponse<List<String>> response = privateStorageService.listCollections(tenant, application, user.getParentUser());

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
            response = privateStorageService.findAll(tenant, application, user.getParentUser(), collection);

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

    @GetMapping(path = "/{collectionName}/search")
    @ApiOperation(value = "Get a data by collection and query", notes = SEARCH_NOTES)
    @PreAuthorize("hasAuthority('SHOW_PRIVATE_STORAGE')")
    public Object searchByQuery(
    		@PathVariable("application") String applicationId,
    		@PathVariable("collectionName") String collection,
            @ApiParam(value = "Query string", example = "_id:818599ad-3502-4e70-a852-fc7af8e0a9f4")
            @RequestParam(required = false, defaultValue = "", name = "q") String query,
            @ApiParam(value = "Page number")
            @RequestParam(required = false, defaultValue = "0") int pageNumber,
            @ApiParam(value = "Number of elements per page")
            @RequestParam(required = false, defaultValue = "10") int pageSize) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        ServiceResponse<List<PrivateStorage>> response = null;
        try {
            Map<String, String> queryParam = new HashMap<>();

            if (query.length() > 0) {
                String[] keysQuery = query.split("&");
                queryParam = Arrays.stream(keysQuery)
                        .map(k -> k.split("="))
                        .collect(Collectors.toMap(k -> k[0], k -> k[1]));
            }

            response = privateStorageService.findByQuery(tenant, application, user.getParentUser(), collection, queryParam, pageNumber, pageSize);

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
            response = privateStorageService.save(tenant, application, user.getParentUser(), collection, collectionContent);

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
            response = privateStorageService.update(tenant, application, user.getParentUser(), collection, collectionContent);

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
            response = privateStorageService.remove(tenant, application, user.getParentUser(), collection, key);

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

        for (ApplicationService.Validations value : ApplicationService.Validations.values()) {
            validationsCode.add(value.getCode());
        }

    }

}
