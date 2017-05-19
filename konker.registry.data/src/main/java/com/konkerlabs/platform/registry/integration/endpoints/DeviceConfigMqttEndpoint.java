package com.konkerlabs.platform.registry.integration.endpoints;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.services.api.DeviceConfigSetupService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.integration.gateways.MqttMessageGateway;

@MessageEndpoint
public class DeviceConfigMqttEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceConfigMqttEndpoint.class);

    private static final String MQTT_SUB_TOPIC = "mgmt/{0}/sub/cfg";
    
    private MessageSource messageSource;
    private DeviceConfigSetupService deviceConfigSetupService;
    private DeviceRegisterService deviceRegisterService;
    private MqttMessageGateway mqttMessageGateway;

    @Autowired
    public DeviceConfigMqttEndpoint(DeviceConfigSetupService deviceConfigSetupService,
    								DeviceRegisterService deviceRegisterService,
    								MessageSource messageSource,
    								MqttMessageGateway mqttMessageGateway) {
        this.deviceConfigSetupService = deviceConfigSetupService;
        this.deviceRegisterService = deviceRegisterService;
        this.messageSource = messageSource;
        this.mqttMessageGateway = mqttMessageGateway;
    }

    @ServiceActivator(inputChannel = "konkerMqttInputConfigChannel")
    public void onEvent(Message<String> message) throws MessagingException {

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("A message has arrived -> " + message.toString());

        Object topic = message.getHeaders().get(MqttHeaders.TOPIC);
        if (topic == null || topic.toString().isEmpty()) {
            throw new MessagingException(message,"Topic cannot be null or empty");
        }

        String apikey = extractFromResource(topic.toString(),1);
		Device device = deviceRegisterService.findByApiKey(apikey);
        
        if (!Optional.ofNullable(device).isPresent()) {
        	throw new MessagingException(message, "Device does not exist");
        }
        
        ServiceResponse<String> serviceResponse = deviceConfigSetupService
        		.findByModelAndLocation(device.getTenant(), 
        				device.getApplication(), 
        				device.getDeviceModel(), 
        				device.getLocation());
        
        String config = Optional.ofNullable(serviceResponse.getResult()).orElse(" { } ");
        
        mqttMessageGateway.send(config, MessageFormat.format(MQTT_SUB_TOPIC, apikey));
    }

	private String extractFromResource(String channel, int index) {
        try {
            return channel.split("/")[index];
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }
}
