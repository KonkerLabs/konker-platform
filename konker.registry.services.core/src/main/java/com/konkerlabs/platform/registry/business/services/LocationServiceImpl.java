package com.konkerlabs.platform.registry.business.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Location;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.DeviceRepository;
import com.konkerlabs.platform.registry.business.repositories.LocationRepository;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.LocationService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class LocationServiceImpl implements LocationService {

    private Logger LOGGER = LoggerFactory.getLogger(LocationServiceImpl.class);

    private static final String DEFAULT_ROOT_NAME = "root";

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Override
    public ServiceResponse<Location> save(Tenant tenant, Application application, Location location) {

        if (!Optional.ofNullable(tenant).isPresent()) {
            return ServiceResponseBuilder.<Location>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode())
                    .build();
        }

        if (!Optional.ofNullable(application).isPresent()) {
        	return ServiceResponseBuilder.<Location>error()
        			.withMessage(ApplicationService.Validations.APPLICATION_NULL.getCode())
        			.build();
        }

        if (!Optional.ofNullable(location).isPresent()) {
            return ServiceResponseBuilder.<Location>error()
                    .withMessage(CommonValidations.RECORD_NULL.getCode())
                    .build();
        }

        location.setTenant(tenant);
        location.setApplication(application);
        location.setGuid(UUID.randomUUID().toString());

        Optional<Map<String, Object[]>> validations = location.applyValidations();

        if (validations.isPresent()) {
            return ServiceResponseBuilder.<Location>error()
                    .withMessages(validations.get())
                    .build();
        }

        if (locationRepository.findByTenantAndApplicationAndName(tenant.getId(), application.getName(), location.getName()) != null) {
            return ServiceResponseBuilder.<Location>error()
                    .withMessage(Validations.LOCATION_NAME_ALREADY_REGISTERED.getCode())
                    .build();
        }

        LOGGER.info("Location created. Id: {}", location.getId(), tenant.toURI(), tenant.getLogLevel());

        Location saved = locationRepository.save(location);

        return ServiceResponseBuilder.<Location>ok().withResult(saved).build();
    }

    @Override
    public ServiceResponse<List<Location>> findAll(Tenant tenant, Application application) {

        if (!Optional.ofNullable(tenant).isPresent()) {
            return ServiceResponseBuilder.<List<Location>>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode())
                    .build();
        }

        if (!Optional.ofNullable(application).isPresent()) {
            return ServiceResponseBuilder.<List<Location>>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_NULL.getCode())
                    .build();
        }

        List<Location> all = locationRepository.findAllByTenantIdAndApplicationName(tenant.getId(), application.getName());
        return ServiceResponseBuilder.<List<Location>>ok().withResult(all).build();
    }

    @Override
    public ServiceResponse<Location> update(Tenant tenant, Application application, String guid, Location updatingLocation) {
        if (!Optional.ofNullable(tenant).isPresent())
            return ServiceResponseBuilder.<Location>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode())
                    .build();

        if (!Optional.ofNullable(application).isPresent()) {
        	return ServiceResponseBuilder.<Location>error()
        			.withMessage(ApplicationService.Validations.APPLICATION_NULL.getCode())
        			.build();
        }

        if (!Optional.ofNullable(guid).isPresent())
            return ServiceResponseBuilder.<Location>error()
                    .withMessage(Validations.LOCATION_GUID_NULL.getCode())
                    .build();

        if (!Optional.ofNullable(updatingLocation).isPresent())
            return ServiceResponseBuilder.<Location>error()
                    .withMessage(CommonValidations.RECORD_NULL.getCode())
                    .build();

        Location locationFromDB = locationRepository.findByTenantAndApplicationAndGuid(tenant.getId(), application.getName(), guid);
        if (locationFromDB == null) {
            return ServiceResponseBuilder.<Location>error()
                    .withMessage(Validations.LOCATION_GUID_DOES_NOT_EXIST.getCode())
                    .build();
        }

        // modify "modifiable" fields
        locationFromDB.setDescription(updatingLocation.getDescription());
        locationFromDB.setName(updatingLocation.getName());
        locationFromDB.setDefaultLocation(updatingLocation.isDefaultLocation());

        Optional<Map<String, Object[]>> validations = locationFromDB.applyValidations();

        if (validations.isPresent()) {
            return ServiceResponseBuilder.<Location>error()
                    .withMessages(validations.get())
                    .build();
        }

        Location saved = locationRepository.save(locationFromDB);

        LOGGER.info("Location updated. Id: {}", locationFromDB.getId(), tenant.toURI(), tenant.getLogLevel());

        return ServiceResponseBuilder.<Location>ok()
                .withResult(saved)
                .build();
    }

    @Override
    public ServiceResponse<Location> remove(Tenant tenant, Application application, String guid) {

        if (!Optional.ofNullable(tenant).isPresent()) {
            return ServiceResponseBuilder.<Location>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode())
                    .build();
        }

        if (!Optional.ofNullable(application).isPresent()) {
            return ServiceResponseBuilder.<Location>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_NULL.getCode())
                    .build();
        }

        if(!Optional.ofNullable(guid).isPresent()) {
            return ServiceResponseBuilder.<Location>error()
                    .withMessage(Validations.LOCATION_GUID_NULL.getCode())
                    .build();
        }

        //find location
        Location location = locationRepository.findByTenantAndApplicationAndGuid(tenant.getId(), application.getName(), guid);

        if(!Optional.ofNullable(location).isPresent()){
            return ServiceResponseBuilder.<Location>error()
                    .withMessage(Validations.LOCATION_GUID_DOES_NOT_EXIST.getCode())
                    .build();
        }

        //find dependencies
        List<Device> devices =
                deviceRepository.findAllByTenantIdAndApplicationNameAndLocationName(tenant.getId(), application.getName(), location.getId());

        ServiceResponse<Location> response = null;

        if(Optional.ofNullable(devices).isPresent() && !devices.isEmpty()) {
            response = ServiceResponseBuilder.<Location>error()
                    .withMessage(Validations.LOCATION_HAVE_DEVICES.getCode())
                    .build();
        }

        if(Optional.ofNullable(response).isPresent()) return response;

        LOGGER.info("Location removed. Id: {}", location.getId(), tenant.toURI(), tenant.getLogLevel());

        return ServiceResponseBuilder.<Location>ok()
                .withMessage(LocationService.Messages.LOCATION_REMOVED_SUCCESSFULLY.getCode())
                .withResult(location)
                .build();
    }

    @Override
    public ServiceResponse<Location> findTree(Tenant tenant, Application application) {

        if (!Optional.ofNullable(tenant).isPresent())
            return ServiceResponseBuilder.<Location>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode())
                    .build();

        if (!Optional.ofNullable(application).isPresent())
            return ServiceResponseBuilder.<Location>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_NULL.getCode())
                    .build();

        Location root = null;

        List<Location> all = locationRepository.findAllByTenantIdAndApplicationName(tenant.getId(), application.getName());

        Map<String, List<Location>> childrenListMap = new HashMap<>();

        for (Location location: all) {
            if (location.getParent() == null) {
                root = location;
            } else {
                String parentGuid = location.getParent().getGuid();
                List<Location> childrens = childrenListMap.get(parentGuid);
                if (childrens == null) {
                    childrens = new ArrayList<>();
                    childrenListMap.put(parentGuid, childrens);
                }
                childrens.add(location);
             }
        }

        for (Location location: all) {
            List<Location> childrens = childrenListMap.get(location.getGuid());
            if (childrens == null) {
                childrens = new ArrayList<>();
            }
            location.setChildrens(childrens);
        }

        if (root == null) {
            return ServiceResponseBuilder.<Location>error()
                    .withMessage(Messages.LOCATION_ROOT_NOT_FOUND.getCode())
                    .build();
        } else {
            return ServiceResponseBuilder.<Location>ok()
                    .withResult(root)
                    .build();
        }

    }

    @Override
    public ServiceResponse<Location> findByName(Tenant tenant, Application application, String locationName) {

        if (!Optional.ofNullable(tenant).isPresent())
            return ServiceResponseBuilder.<Location>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode())
                    .build();

        if (!Optional.ofNullable(application).isPresent())
            return ServiceResponseBuilder.<Location>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_NULL.getCode())
                    .build();

        //find location
        Location location = locationRepository.findByTenantAndApplicationAndName(tenant.getId(), application.getName(), locationName);

        if (Optional.ofNullable(location).isPresent()) {
            return ServiceResponseBuilder.<Location>ok()
                    .withResult(location)
                    .build();
        } else {
            return ServiceResponseBuilder.<Location>error()
                    .withMessage(Messages.LOCATION_NOT_FOUND.getCode())
                    .build();
        }

    }

    @Override
    public ServiceResponse<Location> findByGuid(Tenant tenant, Application application, String guid) {

        if (!Optional.ofNullable(tenant).isPresent())
            return ServiceResponseBuilder.<Location>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode())
                    .build();

        if (!Optional.ofNullable(application).isPresent())
            return ServiceResponseBuilder.<Location>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_NULL.getCode())
                    .build();

        //find location
        Location location = locationRepository.findByTenantAndApplicationAndGuid(tenant.getId(), application.getName(), guid);

        if (Optional.ofNullable(location).isPresent()) {
            return ServiceResponseBuilder.<Location>ok()
                    .withResult(location)
                    .build();
        } else {
            return ServiceResponseBuilder.<Location>error()
                    .withMessage(Messages.LOCATION_NOT_FOUND.getCode())
                    .build();
        }

    }

    @Override
    public ServiceResponse<Location> findDefault(Tenant tenant, Application application) {

        if (!Optional.ofNullable(tenant).isPresent())
            return ServiceResponseBuilder.<Location>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode())
                    .build();

        if (!Optional.ofNullable(application).isPresent())
            return ServiceResponseBuilder.<Location>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_NULL.getCode())
                    .build();

        List<Location> all = locationRepository.findAllByTenantIdAndApplicationName(tenant.getId(), application.getName());
        for (Location location: all) {
            if (location.isDefaultLocation()) {
                return ServiceResponseBuilder.<Location>ok()
                        .withResult(location)
                        .build();
            }
        }

        if (!all.isEmpty()) {
            return ServiceResponseBuilder.<Location>error()
                    .withMessage(Messages.LOCATION_DEFAULT_NOT_FOUND.getCode())
                    .build();
        } else {
            return ServiceResponseBuilder.<Location>ok()
                    .withResult(getRootDefaultLocation(tenant, application))
                    .build();
        }

    }

    @Override
    public ServiceResponse<Location> findRoot(Tenant tenant, Application application) {

        if (!Optional.ofNullable(tenant).isPresent())
            return ServiceResponseBuilder.<Location>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode())
                    .build();

        if (!Optional.ofNullable(application).isPresent())
            return ServiceResponseBuilder.<Location>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_NULL.getCode())
                    .build();

        List<Location> all = locationRepository.findAllByTenantIdAndApplicationName(tenant.getId(), application.getName());
        for (Location location: all) {
            if (location.getParent() == null) {
                return ServiceResponseBuilder.<Location>ok()
                        .withResult(location)
                        .build();
            }
        }

        if (!all.isEmpty()) {
            return ServiceResponseBuilder.<Location>error()
                    .withMessage(Messages.LOCATION_ROOT_NOT_FOUND.getCode())
                    .build();
        } else {
            return ServiceResponseBuilder.<Location>ok()
                    .withResult(getRootDefaultLocation(tenant, application))
                    .build();
        }

    }

    private Location getRootDefaultLocation(Tenant tenant, Application application) {

        Location root = Location.builder()
                                .tenant(tenant)
                                .application(application)
                                .guid(UUID.randomUUID().toString())
                                .defaultLocation(true)
                                .name(DEFAULT_ROOT_NAME)
                                .build();

        locationRepository.save(root);

        return root;

    }

}
