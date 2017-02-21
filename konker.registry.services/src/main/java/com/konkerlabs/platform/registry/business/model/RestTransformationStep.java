package com.konkerlabs.platform.registry.business.model;

import com.konkerlabs.platform.registry.business.model.enumerations.IntegrationType;
import com.konkerlabs.platform.utilities.validations.InterpolableURIValidator;
import lombok.Builder;
import lombok.EqualsAndHashCode;

import java.util.*;

@EqualsAndHashCode(callSuper = true)
public class RestTransformationStep extends TransformationStep {

    public enum Validations {
        ATTRIBUTES_NULL_EMPTY("model.transformation.rest.attributes.not_empty"),
        ATTRIBUTES_HEADERS_INVALID  ("model.transformation.rest.attributes.headers.invalid"),
        ATTRIBUTES_METHOD_MISSING("model.transformation.rest.attributes.method.missing"),
        ATTRIBUTES_URL_MISSING("model.transformation.rest.attributes.url.missing"),
        ATTRIBUTES_USERNAME_MISSING("model.transformation.rest.attributes.username.missing"),
        ATTRIBUTES_PASSWORD_MISSING("model.transformation.rest.attributes.password.missing");

        private String code;

        Validations(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    public static final String REST_ATTRIBUTE_METHOD = "method";
    public static final String REST_ATTRIBUTE_HEADERS = "headers";
    public static final String REST_URL_ATTRIBUTE_NAME = "url";
    public static final String REST_USERNAME_ATTRIBUTE_NAME = "username";
    public static final String REST_PASSWORD_ATTRIBUTE_NAME = "password";
    public static final String URI_SCHEME = "resttransformationstep";


    @Override
    public String getUriScheme() {
        return URI_SCHEME;
    }

    @Override
    public String getContext() {
        return (String) this.getAttributes().get(REST_ATTRIBUTE_METHOD);
    }

    @Override
    public String getGuid() {
        return (String) getAttributes().get(REST_URL_ATTRIBUTE_NAME);
    }

    @Builder
    public RestTransformationStep(Map<String, Object> attributes) {
        super(IntegrationType.REST, attributes);
    }

    @Override
    public Optional<Map<String, Object[]>> applyValidations() {
        Map<String, Object[]> validations = new HashMap<>();

        if (!Optional.ofNullable(getAttributes()).filter(attributes -> !attributes.isEmpty()).isPresent())
            validations.put(Validations.ATTRIBUTES_NULL_EMPTY.getCode(),null);

        Optional.ofNullable(getAttributes()).filter(attributes -> !attributes.isEmpty())
            .ifPresent(attr -> {
                if (!attr.containsKey(REST_ATTRIBUTE_METHOD))
                    validations.put(Validations.ATTRIBUTES_METHOD_MISSING.getCode(),null);
                if (!attr.containsKey(REST_URL_ATTRIBUTE_NAME) ||
                        (attr.get(REST_URL_ATTRIBUTE_NAME) != null &&
                        ((String) attr.get(REST_URL_ATTRIBUTE_NAME)).isEmpty())) {
                    validations.put(Validations.ATTRIBUTES_URL_MISSING.getCode(),null);
                } else {
                    InterpolableURIValidator.to((String) attr.get(REST_URL_ATTRIBUTE_NAME))
                            .applyValidations()
                            .filter(stringMap -> !stringMap.isEmpty())
                            .ifPresent(stringMap -> {
                                validations.putAll(stringMap);
                            });
                }
                if (!attr.containsKey(REST_USERNAME_ATTRIBUTE_NAME))
                    validations.put(Validations.ATTRIBUTES_USERNAME_MISSING.getCode(),null);
                if (!attr.containsKey(REST_PASSWORD_ATTRIBUTE_NAME))
                    validations.put(Validations.ATTRIBUTES_PASSWORD_MISSING.getCode(),null);
            });

        return Optional.of(validations).filter(stringMap -> !stringMap.isEmpty());
    }
}
