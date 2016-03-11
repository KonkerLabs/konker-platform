package com.konkerlabs.platform.registry.integration.gateways;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.springframework.beans.factory.SmartFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Component
public class SmsMessageGatewayFactory implements SmartFactoryBean<SMSMessageGateway> {

    private static final Config smsServiceConfig = ConfigFactory.load().getConfig("sms");

    @Autowired
    private RestTemplate restTemplate;

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
        smsMessageGateway.setApiUri(new URI(smsServiceConfig.getString("uri")));
        smsMessageGateway.setUsername(smsServiceConfig.getString("username"));
        smsMessageGateway.setPassword(smsServiceConfig.getString("password"));
        smsMessageGateway.setFromPhoneNumber(smsServiceConfig.getString("from"));
        smsMessageGateway.setRestTemplate(restTemplate);

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
