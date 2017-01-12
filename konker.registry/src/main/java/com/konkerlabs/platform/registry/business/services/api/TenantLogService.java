package com.konkerlabs.platform.registry.business.services.api;


import java.util.List;

import com.konkerlabs.platform.registry.audit.model.TenantLog;
import com.konkerlabs.platform.registry.business.model.Tenant;

public interface TenantLogService {

	ServiceResponse<List<TenantLog>> findByTenant(Tenant tenant, boolean ascending);

}
