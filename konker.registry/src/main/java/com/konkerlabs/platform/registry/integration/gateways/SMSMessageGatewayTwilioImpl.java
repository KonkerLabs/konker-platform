package com.konkerlabs.platform.registry.integration.gateways;

import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.konkerlabs.platform.registry.integration.exceptions.IntegrationException;
import org.springframework.stereotype.Component;

@Component
public class SMSMessageGatewayTwilioImpl implements SMSMessageGateway {

    private static final Logger LOGGER = LoggerFactory.getLogger(SMSMessageGatewayTwilioImpl.class);

    @Override
    public void send(String text, String phoneNumber) throws IntegrationException {
        // TODO Implement sending message
        LOGGER.info(MessageFormat.format("Sending {0} to {1}.", text, phoneNumber));
    }
}
