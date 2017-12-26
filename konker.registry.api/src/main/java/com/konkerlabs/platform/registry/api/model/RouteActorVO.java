package com.konkerlabs.platform.registry.api.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.konkerlabs.platform.registry.business.model.AmazonKinesis;
import com.konkerlabs.platform.registry.business.model.EventRoute;
import com.konkerlabs.platform.registry.business.model.EventRoute.RouteActor;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_EMPTY)
@JsonTypeInfo( use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes( {
        @Type( value = RouteDeviceActorVO.class, name = RouteActorVO.TYPE_DEVICE ),
        @Type( value = RouteRestActorVO.class, name = RouteActorVO.TYPE_REST ),
        @Type( value = RouteModelLocationActorVO.class, name = RouteActorVO.TYPE_MODEL_LOCATION ),
        @Type( value = RouteAmazonKinesisActorVO.class, name = RouteActorVO.TYPE_AMAZON_KINESIS ),
        @Type( value = RouteApplicationActorVO.class, name = RouteActorVO.TYPE_APPLICATION )
} )
public class RouteActorVO {

    public static final String TYPE_DEVICE = "DEVICE";
    public static final String TYPE_REST = "REST";
    public static final String TYPE_MODEL_LOCATION = "MODEL_LOCATION";
    public static final String TYPE_AMAZON_KINESIS = "AMAZON_KINESIS";
    public static final String TYPE_APPLICATION = "APPLICATION";

    @ApiModelProperty(position = 0, value = "type",
            allowableValues = TYPE_DEVICE + "," + TYPE_REST + "," + TYPE_MODEL_LOCATION + "," + TYPE_AMAZON_KINESIS + "," + TYPE_APPLICATION, 
            example = TYPE_DEVICE)
    private String type;

    public RouteActorVO apply(RouteActor t) {

        if (t.isDevice()) {

            String uriPath = t.getUri().getPath();

            RouteDeviceActorVO vo = new RouteDeviceActorVO();
            vo.setType(RouteActorType.DEVICE.name());
            vo.setGuid(uriPath.startsWith("/") ? uriPath.substring(1) : uriPath);
            vo.setChannel(t.getData().get(EventRoute.DEVICE_MQTT_CHANNEL));

            return vo;

        } else if (t.isRestDestination()) {

            String uriPath = t.getUri().getPath();

            RouteRestActorVO vo = new RouteRestActorVO();
            vo.setType(RouteActorType.REST.name());
            vo.setGuid(uriPath.startsWith("/") ? uriPath.substring(1) : uriPath);

            return vo;

        } else if (t.isModelLocation()) {

            Pattern pattern = Pattern.compile("/?(.*?)/(.*?)");

            String uriPath = t.getUri().getPath();
            Matcher matcher = pattern.matcher(uriPath);

            RouteModelLocationActorVO vo = new RouteModelLocationActorVO();
            vo.setType(RouteActorType.MODEL_LOCATION.name());
            if (matcher.matches()) {
                vo.setDeviceModelGuid(matcher.group(1));
                vo.setLocationGuid(matcher.group(2));
            }
            vo.setChannel(t.getData().get(EventRoute.DEVICE_MQTT_CHANNEL));

            return vo;

        } else if (t.isAmazonKinesis()) {

            AmazonKinesis amazonKinesisProperties = AmazonKinesis.builder().build();
            amazonKinesisProperties.setValues(t.getData());

            RouteAmazonKinesisActorVO vo = new RouteAmazonKinesisActorVO();
            vo.setKey(amazonKinesisProperties.getKey());
            //vo.setSecret(amazonKinesisProperties.getSecret());
            vo.setRegion(amazonKinesisProperties.getRegion());
            vo.setStreamName(amazonKinesisProperties.getStreamName());

            return vo;

        } else if (t.isApplication()) {
        	
        	String uriPath = t.getUri().getPath();

            RouteApplicationActorVO vo = new RouteApplicationActorVO();
            vo.setType(RouteActorType.APPLICATION.name());
            vo.setName(uriPath.startsWith("/") ? uriPath.substring(1) : uriPath);

            return vo;
        }

        return null;

    }

    public RouteActor patchDB(RouteActor t) {
        return t;
    }

}
