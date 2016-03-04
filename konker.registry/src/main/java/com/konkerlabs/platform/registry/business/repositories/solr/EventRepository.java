package com.konkerlabs.platform.registry.business.repositories.solr;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;

public interface EventRepository {

    void push(Device device, Event event) throws BusinessException;

}
