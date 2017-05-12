package com.konkerlabs.platform.registry.business.services;

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

        if(!devices.isEmpty()) {
            return ServiceResponseBuilder.<Location>error()
                    .withMessage(Validations.LOCATION_HAVE_DEVICES.getCode())
                    .build();
        }

        List<Location> childrens =
                locationRepository.findChildrensByParentId(tenant.getId(), application.getName(), location.getId());

        if(!childrens.isEmpty()) {
            return ServiceResponseBuilder.<Location>error()
                    .withMessage(Validations.LOCATION_HAVE_CHILDRENS.getCode())
                    .build();
        }

        ServiceResponse<List<DeviceConfig>> deviceConfigResponse = deviceConfigSetupService.findAllByLocation(tenant, application, location);

        if (!deviceConfigResponse.isOk()) {
            return ServiceResponseBuilder.<Location>error()
                    .withMessages(deviceConfigResponse.getResponseMessages())
                    .build();
        }

        if(!deviceConfigResponse.getResult().isEmpty()) {
            return ServiceResponseBuilder.<Location>error()
                    .withMessage(Validations.LOCATION_HAVE_DEVICE_CONFIGS.getCode())
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

}
