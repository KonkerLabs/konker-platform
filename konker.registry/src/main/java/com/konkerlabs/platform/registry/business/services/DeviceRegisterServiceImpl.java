package com.konkerlabs.platform.registry.business.services;

import java.util.Arrays;
import java.util.List;

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
    public ServiceResponse register(Device device) throws BusinessException {
        if (device == null)
            throw new BusinessException("Record cannot be null");

        device.onRegistration();

        device.setTenant(tenantRepository.findByName("Konker"));

        if (device.getTenant() == null) {
            return ServiceResponse.builder()
                    .responseMessages(Arrays.asList(new String[] { "Default tenant does not exist" }))
                    .status(ServiceResponse.Status.ERROR).build();
        }

        List<String> validations = device.applyValidations();

        if (validations != null)
            return ServiceResponse.builder().responseMessages(validations).status(ServiceResponse.Status.ERROR).build();

        if (deviceRepository.findByDeviceId(device.getDeviceId()) != null) {
            return ServiceResponse.builder()
                    .responseMessages(Arrays.asList(new String[] { "Device ID already registered" }))
                    .status(ServiceResponse.Status.ERROR).build();
        }

        deviceRepository.save(device);

        return ServiceResponse.builder().status(ServiceResponse.Status.OK).build();
    }

    @Override
    public List<Device> getAll() {
        return deviceRepository.findAll();
    }

    @Override
    public Device findById(String deviceId) {
        return deviceRepository.findByDeviceId(deviceId);
    }

    @Override
    public Device logEvent(String deviceId, String payload) {
        return null;
    }

    @Override
    public ServiceResponse update(String deviceId, Device updatingDevice) throws BusinessException {
        if (deviceId == null) {
            throw new BusinessException("Cannot update device with null ID");
        }

        if (updatingDevice == null) {
            throw new BusinessException("Cannot update null device");
        }

        if (!deviceId.equalsIgnoreCase(updatingDevice.getDeviceId())) {
            return ServiceResponse.builder().responseMessages(Arrays.asList(new String[] { "Cannot modify device ID" }))
                    .status(ServiceResponse.Status.ERROR).build();

        }

        Device deviceFromDB = findById(deviceId);
        if (deviceFromDB == null) {
            return ServiceResponse.builder()
                    .responseMessages(Arrays.asList(new String[] { "Device ID does not exists" }))
                    .status(ServiceResponse.Status.ERROR).build();
        }

        // modify "modifiable" fields
        deviceFromDB.setDescription(updatingDevice.getDescription());
        deviceFromDB.setName(updatingDevice.getName());

        List<String> validations = deviceFromDB.applyValidations();

        if (validations != null) {
            return ServiceResponse.builder().responseMessages(validations).status(ServiceResponse.Status.ERROR).build();
        }

        deviceRepository.save(deviceFromDB);

        return ServiceResponse.builder().status(ServiceResponse.Status.OK).build();
    }
}
