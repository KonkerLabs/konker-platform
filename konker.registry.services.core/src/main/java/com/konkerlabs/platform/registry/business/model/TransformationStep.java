package com.konkerlabs.platform.registry.business.model;

import com.konkerlabs.platform.registry.business.model.behaviors.URIDealer;
import com.konkerlabs.platform.registry.business.model.enumerations.IntegrationType;
import com.konkerlabs.platform.utilities.validations.api.Validatable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public abstract class TransformationStep implements URIDealer, Validatable {

    private IntegrationType type;
    @Singular
    private Map<String, Object> attributes = new HashMap<>();

    public TransformationStep() {
    }
}
