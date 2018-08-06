package com.konkerlabs.platform.registry.api.web.controller;

import com.konkerlabs.platform.registry.api.exceptions.BadServiceResponseException;
import com.konkerlabs.platform.registry.api.exceptions.NotAuthorizedResponseException;
import com.konkerlabs.platform.registry.api.exceptions.NotFoundResponseException;
import com.konkerlabs.platform.registry.api.model.DeviceVO;
import com.konkerlabs.platform.registry.api.model.LocationInputVO;
import com.konkerlabs.platform.registry.api.model.LocationVO;
import com.konkerlabs.platform.registry.api.model.RestResponse;
import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.model.Location.Validations;
import com.konkerlabs.platform.registry.business.services.api.GatewayService;
import com.konkerlabs.platform.registry.business.services.api.LocationSearchService;
import com.konkerlabs.platform.registry.business.services.api.LocationService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@Scope("request")
@RequestMapping(value = "/{application}/locations")
@Api(tags = "locations")
public class LocationRestController extends AbstractRestController implements InitializingBean {

    private final LocationService locationService;

    private final LocationSearchService locationSearchService;

    @Autowired
    private OauthClientDetails oauthClientDetails;

    @Autowired
    private GatewayService gatewayService;

    private Set<String> validationsCode = new HashSet<>();

    @Autowired
    public LocationRestController(LocationService locationService, LocationSearchService locationSearchService) {
        this.locationService = locationService;
        this.locationSearchService = locationSearchService;
    }

    @GetMapping(path = "/")
    @PreAuthorize("hasAuthority('LIST_LOCATIONS')")
    @ApiOperation(
            value = "List all locations by application",
            response = LocationVO.class)
    public List<LocationVO> list(@PathVariable("application") String applicationId)
            throws BadServiceResponseException, NotFoundResponseException {

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
        ServiceResponse<List<Location>> locationResponse = null;

        if(gateway != null){
            locationResponse = locationSearchService.findAll(gateway, tenant, application);
        } else {
            locationResponse = locationSearchService.findAll(tenant, application);
        }

        if (!locationResponse.isOk()) {
            throw new BadServiceResponseException( locationResponse, validationsCode);
        } else {
            return new LocationVO().apply(locationResponse.getResult());
        }

    }

    @GetMapping(path = "/{locationName}")
    @ApiOperation(
            value = "Get a location by name",
            response = RestResponse.class
    )
    @PreAuthorize("hasAuthority('SHOW_LOCATION')")
    public LocationVO read(
            @PathVariable("application") String applicationId,
            @PathVariable("locationName") String locationName)
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

        if(gateway != null) {
            authorizeGateway(gateway, Location.builder().name(locationName).build());
        }

        ServiceResponse<Location> locationResponse = locationSearchService.findByName(tenant, application, locationName, true);

