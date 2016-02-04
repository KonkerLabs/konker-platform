package com.konkerlabs.platform.registry.business.services.api;


import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.EventRule;

import java.util.List;

public interface EventRuleService {
    ServiceResponse create(EventRule rule) throws BusinessException;

    List<EventRule> getAll();
}