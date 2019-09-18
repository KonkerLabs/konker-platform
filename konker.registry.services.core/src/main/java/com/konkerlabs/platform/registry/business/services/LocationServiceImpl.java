package com.konkerlabs.platform.registry.business.services;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
import com.konkerlabs.platform.registry.business.model.DeviceConfig;
import com.konkerlabs.platform.registry.business.model.Location;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.DeviceRepository;
import com.konkerlabs.platform.registry.business.repositories.LocationRepository;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.DeviceConfigSetupService;
import com.konkerlabs.platform.registry.business.services.api.LocationService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class LocationServiceImpl implements LocationService {

    private Logger LOGGER = LoggerFactory.getLogger(LocationServiceImpl.class);

    @Autowired
    private DeviceConfigSetupService deviceConfigSetupService;

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

        Map<String, Object[]> validations = checkLocationIsInsertable(tenant, application, location);
        if (validations != null && !validations.isEmpty()) {
            return ServiceResponseBuilder.<Location>error()
                    .withMessages(validations)
                    .build();
        }

        if (location.isDefaultLocation()) {
            setFalseDefaultToAllLocations(tenant, application);
        }
        
        if (!Optional.ofNullable(location.getParent()).isPresent()) {
        	location.setDefaultLocation(true);
        }

        Location saved = locationRepository.save(location);

        LOGGER.info("Location created. Id: {}", location.getId(), tenant.toURI(), tenant.getLogLevel());

        return ServiceResponseBuilder.<Location>ok()
                .withMessage(LocationService.Messages.LOCATION_REGISTERED_SUCCESSFULLY.getCode())
                .withResult(saved)
                .build();
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

        if (updatingLocation.getId()
                .equals(updatingLocation.getParent().getId())) {
            return ServiceResponseBuilder.<Location>error()
                    .withMessage(Validations.LOCATION_PARENT_INVALID.getCode())
                    .build();
        }

        Location locationFromDB = locationRepository.findByTenantAndApplicationAndGuid(tenant.getId(), application.getName(), guid);
        if (locationFromDB == null) {
            return ServiceResponseBuilder.<Location>error()
                    .withMessage(Validations.LOCATION_GUID_DOES_NOT_EXIST.getCode())
                    .build();
        }

        updatingLocation.setTenant(tenant);
        updatingLocation.setApplication(application);

        Map<String, Object[]> validations = checkLocationIsUpdatable(tenant, application, updatingLocation);
        if (validations != null && !validations.isEmpty()) {
            return ServiceResponseBuilder.<Location>error()
                    .withMessages(validations)
                    .build();
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
        
        if (location.isDefaultLocation()) {
        	return ServiceResponseBuilder.<Location>error()
                    .withMessage(Validations.LOCATION_NOT_REMOVED_IS_DEFAULT.getCode())
                    .build();
        }

        Map<String, Object[]> validations = checkLocationIsRemovable(tenant, application, location);
        if (validations != null && !validations.isEmpty()) {
            return ServiceResponseBuilder.<Location>error()
                    .withMessages(validations)
                    .build();
        }

        // build node tree
        List<Location> allNodes = locationRepository.findAllByTenantIdAndApplicationName(tenant.getId(), application.getName());
        Location currentTree = LocationTreeUtils.searchLocationByName(LocationTreeUtils.buildTree(allNodes), location.getName(), 0);

        // list deepest order
        List<Location> allTreeNodes = LocationTreeUtils.getNodesListBreadthFirstOrder(currentTree);
        Collections.reverse(allTreeNodes);

        // remove children first
        for (Location node: allTreeNodes) {
            locationRepository.delete(node);
        }

        LOGGER.info("Location removed. Id: {}", location.getId(), tenant.toURI(), tenant.getLogLevel());

        return ServiceResponseBuilder.<Location>ok()
                .withMessage(LocationService.Messages.LOCATION_REMOVED_SUCCESSFULLY.getCode())
                .withResult(location)
                .build();
    }

    private Map<String, Object[]> checkLocationIsInsertable(Tenant tenant, Application application, Location location){

        Map<String, Object[]> messages = new HashMap<>();

        Optional<Map<String, Object[]>> validations = location.applyValidations();

        if (validations.isPresent() && !validations.get().isEmpty()) {
            messages.putAll(validations.get());
            return messages;
        }

        if (locationRepository.findByTenantAndApplicationAndName(tenant.getId(), application.getName(), location.getName()) != null) {
            messages.put(Validations.LOCATION_NAME_ALREADY_REGISTERED.getCode(), new Object[] {location.getName()});
        }

        if (location.getParent() == null) {
            if (locationRepository.findRootLocationByTenantAndApplication(tenant.getId(), application.getName()) != null) {
                messages.put(Validations.LOCATION_PARENT_NULL.getCode(), new Object[] {location.getName()});
            }
        }

        return messages;

    }

    private Map<String, Object[]> checkLocationIsUpdatable(Tenant tenant, Application application, Location location){

        Map<String, Object[]> messages = new HashMap<>();

        Optional<Map<String, Object[]>> validations = location.applyValidations();

        if (validations.isPresent() && !validations.get().isEmpty()) {
            messages.putAll(validations.get());
            return messages;
        }

        String guid = location.getGuid();

        final Location sameNameLocation = locationRepository.findByTenantAndApplicationAndName(tenant.getId(), application.getName(), location.getName());
        if (sameNameLocation != null && !sameNameLocation.getGuid().equals(guid)) {
            messages.put(Validations.LOCATION_NAME_ALREADY_REGISTERED.getCode(), new Object[] {location.getName()});
        }

        if (location.getParent() == null) {
            final Location rootLocation = locationRepository.findRootLocationByTenantAndApplication(tenant.getId(), application.getName());
            if (rootLocation != null && !rootLocation.getGuid().equals(guid)) {
                messages.put(Validations.LOCATION_PARENT_NULL.getCode(), new Object[] {location.getName()});
            }
        }

        return messages;

    }

    private Map<String, Object[]> checkLocationIsRemovable(Tenant tenant, Application application, Location location) {

        Map<String, Object[]> messages = new HashMap<>();

        // root location?
        if (location.getParent() == null) {
            messages.put(Validations.LOCATION_IS_ROOT.getCode(), new Object[] {location.getName()});
        }

        // dependencies: devices
        List<Device> devices =
                deviceRepository.findAllByTenantIdAndApplicationNameAndLocationName(tenant.getId(), application.getName(), location.getId());

        if(!devices.isEmpty()) {
            messages.put(Validations.LOCATION_HAVE_DEVICES.getCode(), new Object[] {location.getName()});
        }

        // dependencies: configs
        ServiceResponse<List<DeviceConfig>> deviceConfigResponse = deviceConfigSetupService.findAllByLocation(tenant, application, location);

        if (!deviceConfigResponse.isOk()) {
            messages.putAll(deviceConfigResponse.getResponseMessages());
            return messages;
        }

        if(!deviceConfigResponse.getResult().isEmpty()) {
            messages.put(Validations.LOCATION_HAVE_DEVICE_CONFIGS.getCode(), new Object[] {location.getName()});
        }

        // dependencies: childers/sublocations
        List<Location> childrens =
                locationRepository.findChildrensByParentId(tenant.getId(), application.getName(), location.getId());

        for (Location child: childrens) {
            Map<String, Object[]> childValidations = checkLocationIsRemovable(tenant, application, child);
            if (childValidations != null && !childValidations.isEmpty()) {
                messages.putAll(childValidations);
            }
        }

        return messages;
    }

    @Override
    public ServiceResponse<Location> updateSubtree(Tenant tenant, Application application, String guid,
            List<Location> sublocations) {

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

        Location parentNode = locationRepository.findByTenantAndApplicationAndGuid(tenant.getId(), application.getName(), guid);
        if (parentNode == null) {
            return ServiceResponseBuilder.<Location>error()
                    .withMessage(Validations.LOCATION_GUID_DOES_NOT_EXIST.getCode())
                    .build();
        }

        // set parent
        setLocationParent(parentNode, sublocations);

        // all nodes must have a valid name
        for (Location location : sublocations) {
            location.setTenant(tenant);
            location.setApplication(application);
            Optional<Map<String, Object[]>> modelValidations = location.applyValidations();
            if (modelValidations.isPresent() && !modelValidations.get().isEmpty()) {
                return ServiceResponseBuilder.<Location>error()
                                             .withMessages(modelValidations.get())
                                             .build();
            }
        }

        List<Location> all = locationRepository.findAllByTenantIdAndApplicationName(tenant.getId(), application.getName());
        Location currentSubtree = LocationTreeUtils.searchLocationByName(LocationTreeUtils.buildTree(all), parentNode.getName(), 0);

        Location newSubtree = parentNode;
        newSubtree.setChildren(sublocations);

        List<Location> removedLocations = LocationTreeUtils.listRemovedLocations(currentSubtree, newSubtree);
        List<Location> newLocations = LocationTreeUtils.listNewLocations(currentSubtree, newSubtree);
        List<Location> existingLocations = LocationTreeUtils.listExistingLocations(currentSubtree, newSubtree);

        // verify multiples defaults
        if (isMultipleDefaults(newSubtree)) {
            return ServiceResponseBuilder.<Location>error()
                    .withMessage(Validations.LOCATION_MULTIPLE_DEFAULTS.getCode())
                    .build();
        }

        // verify same name in use
        Map<String, Object[]> validations = verifyNameInUse(newSubtree);
        if (validations != null && !validations.isEmpty()) {
            return ServiceResponseBuilder.<Location>error()
                    .withMessages(validations)
                    .build();
        }

        // verify removed locations
        for (Location location: removedLocations) {
            validations = checkLocationIsRemovable(tenant, application, location);
            if (validations != null && !validations.isEmpty()) {
                return ServiceResponseBuilder.<Location>error()
                        .withMessages(validations)
                        .build();
            }
        }

        // verify new locations
        for (Location location: newLocations) {
            validations = checkLocationIsInsertable(tenant, application, location);
            if (validations != null && !validations.isEmpty()) {
                return ServiceResponseBuilder.<Location>error()
                        .withMessages(validations)
                        .build();
            }
        }

        // verify new existing
        for (Location location: existingLocations) {
            validations = checkLocationIsUpdatable(tenant, application, location);
            if (validations != null && !validations.isEmpty()) {
                return ServiceResponseBuilder.<Location>error()
                        .withMessages(validations)
                        .build();
            }
        }

        // update nodes
        for (Location location: existingLocations) {
            this.update(tenant, application, location.getGuid(), location);
        }

        // create nodes
        for (Location location: newLocations) {
            this.save(tenant, application, location);
        }

        // remove nodes
        for (Location location: removedLocations) {
            this.remove(tenant, application, location.getGuid());
        }

        return ServiceResponseBuilder.<Location>ok().build();
    }

    private void setLocationParent(Location parent, List<Location> childrens) {

        if (childrens == null) {
            return;
        }

        for (Location child : childrens) {
            child.setParent(parent);
            setLocationParent(child, child.getChildren());
        }

    }

    private Map<String, Object[]> verifyNameInUse(Location root) {

        Map<String, Object[]> messages = new HashMap<>();

        List<Location> locations = LocationTreeUtils.getNodesList(root);

        Set<String> namesInUse = new HashSet<>();

        for (Location location : locations) {
            String name = location.getName();
            if (namesInUse.contains(name)) {
                messages.put(Validations.LOCATION_NAME_ALREADY_REGISTERED.getCode(), new Object[] {location.getName()});
                return messages;
            } else {
                namesInUse.add(name);
            }
        }

        return messages;

    }

    private boolean isMultipleDefaults(Location root) {

        int defaultCount = 0;

        List<Location> locations = LocationTreeUtils.getNodesList(root);

        for (Location location : locations) {
            if (location.isDefaultLocation()) {
                defaultCount++;
            }
        }

        return defaultCount > 1;

    }

}
