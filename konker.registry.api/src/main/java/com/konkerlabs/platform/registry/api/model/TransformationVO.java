package com.konkerlabs.platform.registry.api.model;


import com.konkerlabs.platform.registry.api.model.core.SerializableVO;
import com.konkerlabs.platform.registry.business.model.RestTransformationStep;
import com.konkerlabs.platform.registry.business.model.Transformation;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Singular;
import org.springframework.data.annotation.Transient;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@ApiModel(
        value = "Transformation",
        discriminator = "com.konkerlabs.platform.registry.api.model")
public class TransformationVO
        implements SerializableVO<Transformation, TransformationVO> {

    private String id;
    private String name;
    private String guid;
    private String description;

    @Singular
    private List<TransformationStepVO> steps = new LinkedList<>();


    @Transient
    @Override
    public TransformationVO apply(Transformation t) {
        TransformationVO r = new TransformationVO();
        r.setId(t.getId());
        r.setGuid(t.getGuid());
        r.setName(t.getName());
        r.setDescription(t.getDescription());
        r.setSteps(new TransformationStepVO().apply(t.getSteps()));
        return r;
    }

    @Override
    public Transformation patchDB(Transformation t) {
        t.setDescription(this.getDescription());
        t.setName(this.getName());
        t.setSteps(this.getSteps().stream()
                .map( i -> i.patchDB(new RestTransformationStep(i.getAttributes())))
                .collect(Collectors.toList()));
        return t;
    }
}
