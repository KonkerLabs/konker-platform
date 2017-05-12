package com.konkerlabs.platform.registry.business.services;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.DeviceConfig;
import com.konkerlabs.platform.registry.business.model.DeviceConfigSetup;
import com.konkerlabs.platform.registry.business.model.DeviceModel;
import com.konkerlabs.platform.registry.business.model.Location;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.DeviceConfigSetupRepository;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.DeviceConfigSetupService;
import com.konkerlabs.platform.registry.business.services.api.DeviceModelService;
import com.konkerlabs.platform.registry.business.services.api.LocationService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.mongodb.util.JSON;
import com.mongodb.util.JSONParseException;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DeviceConfigSetupServiceImpl implements DeviceConfigSetupService {

    @Autowired
    private DeviceConfigSetupRepository deviceConfigSetupRepository;

    @Autowired
    private LocationService locationService;

    @Override
    public ServiceResponse<List<DeviceConfig>> findAll(Tenant tenant, Application application) {

        if (!Optional.ofNullable(tenant).isPresent()) {
            return ServiceResponseBuilder.<List<DeviceConfig>>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode()).build();
        }

        if (!Optional.ofNullable(application).isPresent()) {
            return ServiceResponseBuilder.<List<DeviceConfig>>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()).build();
        }

        return ServiceResponseBuilder.<List<DeviceConfig>>ok()
                .withResult(getDeviveConfis(tenant, application))
                .build();

    }

    @Override
    public ServiceResponse<DeviceConfig> save(Tenant tenant, Application application, DeviceModel deviceModel, Location location, String json) {

        ServiceResponse<DeviceConfig> validationsResponse = validate(tenant, application, deviceModel, location);
        if (validationsResponse != null && !validationsResponse.isOk()) {
            return validationsResponse;
        }

        if (isInvalidJson(json)) {
            return ServiceResponseBuilder.<DeviceConfig>error()
                    .withMessage(Validations.DEVICE_INVALID_JSON.getCode()).build();
        }

        DeviceConfigSetup deviceConfigSetupDB = getCurrentConfigSetup(tenant, application);
        List<DeviceConfig> configs = deviceConfigSetupDB.getConfigs();

        DeviceConfig deviceConfig = findDeviceConfig(configs, deviceModel, location);

        if (deviceConfig == null) {
            deviceConfig = DeviceConfig.builder()
                                .deviceModel(deviceModel.getName())
                                .deviceModelGuid(deviceModel.getGuid())
                                .locationName(location.getName())
                                .locationGuid(location.getGuid())
                                .json(json)
                                .build();

            configs.add(deviceConfig);
        } else {
            deviceConfig.setJson(json);
        }

        DeviceConfigSetup deviceConfigSetupNew = getNewApplication(tenant, application, deviceConfigSetupDB.getVersion() + 1);
        deviceConfigSetupNew.setConfigs(configs);
        deviceConfigSetupRepository.save(deviceConfigSetupNew);

        return ServiceResponseBuilder.<DeviceConfig>ok().withResult(deviceConfig).build();

    }

    private boolean isInvalidJson(String json) {

        if (StringUtils.isBlank(json)) {
            return true;
        }

        try {
            JSON.parse(json);
        } catch (JSONParseException e) {
            return true;
        }

        return false;
    }

    @Override
    public ServiceResponse<DeviceConfig> update(Tenant tenant, Application application, DeviceModel deviceModel, Location location, String json) {

        ServiceResponse<DeviceConfig> validationsResponse = validate(tenant, application, deviceModel, location);
        if (validationsResponse != null && !validationsResponse.isOk()) {
            return validationsResponse;
        }

        if (isInvalidJson(json)) {
            return ServiceResponseBuilder.<DeviceConfig>error()
                    .withMessage(Validations.DEVICE_INVALID_JSON.getCode()).build();
        }

        DeviceConfigSetup deviceConfigSetupDB = getCurrentConfigSetup(tenant, application);
        List<DeviceConfig> configs = deviceConfigSetupDB.getConfigs();

        DeviceConfig deviceConfig = findDeviceConfig(configs, deviceModel, location);

        if (deviceConfig == null) {
            return ServiceResponseBuilder.<DeviceConfig>error()
                    .withMessage(Validations.DEVICE_CONFIG_NOT_FOUND.getCode())
                    .withResult(deviceConfig).build();
        } else {
            deviceConfig.setJson(json);
        }

        DeviceConfigSetup deviceConfigSetupNew = getNewApplication(tenant, application, deviceConfigSetupDB.getVersion() + 1);
        deviceConfigSetupNew.setConfigs(configs);
        deviceConfigSetupRepository.save(deviceConfigSetupNew);

        return ServiceResponseBuilder.<DeviceConfig>ok().withResult(deviceConfig).build();

    }

    @Override
    public ServiceResponse<DeviceConfigSetup> remove(Tenant tenant, Application application, DeviceModel deviceModel, Location location) {

        ServiceResponse<DeviceConfigSetup> validationsResponse = validate(tenant, application, deviceModel, location);
        if (validationsResponse != null && !validationsResponse.isOk()) {
            return validationsResponse;
        }

        DeviceConfigSetup deviceConfigSetupDB = getCurrentConfigSetup(tenant, application);
        List<DeviceConfig> configs = deviceConfigSetupDB.getConfigs();
        configs.remove(DeviceConfig.builder().deviceModelGuid(deviceModel.getGuid()).locationGuid(location.getGuid()).build());

        DeviceConfigSetup deviceConfigSetupNew = getNewApplication(tenant, application, deviceConfigSetupDB.getVersion() + 1);
        deviceConfigSetupNew.setConfigs(configs);

        deviceConfigSetupRepository.save(deviceConfigSetupNew);

        return ServiceResponseBuilder.<DeviceConfigSetup>ok()
                                     .withResult(deviceConfigSetupNew)
                                     .withMessage(Messages.DEVICE_CONFIG_REMOVED_SUCCESSFULLY.getCode())
                                     .build();

    }

    @Override
    public ServiceResponse<String> findByModelAndLocation(Tenant tenant, Application application,
            DeviceModel deviceModel, Location location) {

        ServiceResponse<String> validationsResponse = validate(tenant, application, deviceModel, location);
        if (validationsResponse != null && !validationsResponse.isOk()) {
            return validationsResponse;
        }

        DeviceConfigSetup deviceConfigSetupDB = getCurrentConfigSetup(tenant, application);
        List<DeviceConfig> configs = deviceConfigSetupDB.getConfigs();

        ServiceResponse<Location> locationResponse = locationService.findByName(tenant, application, location.getName(), true);
        if (!locationResponse.isOk()) {
            return ServiceResponseBuilder.<String>error()
                    .withMessages(locationResponse.getResponseMessages())
                    .build();
        }

        Location locationNode = locationResponse.getResult();
        DeviceConfig config = null;

        // search for config in location and above locations
        while (locationNode != null) {
            if (config == null) {
                config = findDeviceConfig(configs, deviceModel, locationNode);
            }
            locationNode = locationNode.getParent();
        }

        if (config != null) {
            return ServiceResponseBuilder.<String>ok().withResult(config.getJson()).build();
        } else {
            return ServiceResponseBuilder.<String>error()
                    .withMessage(Validations.DEVICE_CONFIG_NOT_FOUND.getCode())
                    .build();
        }

    }

    private DeviceConfig findDeviceConfig(List<DeviceConfig> configs, DeviceModel model, Location location) {

        for (DeviceConfig deviceConfig : configs) {
            if (deviceConfig.getDeviceModelGuid().equals(model.getGuid()) &&
                deviceConfig.getLocationGuid().equals(location.getGuid())) {
                    return deviceConfig;
                }
        }

        return null;
    }

    private List<DeviceConfig> getDeviveConfis(Tenant tenant, Application application) {

        DeviceConfigSetup deviceConfigSetup = getCurrentConfigSetup(tenant, application);
        return deviceConfigSetup.getConfigs();

    }

    private DeviceConfigSetup getCurrentConfigSetup(Tenant tenant, Application application) {

        List<DeviceConfigSetup> configSetups = deviceConfigSetupRepository.findAllByTenantIdAndApplicationName(tenant.getId(), application.getName());

        if (configSetups.isEmpty()) {
            DeviceConfigSetup configSetup = getNewApplication(tenant, application, 0);
            deviceConfigSetupRepository.save(configSetup);
            return configSetup;
        }

        Collections.sort(configSetups);

        return configSetups.get(0);

    }

    private DeviceConfigSetup getNewApplication(Tenant tenant, Application application, int version) {
        return DeviceConfigSetup.builder()
                                .tenant(tenant)
                                .application(application)
                                .configs(new ArrayList<DeviceConfig>())
                                .version(version)
                                .date(Instant.now())
                                .build();
    }

    private <T> ServiceResponse<T> validate(Tenant tenant, Application application, DeviceModel deviceModel, Location location) {

        if (!Optional.ofNullable(tenant).isPresent()) {
            return ServiceResponseBuilder.<T>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode()).build();
        }

        if (!Optional.ofNullable(application).isPresent()) {
            return ServiceResponseBuilder.<T>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()).build();
        }

        if (!Optional.ofNullable(deviceModel).isPresent() || !Optional.ofNullable(deviceModel.getGuid()).isPresent()) {
            return ServiceResponseBuilder.<T>error()
                    .withMessage(DeviceModelService.Validations.DEVICE_MODEL_NULL.getCode()).build();
        }

        if (!Optional.ofNullable(location).isPresent() || !Optional.ofNullable(location.getGuid()).isPresent()) {
            return ServiceResponseBuilder.<T>error()
                    .withMessage(LocationService.Validations.LOCATION_GUID_NULL.getCode()).build();
        }

        return null;
    }

    @Override
    public ServiceResponse<List<DeviceConfig>> findAllByDeviceModel(Tenant tenant, Application application, DeviceModel deviceModel) {

        if (!Optional.ofNullable(tenant).isPresent()) {
            return ServiceResponseBuilder.<List<DeviceConfig>>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode()).build();
        }

        if (!Optional.ofNullable(application).isPresent()) {
            return ServiceResponseBuilder.<List<DeviceConfig>>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()).build();
        }

        if (!Optional.ofNullable(deviceModel).isPresent() || !Optional.ofNullable(deviceModel.getGuid()).isPresent()) {
            return ServiceResponseBuilder.<List<DeviceConfig>>error()
                    .withMessage(DeviceModelService.Validations.DEVICE_MODEL_NULL.getCode()).build();
        }

        return ServiceResponseBuilder.<List<DeviceConfig>>ok()
                .withResult(getDeviveConfis(tenant, application)
                                .stream()
                                .filter(p -> p.getDeviceModelGuid().equals(deviceModel.getGuid()))
                                .collect(Collectors.toList()))
                .build();

    }

    @Override
    public ServiceResponse<List<DeviceConfig>> findAllByLocation(Tenant tenant, Application application,
            Location location) {

        if (!Optional.ofNullable(tenant).isPresent()) {
            return ServiceResponseBuilder.<List<DeviceConfig>>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode()).build();
        }

        if (!Optional.ofNullable(application).isPresent()) {
            return ServiceResponseBuilder.<List<DeviceConfig>>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()).build();
        }

        if (!Optional.ofNullable(location).isPresent() || !Optional.ofNullable(location.getGuid()).isPresent()) {
            return ServiceResponseBuilder.<List<DeviceConfig>>error()
                    .withMessage(LocationService.Validations.LOCATION_GUID_NULL.getCode()).build();
        }

        return ServiceResponseBuilder.<List<DeviceConfig>>ok()
                .withResult(getDeviveConfis(tenant, application)
                                .stream()
                                .filter(p -> p.getLocationGuid().equals(location.getGuid()))
                                .collect(Collectors.toList()))
                .build();

    };

}
