package com.konkerlabs.platform.registry.business.model.validation;

import java.util.Map;
import java.util.Optional;

public interface Validatable {

    Optional<Map<String, Object[]>> applyValidations();

}
