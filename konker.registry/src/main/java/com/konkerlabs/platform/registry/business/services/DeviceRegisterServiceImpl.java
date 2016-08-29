package com.konkerlabs.platform.registry.business.services;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.DeviceRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.NewServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DeviceRegisterServiceImpl implements DeviceRegisterService {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Override
    public NewServiceResponse<Device> register(Tenant tenant, Device device) {

        if (!Optional.ofNullable(tenant).isPresent())
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode(),null)
                    .build();

        if (!Optional.ofNullable(device).isPresent())
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(CommonValidations.RECORD_NULL.getCode(),null)
                    .build();

        if (!tenantRepository.exists(tenant.getId()))
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode(),null)
                    .build();

        device.onRegistration();

        device.setTenant(tenant);

        Optional<Map<String, Object[]>> validations = device.applyValidations();

        if (validations.isPresent())
            return ServiceResponseBuilder.<Device>error()
                    .withMessages(validations.get())
                    .build();

        if (deviceRepository.findByTenantIdAndDeviceId(tenant.getId(), device.getDeviceId()) != null) {
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(Validations.DEVICE_ID_ALREADY_REGISTERED.getCode(),null)
                    .build();
        }

        device.setApiKey(device.getDeviceId());

        Device saved = deviceRepository.save(device);

        return ServiceResponseBuilder.<Device>ok().withResult(saved).build();
    }

    @Override
    public NewServiceResponse<List<Device>> findAll(Tenant tenant) {
        List<Device> all = deviceRepository.findAllByTenant(tenant.getId());
        return ServiceResponseBuilder.<List<Device>>ok().withResult(all).build();
    }


    @Override
    public Device findByApiKey(String apiKey) {
        return deviceRepository.findByApiKey(apiKey);
    }

    @Override
    public Device findByTenantDomainNameAndDeviceId(String tenantDomainName, String deviceId) {
        return deviceRepository.findByTenantIdAndDeviceId(
            tenantRepository.findByDomainName(tenantDomainName).getId(),
            deviceId
        );
    }


    @Override
    public NewServiceResponse<Device> switchEnabledDisabled(Tenant tenant, String id) {
        if (!Optional.ofNullable(id).isPresent())
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(Validations.DEVICE_ID_NULL.getCode(),null)
                    .build();

        Device found = getByDeviceId(tenant, id).getResult();

        if (!Optional.ofNullable(found).isPresent())
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(Validations.DEVICE_ID_DOES_NOT_EXIST.getCode(),null)
                    .build();

        found.setActive(!found.isActive());

        Device updated = deviceRepository.save(found);

        return ServiceResponseBuilder.<Device>ok()
                .withResult(updated)
                .build();
    }

    @Override
    public NewServiceResponse<Device> update(Tenant tenant, String id, Device updatingDevice) {
        if (!Optional.ofNullable(tenant).isPresent())
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode(),null)
                    .build();

        if (!Optional.ofNullable(id).isPresent())
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(Validations.DEVICE_ID_NULL.getCode(),null)
                    .build();

        if (!Optional.ofNullable(updatingDevice).isPresent())
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(CommonValidations.RECORD_NULL.getCode(),null)
                    .build();

        Device deviceFromDB = getByDeviceId(tenant, id).getResult();
        if (deviceFromDB == null) {
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(Validations.DEVICE_ID_DOES_NOT_EXIST.getCode(),null)
                    .build();
        }

        // modify "modifiable" fields
        deviceFromDB.setDescription(updatingDevice.getDescription());
        deviceFromDB.setName(updatingDevice.getName());
        deviceFromDB.setActive(updatingDevice.isActive());

        Optional<Map<String, Object[]>> validations = deviceFromDB.applyValidations();

        if (validations.isPresent()) {
            return ServiceResponseBuilder.<Device>error()
                    .withMessages(validations.get())
                    .build();
        }

        Device saved = deviceRepository.save(deviceFromDB);

        return ServiceResponseBuilder.<Device>ok()
                .withResult(saved)
                .build();
    }

    @Override
    public NewServiceResponse<Device> getByDeviceId(Tenant tenant, String id) {
        if (!Optional.ofNullable(tenant).isPresent())
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode(),null)
                    .build();

        if (!Optional.ofNullable(id).isPresent())
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(Validations.DEVICE_ID_NULL.getCode(),null)
                    .build();

        Tenant t = tenantRepository.findByName(tenant.getName());

        if (!Optional.ofNullable(tenant).isPresent())
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode(),null)
                    .build();

        Device device = deviceRepository.findByTenantAndId(t.getId(), id);
        if (!Optional.ofNullable(device).isPresent()) {
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(Validations.DEVICE_ID_DOES_NOT_EXIST.getCode(),null)
                    .build();
        }

        return ServiceResponseBuilder.<Device>ok()
                .withResult(device)
                .build();
    }

}
