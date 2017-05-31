package com.konkerlabs.platform.registry.integration.endpoints;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.services.api.DeviceConfigSetupService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.data.config.RabbitMQConfig;
import com.konkerlabs.platform.registry.integration.gateways.RabbitGateway;

@Service
public class DeviceConfigRabbitEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceConfigRabbitEndpoint.class);

    @Autowired
    private RabbitGateway rabbitGateway;

    @Autowired
    private DeviceConfigSetupService deviceConfigSetupService;

    @Autowired
    private DeviceRegisterService deviceRegisterService;

    @RabbitListener(queues = "mgmt.config.pub")
    public void onConfigPub(Message message) {

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("A message has arrived -> " + message.toString());

        MessageProperties properties = message.getMessageProperties();
        if (properties == null || properties.getHeaders().isEmpty()) {
            LOGGER.error("MessageProperties is empty");
            return;
        }

        String apiKey = (String) properties.getHeaders().get(RabbitMQConfig.MSG_HEADER_APIKEY);
		Device device = deviceRegisterService.findByApiKey(apiKey);

        if (!Optional.ofNullable(device).isPresent()) {
            LOGGER.error("Device does not exist");
            return;
        }

        ServiceResponse<String> serviceResponse = deviceConfigSetupService
        		.findByModelAndLocation(device.getTenant(),
        				device.getApplication(),
        				device.getDeviceModel(),
        				device.getLocation());

        String config = Optional.ofNullable(serviceResponse.getResult()).orElse(" { } ");

        rabbitGateway.sendConfig(apiKey, config);

    }

}
