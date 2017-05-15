package com.konkerlabs.platform.registry.api.model;

import com.konkerlabs.platform.registry.api.model.core.SerializableVO;
import com.konkerlabs.platform.registry.business.model.RestTransformationStep;
import com.konkerlabs.platform.registry.business.model.Transformation;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Singular;
import org.springframework.data.annotation.Transient;

import java.util.*;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@ApiModel(
        value = "Transformation",
        discriminator = "com.konkerlabs.platform.registry.web.model")
public class RestTransformationVO
        implements SerializableVO<Transformation, RestTransformationVO> {

    @ApiModelProperty(position = 0)
    private String id;
    @ApiModelProperty(value = "guid", example = "818599ad-3502-4e70-a852-fc7af8e0a9f3", position = 0)
    private String guid;
    @ApiModelProperty(value = "name", example = "convertValue", position = 1)
    private String name;
    @ApiModelProperty(value = "description", example = "this transformation does that", position = 2)
    private String description;

    @Singular
    private List<RestTransformationStepVO> steps = new LinkedList<>();

    @Transient
    @Override
    public RestTransformationVO apply(Transformation t) {
        RestTransformationVO r = new RestTransformationVO();
        r.setId(t.getId());
        r.setGuid(t.getGuid());
        r.setName(t.getName());
        r.setDescription(t.getDescription());
        r.setSteps(new RestTransformationStepVO().apply(t.getSteps()));
        return r;
    }

    @Override
    public Transformation patchDB(Transformation t) {
        t.setDescription(this.getDescription());
        t.setName(this.getName());
        t.setSteps(this.getSteps().stream()
                .map(i -> {
                            return new RestTransformationStep(new HashMap<String, Object>() {{
                                put(RestTransformationStep.REST_ATTRIBUTE_METHOD, i.getMethod());
                                put(RestTransformationStep.REST_URL_ATTRIBUTE_NAME, i.getUrl());
                                put(RestTransformationStep.REST_USERNAME_ATTRIBUTE_NAME, i.getUsername());
                                put(RestTransformationStep.REST_PASSWORD_ATTRIBUTE_NAME, i.getPassword());
                                put(RestTransformationStep.REST_ATTRIBUTE_HEADERS, i.getHeaders());
                            }});
                        }
                ).collect(Collectors.toList()));
        return t;
    }

}
