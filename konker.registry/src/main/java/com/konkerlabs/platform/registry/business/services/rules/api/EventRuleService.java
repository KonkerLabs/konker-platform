package com.konkerlabs.platform.registry.business.services.rules.api;


import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.EventRule;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;

import java.net.URI;
import java.util.List;

public interface EventRuleService {
    ServiceResponse<EventRule> save(Tenant tenant, EventRule rule) throws BusinessException;
    List<EventRule> getAll(Tenant tenant);
    EventRule findById(String id);
    List<EventRule> findByIncomingUri(URI uri);
}