package com.konkerlabs.platform.registry.integration.processors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Gateway;
import com.konkerlabs.platform.registry.business.services.LocationTreeUtils;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.data.core.integration.converters.helper.ConverterHelper;
import com.konkerlabs.platform.registry.data.core.integration.gateway.RabbitGateway;
import com.konkerlabs.platform.utilities.parsers.json.JsonParsingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GatewayEventProcessor {

    public enum Messages {
        APIKEY_MISSING("integration.event_processor.api_key.missing"),
        CHANNEL_MISSING("integration.event_processor.channel.missing"),
        DEVICE_NOT_FOUND("integration.event_processor.channel.not_found"),
        INVALID_GATEWAY_LOCATION("integration.event_processor.gateway.location.invalid"),
        INVALID_PAYLOAD("integration.event_processor.payload.invalid");

        private String code;

        public String getCode() {
            return code;
        }

        Messages(String code) {
            this.code = code;
        }
    }

    private static final String GATEWAY_EVENT_DROPPED = "Incoming event has been dropped: [Gateway: {0}] - [Payload: {1}]";

    private static final Logger LOGGER = LoggerFactory.getLogger(GatewayEventProcessor.class);

    private DeviceRegisterService deviceRegisterService;
    private JsonParsingService jsonParsingService;
    private ConverterHelper converterHelper;
    private RabbitGateway rabbitGateway;

    @Autowired
    public GatewayEventProcessor(DeviceRegisterService deviceRegisterService,
                                 JsonParsingService jsonParsingService,
                                 ConverterHelper converterHelper,
                                 RabbitGateway rabbitGateway) {
        this.deviceRegisterService = deviceRegisterService;
        this.jsonParsingService = jsonParsingService;
        this.converterHelper = converterHelper;
        this.rabbitGateway = rabbitGateway;
    }

    private Boolean isValidAuthority(Gateway gateway, Device device) throws BusinessException {
        return LocationTreeUtils.isSublocationOf(gateway.getLocation(), device.getLocation());
    }

	public void process(Gateway gateway, String payloadList) throws BusinessException, JsonProcessingException {
    	List<Map<String, Object>> payloadsGateway = jsonParsingService.toListMap(payloadList);

    	for (Map<String, Object> payloadGateway : payloadsGateway) {
    		ServiceResponse<Device> result = deviceRegisterService.findByDeviceId(
    				gateway.getTenant(),
    				gateway.getApplication(),
    				payloadGateway.get("deviceId").toString());

    		if (result.isOk() && Optional.ofNullable(result.getResult()).isPresent()) {
    			Device device = result.getResult();

    			if (isValidAuthority(gateway, device)) {
                    if (!Optional.ofNullable(device.getApiKey()).isPresent()) {
                        ServiceResponse<DeviceRegisterService.DeviceSecurityCredentials> serviceResponse = deviceRegisterService.generateSecurityPassword(
                                device.getTenant(),
                                device.getApplication(),
                                device.getGuid());
                        device.setApiKey(serviceResponse.getResult().getDevice().getApiKey());
                    }

    				Map<String, Object> devicePayload = (Map<String, Object>) payloadGateway.get("payload");

    				Instant ingestedTimestamp = Instant.now();

                    byte[] payloadBytes = converterHelper.getJsonPayload(device, jsonParsingService.toJsonString(devicePayload)).getResult();
                    rabbitGateway.queueEventDataPub(device.getApiKey(),
                            payloadGateway.get("channel").toString(),
                            ingestedTimestamp.toEpochMilli(),
                            payloadBytes);
    			} else {
    				LOGGER.warn(MessageFormat.format("The gateway does not have authority over the device: {0}",
                            device.getName()),
                    		gateway.toURI(),
                    		gateway.getTenant().getLogLevel());
                }
    		} else {
                LOGGER.debug(MessageFormat.format(GATEWAY_EVENT_DROPPED,
                        gateway.toURI(),
                        payloadList),
                		gateway.toURI(),
                		gateway.getTenant().getLogLevel());
            }

		}

    }

    public void process(Gateway gateway,
                        String payloadList,
                        String deviceIdFieldName,
                        String deviceNameFieldName,
                        String deviceChannelFieldName) throws BusinessException, JsonProcessingException {
        List<Map<String, Object>> payloadsGateway = jsonParsingService.toListMap(payloadList);

        for (Map<String, Object> payloadDevice : payloadsGateway) {
            ServiceResponse<Device> result = deviceRegisterService.findByDeviceId(
                    gateway.getTenant(),
                    gateway.getApplication(),
                    payloadDevice.get(deviceIdFieldName).toString());

            if (!result.isOk()) {
                String deviceName = payloadDevice.get(deviceNameFieldName) != null
                        ? payloadDevice.get(deviceNameFieldName).toString()
                        : payloadDevice.get(deviceIdFieldName).toString();

                result = deviceRegisterService.register(
                        gateway.getTenant(),
                        gateway.getApplication(),
                        Device.builder()
                                .deviceId(payloadDevice.get(deviceIdFieldName).toString())
                                .name(deviceName)
                                .location(gateway.getLocation())
                                .active(true)
                                .build());
            }

            if (result.isOk() && Optional.ofNullable(result.getResult()).isPresent()) {
                Device device = result.getResult();

                if (isValidAuthority(gateway, device)) {
                    if (!Optional.ofNullable(device.getApiKey()).isPresent()) {
                        ServiceResponse<DeviceRegisterService.DeviceSecurityCredentials> serviceResponse = deviceRegisterService.generateSecurityPassword(
                                device.getTenant(),
                                device.getApplication(),
                                device.getGuid());
                        device.setApiKey(serviceResponse.getResult().getDevice().getApiKey());
                    }

                    Instant ingestedTimestamp = Instant.now();

                    String channel = null;
                    if (Optional.ofNullable(deviceChannelFieldName).isPresent()) {
                        channel = payloadDevice.get(deviceChannelFieldName).toString();
                    } else {
                        channel = "data";
                    }

                    byte[] payloadBytes = converterHelper.getJsonPayload(device, jsonParsingService.toJsonString(payloadDevice)).getResult();
                    rabbitGateway.queueEventDataPub(device.getApiKey(),
                            channel,
                            ingestedTimestamp.toEpochMilli(),
                            payloadBytes);
                } else {
                    LOGGER.warn(MessageFormat.format("The gateway does not have authority over the device: {0}",
                            device.getName()),
                            gateway.toURI(),
                            gateway.getTenant().getLogLevel());
                }
            } else {
                LOGGER.debug(MessageFormat.format(GATEWAY_EVENT_DROPPED,
                        gateway.toURI(),
                        payloadList),
                        gateway.toURI(),
                        gateway.getTenant().getLogLevel());
            }

        }

    }

}
