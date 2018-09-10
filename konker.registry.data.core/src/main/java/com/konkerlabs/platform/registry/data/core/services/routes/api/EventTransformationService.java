package com.konkerlabs.platform.registry.data.core.services.routes.api;

import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Transformation;

import java.util.Optional;

public interface EventTransformationService {

    Optional<Event> transform(Event original, Transformation transformation);

}
