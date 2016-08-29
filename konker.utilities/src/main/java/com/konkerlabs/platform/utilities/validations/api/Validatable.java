package com.konkerlabs.platform.utilities.validations.api;

import java.util.Map;
import java.util.Optional;

public interface Validatable {

    Optional<Map<String, Object[]>> applyValidations();

}
