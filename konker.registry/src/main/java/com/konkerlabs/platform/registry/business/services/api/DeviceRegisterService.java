package com.konkerlabs.platform.registry.business.services.api;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Tenant;

import java.util.List;

public interface DeviceRegisterService {
    ServiceResponse<Device> register(Tenant tenant, Device device);

    ServiceResponse<Device> update(Tenant tenant, String id, Device device);

    List<Device> getAll(Tenant tenant);

    // Device findById(String id);
    ServiceResponse<Device> getById(Tenant tenant, String id);

    Device findByApiKey(String apiKey);
<<<<<<< HEAD

    // TODO This method must be extinguished when event rule specialized URI
    // gets available
    Device findByDeviceId(String deviceId);

=======
>>>>>>> fce185f8bc8b129ea3cd0c4cc511b7259124c5aa
    Device findByTenantDomainNameAndDeviceId(String tenantDomainName, String deviceId);

    ServiceResponse<Device> switchActivation(Tenant tenant, String id);
}