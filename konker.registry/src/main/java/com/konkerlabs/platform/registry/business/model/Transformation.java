package com.konkerlabs.platform.registry.business.model;

import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.utilities.validations.api.Validatable;
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
public class Transformation implements Validatable {

    public enum Validations {
        NAME_NULL("model.transformation.name.not_null"),
        STEPS_EMPTY("model.tranformation.steps.not_empty");

        public String getCode() {
            return code;
        }

        private String code;

        Validations(String code) {
            this.code = code;
        }
    }

    @Id
    private String id;
    @DBRef
    private Tenant tenant;
    private String name;
    private String description;
    @Singular
    private List<TransformationStep> steps = new LinkedList<>();

    public Optional<Map<String, Object[]>> applyValidations() {
        Map<String, Object[]> validations = new HashMap<>();

        if (!Optional.ofNullable(getTenant()).isPresent())
            validations.put(CommonValidations.TENANT_NULL.getCode(),null);
        if (!Optional.ofNullable(getName()).filter(s -> !s.isEmpty()).isPresent())
            validations.put(Validations.NAME_NULL.getCode(),null);
        if (Optional.of(getSteps()).filter(transformationSteps -> transformationSteps.isEmpty()).isPresent())
            validations.put(Validations.STEPS_EMPTY.getCode(),null);

        steps.stream()
            .forEach(transformationStep -> {
                Optional.of(transformationStep.applyValidations())
                        .filter(op -> !Optional.empty().equals(op))
                        .ifPresent(op -> validations.putAll(op.get()));
            });

        return Optional.of(validations).filter(stringMap -> !stringMap.isEmpty());
    }
}
