package com.konkerlabs.platform.registry.business.model;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

@Data
@Builder
@Document(collection = "transformations")
public class Transformation {

    @Id
    private String id;
    @DBRef
    private Tenant tenant;
    private String name;
    private String description;
    @Singular
    private List<TransformationStep> steps = new LinkedList<>();

    public Set<String> applyValidation() {
        Set<String> validations = new HashSet<>();

        if (!Optional.ofNullable(getTenant()).isPresent())
            validations.add("Tenant cannot be null");
        if (!Optional.ofNullable(getName()).filter(s -> !s.isEmpty()).isPresent())
            validations.add("Name cannot be null or empty");
        if (Optional.of(getSteps()).filter(transformationSteps -> transformationSteps.isEmpty()).isPresent())
            validations.add("At least one transformation step is needed");

        steps.stream()
            .forEach(transformationStep -> {
                Optional.ofNullable(transformationStep.applyValidations())
                    .ifPresent(strings -> validations.addAll(strings));
            });

        return validations;
    }
}
