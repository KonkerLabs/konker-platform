package com.konkerlabs.platform.registry.business.model;

import lombok.Builder;

import java.util.*;

public class RestTransformationStep extends Transformation.TransformationStep {

    @Builder
    public RestTransformationStep(Map<String, String> attributes) {
        super(TransformationType.REST, attributes);
    }

    @Override
    public Set<String> applyValidations() {
        Set<String> validations = new HashSet<>();

        if (!Optional.ofNullable(getAttributes()).filter(attributes -> !attributes.isEmpty()).isPresent())
            validations.add("REST step attributes cannot be null or empty");

        Optional.ofNullable(getAttributes()).filter(attributes -> !attributes.isEmpty())
            .ifPresent(attr -> {
                if (!attr.containsKey("url") || attr.get("url").isEmpty())
                    validations.add("REST step: URL attribute is missing");
                if (!attr.containsKey("username"))
                    validations.add("REST step: Username attribute is missing");
                if (!attr.containsKey("password"))
                    validations.add("REST step: Password attribute is missing");
            });

        return validations;
    }
}
