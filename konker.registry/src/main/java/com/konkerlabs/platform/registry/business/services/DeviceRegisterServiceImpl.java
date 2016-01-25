package com.konkerlabs.platform.registry.business.services;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.repositories.DeviceRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
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

        if (device.getTenant()==null) {
            return ServiceResponse.builder()
                    .responseMessages(Arrays.asList(new String[]{"Default tenant does not exist"}))
                    .status(ServiceResponse.Status.ERROR)
                    .build();
        }

        List<String> validations = device.applyValidations();

        if (validations != null)
            return ServiceResponse.builder()
                .responseMessages(validations)
                .status(ServiceResponse.Status.ERROR)
                .build();

        if (deviceRepository.findByDeviceId(device.getDeviceId()) != null) {
            return ServiceResponse.builder()
                    .responseMessages(Arrays.asList(new String[]{"Device ID already registered"}))
                    .status(ServiceResponse.Status.ERROR)
                    .build();
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
}
