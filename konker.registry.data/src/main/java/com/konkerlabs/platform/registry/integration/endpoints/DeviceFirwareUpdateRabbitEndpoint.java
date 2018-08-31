package com.konkerlabs.platform.registry.integration.endpoints;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.DeviceFwUpdate;
import com.konkerlabs.platform.registry.business.services.api.DeviceFirmwareUpdateService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.data.config.RabbitMQDataConfig;
import com.konkerlabs.platform.registry.integration.gateways.RabbitGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
public class DeviceFirwareUpdateRabbitEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceFirwareUpdateRabbitEndpoint.class);

    @Autowired
    private RabbitGateway rabbitGateway;

    @Autowired
    private DeviceFirmwareUpdateService deviceFirmwareUpdateService;

    @Autowired
    private DeviceRegisterService deviceRegisterService;

    public void setRabbitGateway(RabbitGateway rabbitGateway) {
        this.rabbitGateway = rabbitGateway;
    }

    public void setDeviceFirmwareUpdateService(DeviceFirmwareUpdateService deviceFirmwareUpdateService) {
        this.deviceFirmwareUpdateService = deviceFirmwareUpdateService;
    }

    public void setDeviceRegisterService(DeviceRegisterService deviceRegisterService) {
        this.deviceRegisterService = deviceRegisterService;
    }

    @RabbitListener(queues = "mgmt.fw.pub")
    public void onFwPub(Message message) {

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("A message has arrived -> " + message.toString());

        MessageProperties properties = message.getMessageProperties();
        if (properties == null || properties.getHeaders().isEmpty()) {
            LOGGER.error("MessageProperties is empty");
            return;
        }

        String apiKey = (String) properties.getHeaders().get(RabbitMQDataConfig.MSG_HEADER_APIKEY);
        if (!StringUtils.hasText(apiKey)) {
            LOGGER.error("Apikey not found.");
            return;
        }

		Device device = deviceRegisterService.findByApiKey(apiKey);

        if (!Optional.ofNullable(device).isPresent()) {
            LOGGER.error("Device does not exist");
            return;
        }

        ServiceResponse<DeviceFwUpdate> serviceResponse = deviceFirmwareUpdateService.findPendingFwUpdateByDevice(
                        device.getTenant(),
        				device.getApplication(),
        				device);
        String result="{ }";

        if (Optional.ofNullable(serviceResponse.getResult()).isPresent()){
            result="{binario/hash,ver??}" ;
        }

        rabbitGateway.sendFirmwareUpdate(apiKey, result);

    }


    @RabbitListener(queues = "mgmt.fw.updated")
    public void onFwPubUpdated(Message message) {

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("A message has arrived -> " + message.toString());

        MessageProperties properties = message.getMessageProperties();
        if (properties == null || properties.getHeaders().isEmpty()) {
            LOGGER.error("MessageProperties is empty");
            return;
        }

        String apiKey = (String) properties.getHeaders().get(RabbitMQDataConfig.MSG_HEADER_APIKEY);
        if (!StringUtils.hasText(apiKey)) {
            LOGGER.error("Apikey not found.");
            return;
        }

        Device device = deviceRegisterService.findByApiKey(apiKey);

        if (!Optional.ofNullable(device).isPresent()) {
            LOGGER.error("Device does not exist");
            return;
        }

        ServiceResponse<DeviceFwUpdate> serviceResponse = deviceFirmwareUpdateService.findPendingFwUpdateByDevice(
                device.getTenant(),
                device.getApplication(),
                device);
        String result="{ }";

        if (Optional.ofNullable(serviceResponse.getResult()).isPresent()){
            result="{binario/hash,ver??}" ;
        }

        rabbitGateway.sendFirmwareUpdate(apiKey, result);

    }

}
