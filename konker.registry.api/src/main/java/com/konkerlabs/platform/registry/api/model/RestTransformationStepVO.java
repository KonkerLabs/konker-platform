package com.konkerlabs.platform.registry.api.model;


import com.konkerlabs.platform.registry.api.model.core.SerializableVO;
import com.konkerlabs.platform.registry.business.model.RestTransformationStep;
import com.konkerlabs.platform.registry.business.model.TransformationStep;
import com.konkerlabs.platform.registry.business.model.enumerations.IntegrationType;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Singular;
import org.springframework.data.annotation.Transient;

import java.util.*;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@ApiModel(
        value = "TransformationStep",
        discriminator = "com.konkerlabs.platform.registry.api.model")
public class RestTransformationStepVO
        implements
        SerializableVO<TransformationStep, RestTransformationStepVO> {

    private IntegrationType type = IntegrationType.REST;
    private String method;
    private String url;
    private String username;
    private String password;
    private Map<String, String> headers = new LinkedHashMap<>();

    @Transient
    @Override
    public RestTransformationStepVO apply(TransformationStep t) {
        RestTransformationStepVO r = new RestTransformationStepVO();
        r.setType(IntegrationType.REST);
        r.setMethod((String) t.getAttributes()
                .get(RestTransformationStep.REST_ATTRIBUTE_METHOD));
        r.setUsername((String) t.getAttributes()
                .get(RestTransformationStep.REST_USERNAME_ATTRIBUTE_NAME));
        r.setPassword((String) t.getAttributes()
                .get(RestTransformationStep.REST_PASSWORD_ATTRIBUTE_NAME));
        r.setUrl((String) t.getAttributes()
                .get(RestTransformationStep.REST_URL_ATTRIBUTE_NAME));
        r.setHeaders((Map) t.getAttributes()
                .get(RestTransformationStep.REST_ATTRIBUTE_HEADERS));
        return r;
    }

    @Override
    public TransformationStep patchDB(TransformationStep t) {
        return t;
    }
}
