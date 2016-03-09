package com.konkerlabs.platform.registry.business.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;

import java.util.*;

@EqualsAndHashCode(callSuper = true)
public class RestTransformationStep extends Transformation.TransformationStep {

    public static final String REST_URL_ATTRIBUTE_NAME = "url";
    public static final String REST_USERNAME_ATTRIBUTE_NAME = "username";
    public static final String REST_PASSWORD_ATTRIBUTE_NAME = "password";

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
                if (!attr.containsKey(REST_URL_ATTRIBUTE_NAME) || attr.get(REST_URL_ATTRIBUTE_NAME).isEmpty())
                    validations.add("REST step: URL attribute is missing");
                if (!attr.containsKey(REST_USERNAME_ATTRIBUTE_NAME))
                    validations.add("REST step: Username attribute is missing");
                if (!attr.containsKey(REST_PASSWORD_ATTRIBUTE_NAME))
                    validations.add("REST step: Password attribute is missing");
            });

        return validations;
    }
}
