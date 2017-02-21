package com.konkerlabs.platform.registry.business.services.api;


import com.konkerlabs.platform.registry.business.model.LoginAudit;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.model.LoginAudit.LoginAuditEvent;

public interface LoginAuditService {

	ServiceResponse<LoginAudit> register(Tenant tenant, User user, LoginAuditEvent event);

}
