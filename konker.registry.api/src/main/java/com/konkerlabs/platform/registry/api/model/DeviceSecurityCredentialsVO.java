package com.konkerlabs.platform.registry.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService.DeviceDataURLs;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService.DeviceSecurityCredentials;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(Include.NON_EMPTY)
@ApiModel(value = "Device Security Credentials", discriminator = "com.konkerlabs.platform.registry.web.model")
public class DeviceSecurityCredentialsVO {

    @ApiModelProperty(position = 0, value = "the device username (web key)", example = "L12UXrlnPd")
    protected String username;
    @ApiModelProperty(position = 2, value = "Publish Events REST URL", example = "http://server/pub/2q7kibmutjdj/<Channel>")
    private String httpURLPub;
    @ApiModelProperty(position = 3, value = "Subscribe Events REST URL", example = "http://server/sub/2q7kibmutjdj/<Channel>")
    private String httpURLSub;
    @ApiModelProperty(position = 4, value = "Publish Events REST Secure URL", example = "https://server/pub/2q7kibmutjdj/<Channel>")
    private String httpsURLPub;
    @ApiModelProperty(position = 5, value = "Subscribe Events REST Secure URL", example = "https://server/sub/2q7kibmutjdj/<Channel>")
    private String httpsURLSub;
    @ApiModelProperty(position = 6, value = "Events MQTT URL", example = "mqtt://dev-server")
    private String mqttURL;
    @ApiModelProperty(position = 7, value = "Events MQTT Secure URL", example = "mqtts://dev-server")
    private String mqttsURL;
    @ApiModelProperty(position = 8, value = "MQTT Publish Topic", example = "pub/2q7kibmutjdj/<Channel>")
    private String mqttPubTopic;
    @ApiModelProperty(position = 9, value = "MQTT Subscribe Topic", example = "sub/2q7kibmutjdj/<Channel>")
    private String mqttSubTopic;

    public DeviceSecurityCredentialsVO(DeviceSecurityCredentials credentials, DeviceRegisterService.DeviceDataURLs deviceDataURLs) {

        this.username = credentials.getDevice().getApiKey();
        setDeviceDataURLs(deviceDataURLs);

    }

    public DeviceSecurityCredentialsVO setDeviceDataURLs(DeviceDataURLs deviceDataURLs) {

        this.httpURLPub = deviceDataURLs.getHttpURLPub();
        this.httpURLSub = deviceDataURLs.getHttpURLSub();
        this.httpsURLPub = deviceDataURLs.getHttpsURLPub();
        this.httpsURLSub = deviceDataURLs.getHttpsURLSub();
        this.mqttURL = deviceDataURLs.getMqttURL();
        this.mqttsURL = deviceDataURLs.getMqttsURL();
        this.mqttPubTopic = deviceDataURLs.getMqttPubTopic();
        this.mqttSubTopic = deviceDataURLs.getMqttSubTopic();

        return this;
    }

}
