package com.konkerlabs.platform.registry.business.repositories.solr;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Tenant;

public interface EventRepository {

    void push(Tenant tenant, Event event) throws BusinessException;

}
