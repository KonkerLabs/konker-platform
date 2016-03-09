package com.konkerlabs.platform.registry.web.forms;

import com.konkerlabs.platform.registry.business.model.RestTransformationStep;
import com.konkerlabs.platform.registry.business.model.Transformation;
import com.konkerlabs.platform.registry.web.forms.api.ModelBuilder;
import lombok.Data;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class TransformationForm implements ModelBuilder<Transformation,TransformationForm,Void> {

    private String name;
    private String description;
    private List<TransformationStepForm> steps = new LinkedList(){
        {add(new TransformationStepForm());}
    };

    @Data
    public static class TransformationStepForm {
        private String url;
        private String username;
        private String password;

        public TransformationStepForm() {
        }

        public TransformationStepForm(String url, String username, String password) {
            this.url = url;
            this.username = username;
            this.password = password;
        }
    }

    @Override
    public Transformation toModel() {
        return Transformation.builder()
            .name(getName())
            .description(getDescription())
            .steps(
                steps.stream()
                    .map(transformationStep -> RestTransformationStep.builder()
                        .attributes(new HashMap<String,String>() {
                            {
                                put(RestTransformationStep.REST_URL_ATTRIBUTE_NAME,transformationStep.getUrl());
                                put(RestTransformationStep.REST_USERNAME_ATTRIBUTE_NAME,transformationStep.getUsername());
                                put(RestTransformationStep.REST_PASSWORD_ATTRIBUTE_NAME,transformationStep.getPassword());
                            }
                        }).build()).collect(Collectors.toList())
            ).build();
    }

    @Override
    public TransformationForm fillFrom(Transformation model) {
        setName(model.getName());
        setDescription(model.getDescription());
        getSteps().clear();
        model.getSteps().stream().forEachOrdered(transformationStep ->
            getSteps().add(new TransformationStepForm(
                transformationStep.getAttributes().get(RestTransformationStep.REST_URL_ATTRIBUTE_NAME),
                transformationStep.getAttributes().get(RestTransformationStep.REST_USERNAME_ATTRIBUTE_NAME),
                transformationStep.getAttributes().get(RestTransformationStep.REST_PASSWORD_ATTRIBUTE_NAME)
            ))
        );
        return this;
    }
}
