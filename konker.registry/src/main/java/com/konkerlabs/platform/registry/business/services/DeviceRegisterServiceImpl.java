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
import com.konkerlabs.platform.registry.business.model.EventRule;
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
    public ServiceResponse<Device> register(Tenant tenant, Device device) {

        if (!Optional.ofNullable(tenant).isPresent())
            return ServiceResponse.<Device>builder()
                .status(ServiceResponse.Status.ERROR)
                .responseMessage("Tenant cannot be null")
                .<Device>build();

        if (!Optional.ofNullable(device).isPresent())
            return ServiceResponse.<Device>builder()
                    .status(ServiceResponse.Status.ERROR)
                    .responseMessage("Record cannot be null")
                    .<Device>build();

        if (!tenantRepository.exists(tenant.getId()))
            return ServiceResponse.<Device>builder()
                    .status(ServiceResponse.Status.ERROR)
                    .responseMessage("Tenant does not exist")
                    .<Device>build();

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

        device.setApiKey(device.getDeviceId());

        Device saved = deviceRepository.save(device);

        return ServiceResponse.<Device>builder().status(ServiceResponse.Status.OK).result(saved).<Device>build();
    }

    @Override
    public List<Device> getAll(Tenant tenant) {
        return deviceRepository.findAllByTenant(tenant.getId());
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
    public ServiceResponse<Device> switchActivation(Tenant tenant, String id) {
        if (!Optional.ofNullable(id).isPresent())
            return ServiceResponse.<Device>builder()
                    .status(ServiceResponse.Status.ERROR)
                    .responseMessage("Device ID cannot be null").<Device>build();

        Device found = getById(tenant, id).getResult();

        if (!Optional.ofNullable(found).isPresent())
            return ServiceResponse.<Device>builder()
                    .status(ServiceResponse.Status.ERROR)
                    .responseMessage("Device ID does not exist").<Device>build();

        found.setActive(!found.isActive());

        Device updated = deviceRepository.save(found);

        return ServiceResponse.<Device>builder()
            .status(ServiceResponse.Status.OK)
            .result(updated)
            .<Device>build();
    }

    @Override
    public ServiceResponse<Device> update(Tenant tenant, String id, Device updatingDevice) {
        if (!Optional.ofNullable(id).isPresent())
            return ServiceResponse.<Device>builder()
                    .status(ServiceResponse.Status.ERROR)
                    .responseMessage("Cannot update device with null ID")
                    .<Device>build();

        if (!Optional.ofNullable(updatingDevice).isPresent())
            return ServiceResponse.<Device>builder()
                    .status(ServiceResponse.Status.ERROR)
                    .responseMessage("Cannot update null device")
                    .<Device>build();

        Device deviceFromDB = getById(tenant, id).getResult();
        if (deviceFromDB == null) {
            return ServiceResponse.<Device>builder()
                    .responseMessages(Arrays.asList(new String[] { "Device ID does not exists" }))
                    .status(ServiceResponse.Status.ERROR).<Device>build();
        }

        // modify "modifiable" fields
        deviceFromDB.setDescription(updatingDevice.getDescription());
        deviceFromDB.setName(updatingDevice.getName());
        deviceFromDB.setActive(updatingDevice.isActive());

        List<String> validations = deviceFromDB.applyValidations();

        if (validations != null) {
            return ServiceResponse.<Device>builder()
                    .responseMessages(validations).status(ServiceResponse.Status.ERROR).<Device>build();
        }

        Device saved = deviceRepository.save(deviceFromDB);

        return ServiceResponse.<Device>builder().status(ServiceResponse.Status.OK).result(saved).<Device>build();
    }

    @Override
    public ServiceResponse<Device> getById(Tenant tenant, String id) {
        try {
            Optional.ofNullable(id).orElseThrow(() -> new BusinessException("Id cannot be null"));
            Optional.ofNullable(tenant).orElseThrow(() -> new BusinessException("Tenant cannot be null"));

            Tenant t = Optional.ofNullable(tenantRepository.findByName(tenant.getName()))
                    .orElseThrow(() -> new BusinessException("Tenant does not exist"));

            Device device = Optional.ofNullable(deviceRepository.findByTenantAndId(t.getId(), id))
                    .orElseThrow(() -> new BusinessException("Device does not exist"));

            return ServiceResponse.<Device> builder().status(ServiceResponse.Status.OK).result(device)
                    .build();
        } catch (BusinessException be) {
            return ServiceResponse.<Device> builder().status(ServiceResponse.Status.ERROR)
                    .responseMessage(be.getMessage()).build();
        }
    }

}
