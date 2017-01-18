package com.konkerlabs.platform.registry.web.forms;

import com.konkerlabs.platform.registry.business.model.RestTransformationStep;
import com.konkerlabs.platform.registry.business.model.Transformation;
import com.konkerlabs.platform.registry.business.model.enumerations.SupportedHttpMethod;
import com.konkerlabs.platform.registry.web.forms.api.ModelBuilder;
import lombok.Data;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
public class TransformationForm implements ModelBuilder<Transformation, TransformationForm, Void> {

    // colocar guid? TODO
    private String id;
    private String name;
    private String description;
    private List<TransformationStepForm> steps = new LinkedList() {
        {
            add(new TransformationStepForm());
        }
    };

    @Data
    public static class TransformationStepForm {
        private String method;
        private String url;
        private String username;
        private String password;

        public TransformationStepForm() {
        }

        public TransformationStepForm(String method, String url, String username, String password) {
            this.method = method;
            this.url = url;
            this.username = username;
            this.password = password;
        }
    }

    @Override
    public Transformation toModel() {
        return Transformation.builder()
                .id(getId())
                .name(getName())
                .description(getDescription())
                .steps(
                        steps.stream()
                                .map(transformationStep -> RestTransformationStep.builder()
                                        .attributes(new HashMap<String, String>() {
                                            {
                                                put(RestTransformationStep.REST_URL_ATTRIBUTE_METHOD, transformationStep.getMethod());
                                                put(RestTransformationStep.REST_URL_ATTRIBUTE_NAME, transformationStep.getUrl());
                                                put(RestTransformationStep.REST_USERNAME_ATTRIBUTE_NAME, transformationStep.getUsername());
                                                put(RestTransformationStep.REST_PASSWORD_ATTRIBUTE_NAME, transformationStep.getPassword());
                                            }
                                        }).build()).collect(Collectors.toList())
                ).build();
    }

    @Override
    public TransformationForm fillFrom(Transformation model) {
        setId(model.getId());
        setName(model.getName());
        setDescription(model.getDescription());
        getSteps().clear();
        model.getSteps().stream().forEachOrdered(transformationStep ->
                getSteps().add(new TransformationStepForm(
                        Optional.ofNullable(transformationStep.getAttributes().get(RestTransformationStep.REST_URL_ATTRIBUTE_METHOD)).isPresent() ?
                                transformationStep.getAttributes().get(RestTransformationStep.REST_URL_ATTRIBUTE_METHOD) : SupportedHttpMethod.POST.getCode(),
                        transformationStep.getAttributes().get(RestTransformationStep.REST_URL_ATTRIBUTE_NAME),
                        transformationStep.getAttributes().get(RestTransformationStep.REST_USERNAME_ATTRIBUTE_NAME),
                        transformationStep.getAttributes().get(RestTransformationStep.REST_PASSWORD_ATTRIBUTE_NAME)
                ))
        );
        return this;
    }

    public SupportedHttpMethod[] getMethodList() {
        return SupportedHttpMethod.values();
    }
}
