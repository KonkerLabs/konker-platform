package com.konkerlabs.platform.registry.business.services;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.konkerlabs.platform.registry.business.model.Tenant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.repositories.DeviceRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DeviceRegisterServiceImpl implements DeviceRegisterService {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Override
    public ServiceResponse<Device> register(Tenant tenant, Device device) throws BusinessException {
        Optional.ofNullable(tenant).orElseThrow(() -> new BusinessException("Tenant cannot be null"));
        Optional.ofNullable(device).orElseThrow(() -> new BusinessException("Record cannot be null"));

        if (!tenantRepository.exists(tenant.getId()))
            throw new BusinessException("Tenant does not exist");

        device.onRegistration();

        device.setTenant(tenant);

        List<String> validations = device.applyValidations();

        if (validations != null)
            return ServiceResponse.<Device>builder()
                    .responseMessages(validations)
                    .status(ServiceResponse.Status.ERROR).<Device>build();

        if (deviceRepository.findByTenantIdAndDeviceId(tenant.getId(), device.getDeviceId()) != null) {
            return ServiceResponse.<Device>builder()
                    .responseMessages(Arrays.asList(new String[] { "Device ID already registered" }))
                    .status(ServiceResponse.Status.ERROR).<Device>build();
        }

        Device saved = deviceRepository.save(device);

        return ServiceResponse.<Device>builder().status(ServiceResponse.Status.OK).result(saved).<Device>build();
    }

    @Override
    public List<Device> getAll(Tenant tenant) {
        return deviceRepository.findAllByTenant(tenant.getId());
    }

    @Override
    public Device findById(String id) {
        return deviceRepository.findOne(id);
    }

    @Override
    public Device findByApiKey(String apiKey) {
        return deviceRepository.findByApiKey(apiKey);
    }

    @Override
    public ServiceResponse<Device> update(String id, Device updatingDevice) throws BusinessException {
        Optional.ofNullable(id)
            .orElseThrow(() -> new BusinessException("Cannot update device with null ID"));

        Optional.ofNullable(updatingDevice)
            .orElseThrow(() -> new BusinessException("Cannot update null device"));

        Device deviceFromDB = findById(id);
        if (deviceFromDB == null) {
            return ServiceResponse.<Device>builder()
                    .responseMessages(Arrays.asList(new String[] { "Device ID does not exists" }))
                    .status(ServiceResponse.Status.ERROR).<Device>build();
        }

        // modify "modifiable" fields
        deviceFromDB.setDescription(updatingDevice.getDescription());
        deviceFromDB.setName(updatingDevice.getName());

        List<String> validations = deviceFromDB.applyValidations();

        if (validations != null) {
            return ServiceResponse.<Device>builder()
                    .responseMessages(validations).status(ServiceResponse.Status.ERROR).<Device>build();
        }

        Device saved = deviceRepository.save(deviceFromDB);

        return ServiceResponse.<Device>builder().status(ServiceResponse.Status.OK).result(saved).<Device>build();
    }
}
