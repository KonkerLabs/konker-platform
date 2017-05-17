package com.konkerlabs.platform.registry.business.services;

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

        Map<String, Object[]> validations = checkLocationIsRemovable(tenant, application, location);
        if (validations != null && !validations.isEmpty()) {
            return ServiceResponseBuilder.<Location>error()
                    .withMessages(validations)
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

    private Map<String, Object[]> checkLocationIsInsertable(Tenant tenant, Application application, Location location){

        Map<String, Object[]> messages = new HashMap<>();

        Optional<Map<String, Object[]>> validations = location.applyValidations();

        if (validations.isPresent()) {
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

        if (validations.isPresent()) {
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

    private Map<String, Object[]> checkLocationIsRemovable(Tenant tenant, Application application, Location location){

        Map<String, Object[]> messages = new HashMap<>();

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

        // TODO: verificar se todos os sublocations tem name (nao pode ser null)

        List<Location> all = locationRepository.findAllByTenantIdAndApplicationName(tenant.getId(), application.getName());
        Location currentSubtree = LocationTreeUtils.searchLocationByName(LocationTreeUtils.buildTree(all), parentNode.getName(), 0);

        Location newSubtree = parentNode;
        newSubtree.setChildrens(sublocations);

        List<Location> removedLocations = LocationTreeUtils.listRemovedLocationns(currentSubtree, newSubtree);
        List<Location> newLocations = LocationTreeUtils.listNewLocationns(currentSubtree, newSubtree);
        List<Location> existingLocations = LocationTreeUtils.listExistingLocationns(currentSubtree, newSubtree);

        // verify multiples defaults
        if (isMultipleDefaults(newSubtree)) {
            return ServiceResponseBuilder.<Location>error()
                    .withMessage(Validations.LOCATION_MULTIPLE_DEFAULTS.getCode())
                    .build();
        }

        // verify removed locations
        for (Location location: removedLocations) {
            Map<String, Object[]> validations = checkLocationIsRemovable(tenant, application, location);
            if (validations != null && !validations.isEmpty()) {
                return ServiceResponseBuilder.<Location>error()
                        .withMessages(validations)
                        .build();
            }
        }

        // verify new locations
        for (Location location: newLocations) {
            Map<String, Object[]> validations = checkLocationIsInsertable(tenant, application, location);
            if (validations != null && !validations.isEmpty()) {
                return ServiceResponseBuilder.<Location>error()
                        .withMessages(validations)
                        .build();
            }
        }

        // verify new existing
        for (Location location: existingLocations) {
            Map<String, Object[]> validations = checkLocationIsUpdatable(tenant, application, location);
            if (validations != null && !validations.isEmpty()) {
                return ServiceResponseBuilder.<Location>error()
                        .withMessages(validations)
                        .build();
            }
        }

        // TODO: verificar se tem nos a serem criados com o mesmo nome

        return ServiceResponseBuilder.<Location>ok()
                .withResult(null)
                .build();
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
