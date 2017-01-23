package com.konkerlabs.platform.registry.web.forms;

import com.konkerlabs.platform.registry.business.model.RestTransformationStep;
import com.konkerlabs.platform.registry.business.model.Transformation;
import com.konkerlabs.platform.registry.business.model.enumerations.SupportedHttpMethod;
import com.konkerlabs.platform.registry.web.forms.api.ModelBuilder;
import lombok.Data;

import java.util.*;
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
        private List<TransformationStepHeaderForm> headers = new ArrayList<>();

        public TransformationStepForm() {
        }

        public TransformationStepForm(String method,
                                      String url,
                                      String username,
                                      String password,
                                      List<TransformationStepHeaderForm> headers) {
            this.method = method;
            this.url = url;
            this.username = username;
            this.password = password;
            List<TransformationStepHeaderForm> defaultHeaders = new ArrayList<TransformationStepHeaderForm>();
            if (headers == null) {
                headers = new ArrayList<>();
            }
            headers.add(
                    new TransformationStepHeaderForm("Content-Type", "application/json"))
            ;
            this.headers = headers != null ? headers : defaultHeaders;
        }

        public TransformationStepForm(String method,
                                      String url,
                                      String username,
                                      String password,
                                      Map<String, String> headers) {

            this.method = method;
            this.url = url;
            this.username = username;
            this.password = password;
            List<TransformationStepHeaderForm> headerFromMap = new ArrayList<>();
            if (headers == null) {
                headers = new HashMap<>();
            }
            headers.forEach((key, value) -> {
                headerFromMap.add(new TransformationStepHeaderForm(key, value));
            });
            this.headers = headerFromMap;
        }

        public TransformationStepForm(String method,
                                      String url,
                                      String username,
                                      String password) {

            this.method = method;
            this.url = url;
            this.username = username;
            this.password = password;
        }

        public Map<String, String> getHeadersAsMap() {
            Map<String, String> headersAsMap = new HashMap<>();
            getHeaders().stream().forEach(item -> {
                headersAsMap.put(item.getKey(), item.getValue());
            });
            return headersAsMap;
        }
    }

    @Data
    public static class TransformationStepHeaderForm {
        private String key;
        private String value;

        public TransformationStepHeaderForm() {
        }

        public TransformationStepHeaderForm(String key, String value) {
            this.key = key;
            this.value = value;
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
                                        .attributes(new HashMap<String, Object>() {
                                            {
                                                put(RestTransformationStep.REST_ATTRIBUTE_METHOD, transformationStep.getMethod());
                                                put(RestTransformationStep.REST_URL_ATTRIBUTE_NAME, transformationStep.getUrl());
                                                put(RestTransformationStep.REST_USERNAME_ATTRIBUTE_NAME, transformationStep.getUsername());
                                                put(RestTransformationStep.REST_PASSWORD_ATTRIBUTE_NAME, transformationStep.getPassword());
                                                put(RestTransformationStep.REST_ATTRIBUTE_HEADERS, transformationStep.getHeadersAsMap());
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
                getSteps().add(
                        new TransformationStepForm(
                                (String) transformationStep.getAttributes().get(RestTransformationStep.REST_ATTRIBUTE_METHOD),
                                (String) transformationStep.getAttributes().get(RestTransformationStep.REST_URL_ATTRIBUTE_NAME),
                                (String) transformationStep.getAttributes().get(RestTransformationStep.REST_USERNAME_ATTRIBUTE_NAME),
                                (String) transformationStep.getAttributes().get(RestTransformationStep.REST_PASSWORD_ATTRIBUTE_NAME),
                                (Map<String, String>) transformationStep.getAttributes().get(RestTransformationStep
                                        .REST_ATTRIBUTE_HEADERS)
                        ))
        );
        return this;
    }


    public SupportedHttpMethod[] getMethodList() {
        return SupportedHttpMethod.values();
    }


}
