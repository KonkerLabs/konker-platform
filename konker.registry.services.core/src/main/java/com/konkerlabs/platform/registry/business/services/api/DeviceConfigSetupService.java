package com.konkerlabs.platform.registry.business.services.api;

import java.util.List;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.DeviceConfig;
import com.konkerlabs.platform.registry.business.model.DeviceConfigSetup;
import com.konkerlabs.platform.registry.business.model.DeviceModel;
import com.konkerlabs.platform.registry.business.model.Location;
import com.konkerlabs.platform.registry.business.model.Tenant;

public interface DeviceConfigSetupService {

    ServiceResponse<List<DeviceConfig>> listAll(Tenant tenant, Application application);

    ServiceResponse<String> findByModelAndLocation(Tenant tenant, Application application, DeviceModel model, Location location);

    ServiceResponse<DeviceConfig> saveOrUpdate(Tenant tenant, Application application, DeviceModel deviceModel,
            Location location, String json);

    ServiceResponse<DeviceConfigSetup> remove(Tenant tenant, Application application, DeviceModel deviceModel,
            Location location);

}
