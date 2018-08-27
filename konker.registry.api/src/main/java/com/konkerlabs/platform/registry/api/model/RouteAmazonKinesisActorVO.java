package com.konkerlabs.platform.registry.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@JsonInclude(Include.NON_EMPTY)
public class RouteAmazonKinesisActorVO
        extends RouteActorVO {

    public RouteAmazonKinesisActorVO() {
        super();
        this.setType(RouteActorVO.TYPE_AMAZON_KINESIS);
    }

    @ApiModelProperty(position = 1, value = "key", example = "key")
    private String key;

    @ApiModelProperty(position = 2, value = "secret", example = "secret")
    private String secret;

    @ApiModelProperty(position = 3, value = "region", example = "us-east-1")
    private String region;

    @ApiModelProperty(position = 4, value = "stream", example = "stream")
    private String streamName;

}
