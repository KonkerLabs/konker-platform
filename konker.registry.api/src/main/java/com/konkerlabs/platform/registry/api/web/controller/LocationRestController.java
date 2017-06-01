package com.konkerlabs.platform.registry.api.web.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
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
import com.konkerlabs.platform.registry.api.model.DeviceVO;
import com.konkerlabs.platform.registry.api.model.LocationInputVO;
import com.konkerlabs.platform.registry.api.model.LocationVO;
import com.konkerlabs.platform.registry.api.model.RestResponse;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Location;
import com.konkerlabs.platform.registry.business.model.Location.Validations;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.LocationSearchService;
import com.konkerlabs.platform.registry.business.services.api.LocationService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@Scope("request")
@RequestMapping(value = "/{application}/locations")
@Api(tags = "locations")
public class LocationRestController extends AbstractRestController implements InitializingBean {

    @Autowired
    private LocationService locationService;

    @Autowired
    private LocationSearchService locationSearchService;

    private Set<String> validationsCode = new HashSet<>();

    @GetMapping(path = "/")
    @PreAuthorize("hasAuthority('LIST_LOCATIONS')")
    @ApiOperation(
            value = "List all locations by application",
            response = LocationVO.class)
    public LocationVO list(@PathVariable("application") String applicationId) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        ServiceResponse<Location> locationResponse = locationSearchService.findRoot(tenant, application);

        if (!locationResponse.isOk()) {
            throw new BadServiceResponseException(user, locationResponse, validationsCode);
        } else {
            return new LocationVO(locationResponse.getResult());
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
            @PathVariable("locationName") String locationName) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        ServiceResponse<Location> locationResponse = locationSearchService.findByName(tenant, application, locationName, true);

        if (!locationResponse.isOk()) {
            throw new NotFoundResponseException(user, locationResponse);
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
            @PathVariable("locationName") String locationName) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        ServiceResponse<List<Device>> locationResponse = locationSearchService.listDevicesByLocationName(tenant, application, locationName);

        if (!locationResponse.isOk()) {
            throw new BadServiceResponseException(user, locationResponse, validationsCode);
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
            @RequestBody LocationInputVO locationForm) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        Location parent = getParent(tenant, application, locationForm);

        Location location = Location.builder()
                .parent(parent)
                .name(locationForm.getName())
                .description(locationForm.getDescription())
                .defaultLocation(locationForm.isDefaultLocation())
                .build();

        ServiceResponse<Location> locationResponse = locationService.save(tenant, application, location);

        if (!locationResponse.isOk()) {
            throw new BadServiceResponseException(user, locationResponse, validationsCode);
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
            @RequestBody LocationInputVO locationForm) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        Location parent = getParent(tenant, application, locationForm);

        Location locationFromDB = null;
        ServiceResponse<Location> locationResponse = locationSearchService.findByName(tenant, application, locationName, false);

        if (!locationResponse.isOk()) {
            throw new BadServiceResponseException(user, locationResponse, validationsCode);
        } else {
            locationFromDB = locationResponse.getResult();
        }

        // update fields
        locationFromDB.setParent(parent);
        locationFromDB.setName(locationForm.getName());
        locationFromDB.setDescription(locationForm.getDescription());
        locationFromDB.setDefaultLocation(locationForm.isDefaultLocation());

        ServiceResponse<Location> updateResponse = locationService.update(tenant, application, locationFromDB.getGuid(), locationFromDB);

        if (!updateResponse.isOk()) {
            throw new BadServiceResponseException(user, locationResponse, validationsCode);
        }

        // update childrens (subtree)
        List<Location> sublocations = getSublocationsFromVO(locationForm.getSublocations());

        if (sublocations != null) {
            updateResponse = locationService.updateSubtree(tenant, application, locationFromDB.getGuid(), sublocations);

            if (!updateResponse.isOk()) {
                throw new BadServiceResponseException(user, updateResponse, validationsCode);
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
                       .childrens(getSublocationsFromVO(locationVO.getSublocations()))
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

        Location parent = null;

        ServiceResponse<Location> parentResponse = locationSearchService.findByName(tenant, application, locationForm.getParentName(), false);
        if (!parentResponse.isOk()) {
            if (parentResponse.getResponseMessages().containsKey(LocationService.Messages.LOCATION_NOT_FOUND.getCode())) {
                Map<String, Object[]> responseMessages = new HashMap<>();
                responseMessages.put(LocationService.Validations.LOCATION_PARENT_NOT_FOUND.getCode(), null);

                throw new BadServiceResponseException(user, responseMessages, validationsCode);
            } else {
                throw new BadServiceResponseException(user, parentResponse, validationsCode);
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
            @PathVariable("locationName") String locationName) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        ServiceResponse<Location> locationResponse = locationSearchService.findByName(tenant, application, locationName, false);

        if (!locationResponse.isOk()) {
            throw new NotFoundResponseException(user, locationResponse);
        }

        Location location = locationResponse.getResult();
        locationResponse = locationService.remove(tenant, application, location.getGuid());

        if (!locationResponse.isOk()) {
            throw new BadServiceResponseException(user, locationResponse, validationsCode);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        for (Validations value : Validations.values()) {
            validationsCode.add(value.getCode());
        }

        for (LocationService.Validations value : LocationService.Validations.values()) {
            validationsCode.add(value.getCode());
        }
    }

}