        if (!locationResponse.isOk()) {
            throw new NotFoundResponseException(locationResponse);
        } else {
            return new LocationVO().apply(locationResponse.getResult());
        }

    }

    @GetMapping(path = "/{locationName}/devices")
    @ApiOperation(
            value = "List the devices of a location and its sub-locations",
            response = RestResponse.class
    )
    @PreAuthorize("hasAuthority('SHOW_LOCATION')")
    public List<DeviceVO> devices(
            @PathVariable("application") String applicationId,
            @PathVariable("locationName") String locationName)
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

        if(gateway != null) {
            authorizeGateway(gateway, Location.builder().name(locationName).build());
        }

        ServiceResponse<List<Device>> locationResponse = locationSearchService.listDevicesByLocationName(tenant, application, locationName);

        if (!locationResponse.isOk()) {
            throw new BadServiceResponseException( locationResponse, validationsCode);
        } else {
            return new DeviceVO().apply(locationResponse.getResult());
        }

    }

    @PostMapping
    @ApiOperation(value = "Create a location")
    @PreAuthorize("hasAuthority('CREATE_LOCATION')")
    public LocationVO create(
            @PathVariable("application") String applicationId,
            @ApiParam(
            		name = "body",
            		value = "JSON filled with the fields described in Model and Example Value beside",
            		required = true)
            @RequestBody LocationInputVO locationForm)
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

        Location parent = getParent(tenant, application, locationForm);

        Location location = Location.builder()
                .parent(parent)
                .name(locationForm.getName())
                .description(locationForm.getDescription())
                .defaultLocation(locationForm.isDefaultLocation())
                .build();

        if(gateway != null) {
            authorizeGateway(gateway, parent);
        }

        ServiceResponse<Location> locationResponse = locationService.save(tenant, application, location);

        if (!locationResponse.isOk()) {
            throw new BadServiceResponseException( locationResponse, validationsCode);
        } else {
            return new LocationVO().apply(locationResponse.getResult());
        }

    }

    @PutMapping(path = "/{locationName}")
    @ApiOperation(value = "Update a location")
    @PreAuthorize("hasAuthority('EDIT_LOCATION')")
    public void update(
            @PathVariable("application") String applicationId,
            @PathVariable("locationName") String locationName,
            @ApiParam(
            		name = "body",
            		value = "JSON filled with the fields described in Model and Example Value beside",
            		required = true)
            @RequestBody LocationInputVO locationForm)
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

        Location parent = getParent(tenant, application, locationForm);

        Location locationFromDB;
        ServiceResponse<Location> locationResponse = locationSearchService.findByName(tenant, application, locationName, false);

        if (!locationResponse.isOk()) {
            throw new BadServiceResponseException( locationResponse, validationsCode);
        } else {
            locationFromDB = locationResponse.getResult();
        }

        if(gateway != null) {
            authorizeGateway(gateway, Location.builder().name(locationName).build());
        }

        // update fields
        locationFromDB.setParent(parent);
        locationFromDB.setName(locationForm.getName());
        locationFromDB.setDescription(locationForm.getDescription());
        locationFromDB.setDefaultLocation(locationForm.isDefaultLocation());

        ServiceResponse<Location> updateResponse = locationService.update(tenant, application, locationFromDB.getGuid(), locationFromDB);

        if (!updateResponse.isOk()) {
            throw new BadServiceResponseException( locationResponse, validationsCode);
        }

        // update childrens (subtree)
        List<Location> sublocations = getSublocationsFromVO(locationForm.getSublocations());

        if (sublocations != null) {
            updateResponse = locationService.updateSubtree(tenant, application, locationFromDB.getGuid(), sublocations);

            if (!updateResponse.isOk()) {
                throw new BadServiceResponseException( updateResponse, validationsCode);
            }
        }

    }

    private List<Location> getSublocationsFromVO(List<LocationVO> sublocationsVO) {

        if (sublocationsVO == null) {
            return null;
        }

        List<Location> sublocations = new ArrayList<>();

        for (LocationVO locationVO : sublocationsVO) {
            sublocations.add(getLocationFromVO(locationVO));
        }

        return sublocations;

    }

    private Location getLocationFromVO(LocationVO locationVO) {
        return Location.builder()
                       .parent(getParentFromVO(locationVO))
                       .name(locationVO.getName())
                       .description(locationVO.getDescription())
                       .children(getSublocationsFromVO(locationVO.getSublocations()))
                       .defaultLocation(locationVO.isDefaultLocation())
                       .build();
    }

    private Location getParentFromVO(LocationVO locationVO) {

        final String parentName = locationVO.getParentName();

        if (!StringUtils.hasText(parentName)) {
            return null;
        } else {
            return Location.builder()
                           .name(parentName)
                           .build();
        }

    }

    private Location getParent(Tenant tenant, Application application, LocationInputVO locationForm)
            throws BadServiceResponseException {

        if (!StringUtils.hasText(locationForm.getParentName())) {
            return null;
        }

        Location parent;

        ServiceResponse<Location> parentResponse = locationSearchService.findByName(tenant, application, locationForm.getParentName(), false);
        if (!parentResponse.isOk()) {
            if (parentResponse.getResponseMessages().containsKey(LocationService.Messages.LOCATION_NOT_FOUND.getCode())) {
                Map<String, Object[]> responseMessages = new HashMap<>();
                responseMessages.put(LocationService.Validations.LOCATION_PARENT_NOT_FOUND.getCode(), null);

                throw new BadServiceResponseException( responseMessages, validationsCode);
            } else {
                throw new BadServiceResponseException( parentResponse, validationsCode);
            }
        } else {
            parent = parentResponse.getResult();
        }

        return parent;

    }

    @DeleteMapping(path = "/{locationName}")
    @ApiOperation(value = "Delete a location")
    @PreAuthorize("hasAuthority('REMOVE_LOCATION')")
    public void delete(
            @PathVariable("application") String applicationId,
            @PathVariable("locationName") String locationName)
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

        ServiceResponse<Location> locationResponse = locationSearchService.findByName(tenant, application, locationName, false);

        if (!locationResponse.isOk()) {
            throw new NotFoundResponseException(locationResponse);
        }

        Location location = locationResponse.getResult();
        if(gateway != null) {
            authorizeGateway(gateway, Location.builder().name(locationName).build());
        }
        locationResponse = locationService.remove(tenant, application, location.getGuid());

        if (!locationResponse.isOk()) {
            throw new BadServiceResponseException( locationResponse, validationsCode);
        }
    }

    @Override
    public void afterPropertiesSet() {
        for (Validations value : Validations.values()) {
            validationsCode.add(value.getCode());
        }

        for (LocationService.Validations value : LocationService.Validations.values()) {
            validationsCode.add(value.getCode());
        }
    }

    private void authorizeGateway(Gateway gateway, Location location) throws NotAuthorizedResponseException {
        if (Optional.ofNullable(gateway).isPresent()) {
            ServiceResponse<Boolean> validationResult =
                    gatewayService.validateGatewayAuthorization(
                            gateway,
                            location
                    );

            if (!validationResult.isOk() || !validationResult.getResult()) {
                throw new NotAuthorizedResponseException(
                        validationResult,
                        validationsCode
                );
            }
        }
    }

}
