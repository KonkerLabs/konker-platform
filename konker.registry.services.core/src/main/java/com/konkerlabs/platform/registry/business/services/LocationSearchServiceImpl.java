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
import com.konkerlabs.platform.registry.business.services.api.LocationSearchService;
import com.konkerlabs.platform.registry.business.services.api.LocationService.Messages;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class LocationSearchServiceImpl implements LocationSearchService {

    private Logger LOGGER = LoggerFactory.getLogger(LocationSearchServiceImpl.class);

    private static final String DEFAULT_ROOT_NAME = "default";

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private DeviceRepository deviceRepository;

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

        if (!Optional.ofNullable(tenant).isPresent())
            return ServiceResponseBuilder.<List<Device>>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode())
                    .build();

        if (!Optional.ofNullable(application).isPresent())
            return ServiceResponseBuilder.<List<Device>>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_NULL.getCode())
                    .build();

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


}