package com.konkerlabs.platform.registry.api.model;

import com.konkerlabs.platform.registry.api.model.core.SerializableVO;
import com.konkerlabs.platform.registry.business.model.Gateway;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@ApiModel(
        value = "Gateway",
        discriminator = "com.konkerlabs.platform.registry.api.model")
public class GatewayVO
        extends GatewayInputVO
        implements SerializableVO<Gateway, GatewayVO> {

    @ApiModelProperty(value = "the gateway guid", position = 0, example = "818599ad-3502-4e70-a852-fc7af8e0a9f3")
    private String guid;
    @ApiModelProperty(value = "the gateway name", example = "Gateway001", required = true, position = 2)
    protected String name;
    @ApiModelProperty(value = "the gateway description", example = "room 101 gateway", position = 3)
    protected String description;
    @ApiModelProperty(value = "the location name of gateway", example = "br_sp", position = 4)
    protected String locationName;
    @ApiModelProperty(example = "true", position = 6)
    protected boolean active = true;
    
    @Override
    public GatewayVO apply(Gateway t) {

        GatewayVO gatewayVO = new GatewayVO();
        gatewayVO.setGuid(t.getGuid());
        gatewayVO.setName(t.getName());
        gatewayVO.setDescription(t.getDescription());
        gatewayVO.setActive(t.isActive());
        gatewayVO.setLocationName(t.getLocation() != null ? t.getLocation().getName() : null);

        return gatewayVO;

    }

    @Override
    public Gateway patchDB(Gateway t) {
        t.setActive(this.isActive());
        t.setName(this.getName());
        t.setDescription(this.getDescription());
        t.setGuid(this.getGuid());
        return t;
    }

}
