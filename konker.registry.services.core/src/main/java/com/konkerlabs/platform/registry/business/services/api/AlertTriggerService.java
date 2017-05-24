package com.konkerlabs.platform.registry.business.services.api;

import java.util.List;

import com.konkerlabs.platform.registry.business.model.AlertTrigger;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Tenant;

public interface AlertTriggerService {

    ServiceResponse<List<AlertTrigger>> listByTenantAndApplication(Tenant tenant, Application application);

}
