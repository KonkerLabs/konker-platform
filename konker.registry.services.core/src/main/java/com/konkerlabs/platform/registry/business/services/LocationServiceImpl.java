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
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
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

    @Autowired
    private MongoTemplate mongoTemplate;

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

        if (location.getParent() == null) {
            if (locationRepository.findRootLocationByTenantAndApplication(tenant.getId(), application.getName()) != null) {
                return ServiceResponseBuilder.<Location>error()
                        .withMessage(Validations.LOCATION_PARENT_NULL.getCode())
                        .build();
            }
        }

        if (location.isDefaultLocation()) {
            setFalseDefaultToAllLocations(tenant, application);
        }

        LOGGER.info("Location created. Id: {}", location.getId(), tenant.toURI(), tenant.getLogLevel());

        Location saved = locationRepository.save(location);

        return ServiceResponseBuilder.<Location>ok()
                .withMessage(LocationService.Messages.LOCATION_REGISTERED_SUCCESSFULLY.getCode())
                .withResult(saved)
                .build();
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

        updatingLocation.setTenant(tenant);
        updatingLocation.setApplication(application);

        Optional<Map<String, Object[]>> validations = updatingLocation.applyValidations();

        if (validations.isPresent()) {
            return ServiceResponseBuilder.<Location>error()
                    .withMessages(validations.get())
                    .build();
        }

        final Location sameNameLocation = locationRepository.findByTenantAndApplicationAndName(tenant.getId(), application.getName(), updatingLocation.getName());
        if (sameNameLocation != null && !sameNameLocation.getGuid().equals(guid)) {
            return ServiceResponseBuilder.<Location>error()
                    .withMessage(Validations.LOCATION_NAME_ALREADY_REGISTERED.getCode())
                    .build();
        }

        if (updatingLocation.getParent() == null) {
            final Location rootLocation = locationRepository.findRootLocationByTenantAndApplication(tenant.getId(), application.getName());
            if (rootLocation != null && !rootLocation.getGuid().equals(guid)) {
                return ServiceResponseBuilder.<Location>error()
                        .withMessage(Validations.LOCATION_PARENT_NULL.getCode())
                        .build();
            }
        }

        if (updatingLocation.isDefaultLocation()) {
            setFalseDefaultToAllLocations(tenant, application);
        }

        // modify "modifiable" fields
        locationFromDB.setDescription(updatingLocation.getDescription());
        locationFromDB.setName(updatingLocation.getName());
        locationFromDB.setDefaultLocation(updatingLocation.isDefaultLocation());
        locationFromDB.setParent(updatingLocation.getParent());

        Location saved = locationRepository.save(locationFromDB);

        LOGGER.info("Location updated. Id: {}", locationFromDB.getId(), tenant.toURI(), tenant.getLogLevel());

        return ServiceResponseBuilder.<Location>ok()
                .withMessage(LocationService.Messages.LOCATION_REGISTERED_SUCCESSFULLY.getCode())
                .withResult(saved)
                .build();
    }

    private void setFalseDefaultToAllLocations(Tenant tenant, Application application) {

        Query query = new Query();
        query.addCriteria(Criteria
                .where("tenant.id").is(tenant.getId())
                .andOperator(Criteria.where("application.name").is(application.getName())));

        Update update = new Update();
        update.set("defaultLocation", false);

        mongoTemplate.updateMulti(query, update, Location.class);

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

        // find location
        Location location = locationRepository.findByTenantAndApplicationAndGuid(tenant.getId(), application.getName(), guid);

        if(!Optional.ofNullable(location).isPresent()){
            return ServiceResponseBuilder.<Location>error()
                    .withMessage(Validations.LOCATION_GUID_DOES_NOT_EXIST.getCode())
                    .build();
        }

        // find dependencies
        List<Device> devices =
                deviceRepository.findAllByTenantIdAndApplicationNameAndLocationName(tenant.getId(), application.getName(), location.getId());

        ServiceResponse<Location> response = null;

        if(!devices.isEmpty()) {
            response = ServiceResponseBuilder.<Location>error()
                    .withMessage(Validations.LOCATION_HAVE_DEVICES.getCode())
                    .build();
        }

        if(Optional.ofNullable(response).isPresent()) return response;

        List<Location> childrens =
                locationRepository.findChildrensByParentId(tenant.getId(), application.getName(), location.getId());

        if(!childrens.isEmpty()) {
            return ServiceResponseBuilder.<Location>error()
                    .withMessage(Validations.LOCATION_HAVE_CHILDRENS.getCode())
                    .build();
        }

        // remove
        locationRepository.delete(location);

        LOGGER.info("Location removed. Id: {}", location.getId(), tenant.toURI(), tenant.getLogLevel());

        return ServiceResponseBuilder.<Location>ok()
                .withMessage(LocationService.Messages.LOCATION_REMOVED_SUCCESSFULLY.getCode())
                .withResult(location)
                .build();
    }

    private Location findTree(Tenant tenant, Application application) {

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

        return root;

    }

    @Override
    public ServiceResponse<Location> findByName(Tenant tenant, Application application, String locationName, boolean loadTree) {

        if (!Optional.ofNullable(tenant).isPresent())
            return ServiceResponseBuilder.<Location>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode())
                    .build();

        if (!Optional.ofNullable(application).isPresent())
            return ServiceResponseBuilder.<Location>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_NULL.getCode())
                    .build();

        // find location
        Location location = null;

        if (!loadTree) {
            location = locationRepository.findByTenantAndApplicationAndName(tenant.getId(), application.getName(), locationName);
        } else {
            Location root = this.findTree(tenant, application);
            location = searchElementByName(root, locationName, 0);
        }

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

    private Location searchElementByName(Location node, String locationName, int deep) {

        if (deep > 50) {
            LOGGER.warn("Too deep structure. Cyclic graph?");
            return null;
        }

        if (node.getName().equals(locationName)) {
            return node;
        }

        for (Location child : node.getChildrens()) {
            Location element = searchElementByName(child, locationName, deep + 1);
            if (element != null) {
                return element;
            }
        }

        return null;

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

        // find location
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

        return ServiceResponseBuilder.<Location>ok()
                .withResult(findRoot(tenant, application).getResult())
                .build();

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

        Location root = findTree(tenant, application);

        if (root != null) {
            return ServiceResponseBuilder.<Location>ok()
                    .withResult(root)
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

    @Override
    public ServiceResponse<List<Device>> listDevicesByLocationName(Tenant tenant, Application application,
            String locationName) {

        List<Device> devices = deviceRepository.findAllByTenantIdAndApplicationName(tenant.getId(), application.getName());
        Location root = this.findTree(tenant, application);
        Location location = searchElementByName(root, locationName, 0);

        if (Optional.ofNullable(location).isPresent()) {
            List<Device> locationDevices = new ArrayList<>();
            locationDevices = searchLocationDevices(location, devices, 0);

            return ServiceResponseBuilder.<List<Device>>ok()
                    .withResult(locationDevices)
                    .build();
        } else {
            return ServiceResponseBuilder.<List<Device>>error()
                    .withMessage(Messages.LOCATION_NOT_FOUND.getCode())
                    .build();
        }

    }

    private List<Device> searchLocationDevices(Location location, List<Device> devices, int deep) {

        List<Device> locationDevices = new ArrayList<>();

        if (deep > 50) {
            LOGGER.warn("Too deep structure. Cyclic graph?");
            return locationDevices;
        }

        for (Device device: devices) {
            if (device.getLocation() != null) {
                if (device.getLocation().getName().equals(location.getName())) {
                    locationDevices.add(device);
                }
            }
        }

        for (Location child: location.getChildrens()) {
            locationDevices.addAll(searchLocationDevices(child, devices, deep + 1));
        }

        return locationDevices;
    }

}
