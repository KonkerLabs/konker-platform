package com.konkerlabs.platform.registry.business.model;

import com.konkerlabs.platform.registry.business.model.enumerations.IntegrationType;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public abstract class TransformationStep {

    private IntegrationType type;
    @Singular
    private Map<String,String> attributes = new HashMap<>();

    public TransformationStep() {
    }

    public abstract Set<String> applyValidations();
}
