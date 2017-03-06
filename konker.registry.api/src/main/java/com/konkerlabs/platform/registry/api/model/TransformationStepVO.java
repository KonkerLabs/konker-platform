package com.konkerlabs.platform.registry.api.model;


import com.konkerlabs.platform.registry.api.model.core.SerializableVO;
import com.konkerlabs.platform.registry.business.model.TransformationStep;
import com.konkerlabs.platform.registry.business.model.enumerations.IntegrationType;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Singular;
import org.springframework.data.annotation.Transient;

import java.util.HashMap;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@ApiModel(
        value = "TransformationStep",
        discriminator = "com.konkerlabs.platform.registry.api.model")
public class TransformationStepVO
        implements
        SerializableVO<TransformationStep, TransformationStepVO> {

    private IntegrationType type;
    @Singular
    private Map<String, Object> attributes = new HashMap<>();

    @Transient
    @Override
    public TransformationStepVO apply(TransformationStep t) {
        TransformationStepVO r = new TransformationStepVO();
        r.setType(t.getType());
        r.setAttributes(t.getAttributes());
        return r;
    }

    @Override
    public TransformationStep applyDB(TransformationStep t) {
        return t;
    }
}
