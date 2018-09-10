package com.konkerlabs.platform.registry.business.services;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.DeviceFirmware;
import com.konkerlabs.platform.registry.business.model.DeviceModel;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.ApplicationRepository;
import com.konkerlabs.platform.registry.business.repositories.DeviceFirmwareRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.DeviceFirmwareService;
import com.konkerlabs.platform.registry.business.services.api.DeviceModelService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DeviceFirmwareServiceImpl implements DeviceFirmwareService {

    private Logger LOGGER = LoggerFactory.getLogger(DeviceFirmwareServiceImpl.class);

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private DeviceFirmwareRepository deviceFirmwareRepository;

    @Autowired
    private DeviceModelService deviceModelService;

    @Override
    public ServiceResponse<DeviceFirmware> save(Tenant tenant, Application application, DeviceFirmware deviceFirmware) {

        ServiceResponse<DeviceFirmware> validationResponse = validate(tenant, application);
        if (!validationResponse.isOk()) {
            return validationResponse;
        }

        if (deviceFirmware.getDeviceModel() == null) {
            ServiceResponse<DeviceModel> response = deviceModelService.findDefault(tenant, application);
            if (response.isOk()) {
                deviceFirmware.setDeviceModel(response.getResult());
            } else {
                return ServiceResponseBuilder.<DeviceFirmware>error()
                        .withMessage(DeviceModelService.Validations.DEVICE_MODEL_DOES_NOT_EXIST.getCode())
                        .build();
            }
        } else {
            if (!deviceFirmware.getDeviceModel().getApplication().getName().equals(application.getName())) {
                return ServiceResponseBuilder.<DeviceFirmware>error()
                        .withMessage(DeviceModelService.Validations.DEVICE_MODEL_DOES_NOT_EXIST.getCode())
                        .build();
            }
        }

        deviceFirmware.setTenant(tenant);
        deviceFirmware.setApplication(application);
        deviceFirmware.setUploadDate(Instant.now());

        Optional<Map<String, Object[]>> validations = deviceFirmware.applyValidations();

        if (validations.isPresent()) {
            return ServiceResponseBuilder.<DeviceFirmware>error()
                    .withMessages(validations.get())
                    .build();
        }

        DeviceFirmware existingDeviceFirmware = deviceFirmwareRepository.findUnique(tenant.getId(), application.getName(), deviceFirmware.getDeviceModel().getId(), deviceFirmware.getVersion());
        if (existingDeviceFirmware != null) {
            return ServiceResponseBuilder.<DeviceFirmware>error()
                    .withMessage(Validations.FIRMWARE_ALREADY_REGISTERED.getCode())
                    .withResult(existingDeviceFirmware)
                    .build();
        }

        DeviceFirmware saved = deviceFirmwareRepository.save(deviceFirmware);

        return ServiceResponseBuilder.<DeviceFirmware>ok().withResult(saved).build();

    }

    @Override
    public ServiceResponse<DeviceFirmware> findByVersion(Tenant tenant, Application application, DeviceModel deviceModel, String version) {

        ServiceResponse<DeviceFirmware> validationResponse = validate(tenant, application);
        if (!validationResponse.isOk()) {
            return validationResponse;
        }

        List<DeviceFirmware> modelFirmwares = deviceFirmwareRepository.listByDeviceModel(tenant.getId(), application.getName(), deviceModel.getId());
        for (DeviceFirmware deviceFirmware: modelFirmwares) {
            if (deviceFirmware.getVersion().equals(version)) {
                return ServiceResponseBuilder.<DeviceFirmware>ok()
                        .withResult(deviceFirmware)
                        .build();
            }
        }

        return ServiceResponseBuilder.<DeviceFirmware>error()
                .withMessage(Validations.FIRMWARE_NOT_FOUND.getCode())
                .build();

    }

    @Override
    public ServiceResponse<List<DeviceFirmware>> listByDeviceModel(Tenant tenant, Application application,
            DeviceModel deviceModel) {

        ServiceResponse<List<DeviceFirmware>> validationResponse = validate(tenant, application);
        if (!validationResponse.isOk()) {
            return validationResponse;
        }

        List<DeviceFirmware> modelFirmwares = deviceFirmwareRepository.listByDeviceModel(tenant.getId(), application.getName(), deviceModel.getId());

        return ServiceResponseBuilder.<List<DeviceFirmware>>ok()
                .withResult(modelFirmwares)
                .build();

    }

    private <T> ServiceResponse<T> validate(Tenant tenant, Application application) {

        if (!Optional.ofNullable(tenant).isPresent()) {
            Device noDevice = Device.builder().guid("NULL").tenant(
                    Tenant.builder().domainName("unknown_domain").build()).build();
            LOGGER.debug(CommonValidations.TENANT_NULL.getCode(),
                    noDevice.toURI(),
                    noDevice.getTenant().getLogLevel());
            return ServiceResponseBuilder.<T>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode())
                    .build();
        }

        if (!Optional.ofNullable(application).isPresent()) {
            Device noDevice = Device.builder()
                    .guid("NULL")
                    .tenant(tenant)
                    .application(Application.builder().name("unknowapp").tenant(tenant).build())
                    .build();
            LOGGER.debug(ApplicationService.Validations.APPLICATION_NULL.getCode(),
                    noDevice.toURI(),
                    noDevice.getTenant().getLogLevel());
            return ServiceResponseBuilder.<T>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_NULL.getCode())
                    .build();
        }

        if (!tenantRepository.exists(tenant.getId())) {
            return ServiceResponseBuilder.<T>error()
                    .withMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode())
                    .build();
        }

        if (!applicationRepository.exists(application.getName())) {
            return ServiceResponseBuilder.<T>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode())
                    .build();
        }

        return ServiceResponseBuilder.<T>ok().build();

    }


}
