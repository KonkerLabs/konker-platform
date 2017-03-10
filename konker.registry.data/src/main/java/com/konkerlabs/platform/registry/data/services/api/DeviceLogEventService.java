package com.konkerlabs.platform.registry.data.services.api;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;

public interface DeviceLogEventService {

    ServiceResponse<Event> logIncomingEvent(Device device, Event event);

    ServiceResponse<Event> logOutgoingEvent(Device device, Event event);

}
