package com.konkerlabs.platform.registry.business.services;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DeviceConfigSetupServiceImpl implements DeviceConfigSetupService {

    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private DeviceConfigSetupRepository deviceConfigSetupRepository;

    @Override
    public ServiceResponse<List<DeviceConfig>> listAll(Tenant tenant, Application application) {

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
    public ServiceResponse<DeviceConfig> saveOrUpdate(Tenant tenant, Application application, DeviceModel deviceModel, Location location, String json) {

        if (!Optional.ofNullable(tenant).isPresent()) {
            return ServiceResponseBuilder.<DeviceConfig>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode()).build();
        }

        if (!Optional.ofNullable(application).isPresent()) {
            return ServiceResponseBuilder.<DeviceConfig>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()).build();
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
        deviceConfigSetupRepository.save(deviceConfigSetupNew);

        return ServiceResponseBuilder.<DeviceConfig>ok()
                .withResult(deviceConfig).build();

    }

    @Override
    public ServiceResponse<DeviceConfigSetup> remove(Tenant tenant, Application application, DeviceModel deviceModel, Location location) {

        if (!Optional.ofNullable(tenant).isPresent()) {
            return ServiceResponseBuilder.<DeviceConfigSetup>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode()).build();
        }

        if (!Optional.ofNullable(application).isPresent()) {
            return ServiceResponseBuilder.<DeviceConfigSetup>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()).build();
        }

        DeviceConfigSetup deviceConfigSetupDB = getCurrentConfigSetup(tenant, application);
        List<DeviceConfig> configs = deviceConfigSetupDB.getConfigs();
        configs.remove(DeviceConfig.builder().deviceModelGuid(deviceModel.getGuid()).locationGuid(location.getGuid()).build());

        DeviceConfigSetup deviceConfigSetupNew = getNewApplication(tenant, application, deviceConfigSetupDB.getVersion() + 1);
        deviceConfigSetupNew.setConfigs(configs);

        deviceConfigSetupRepository.save(deviceConfigSetupNew);

        return ServiceResponseBuilder.<DeviceConfigSetup>ok()
                    .withResult(deviceConfigSetupNew).build();

    }

    @Override
    public ServiceResponse<String> findByModelAndLocation(Tenant tenant, Application application,
            DeviceModel model, Location location) {

        if (!Optional.ofNullable(tenant).isPresent()) {
            return ServiceResponseBuilder.<String>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode()).build();
        }

        if (!Optional.ofNullable(application).isPresent()) {
            return ServiceResponseBuilder.<String>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()).build();
        }

        DeviceConfigSetup deviceConfigSetupDB = getCurrentConfigSetup(tenant, application);
        List<DeviceConfig> configs = deviceConfigSetupDB.getConfigs();

        DeviceConfig config = findDeviceConfig(configs, model, location);

        if (config != null) {
            return ServiceResponseBuilder.<String>ok().withResult(config.getJson()).build();
        } else {
            return ServiceResponseBuilder.<String>ok().withResult(config.getJson()).build();
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

}
