package com.konkerlabs.platform.registry.integration.gateways;

import java.net.URI;

import org.springframework.beans.factory.SmartFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.konkerlabs.platform.registry.config.SmsConfig;

@Component
public class SmsMessageGatewayFactory implements SmartFactoryBean<SMSMessageGateway> {

    @Autowired
    private SmsConfig smsConfig;

    @Autowired
    private HttpGateway httpGateway;

    @Override
    public boolean isPrototype() {
        return false;
    }

    @Override
    public boolean isEagerInit() {
        return false;
    }

    @Override
    public SMSMessageGateway getObject() throws Exception {
        SMSMessageGatewayTwilioImpl smsMessageGateway = new SMSMessageGatewayTwilioImpl();
        smsMessageGateway.setApiUri(new URI(smsConfig.getUri()));
        smsMessageGateway.setUsername(smsConfig.getUsername());
        smsMessageGateway.setPassword(smsConfig.getPassword());
        smsMessageGateway.setFromPhoneNumber(smsConfig.getFrom());
//        smsMessageGateway.setRestTemplate(restTemplate);
        smsMessageGateway.setHttpGateway(httpGateway);

        return smsMessageGateway;
    }

    @Override
    public Class<?> getObjectType() {
        return SMSMessageGateway.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
