package com.konkerlabs.platform.registry.business.model.validation;

import java.util.Map;

public interface Validatable {

    Map<String, Object[]> applyValidations();

}
