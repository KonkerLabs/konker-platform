package com.konkerlabs.platform.registry.business.services.api;


import com.konkerlabs.platform.registry.audit.model.TenantLog;
import com.konkerlabs.platform.registry.business.model.Tenant;

import java.util.List;

public interface TenantLogService {

	ServiceResponse<List<TenantLog>> findByTenant(Tenant tenant, boolean ascendingOrder);

}
