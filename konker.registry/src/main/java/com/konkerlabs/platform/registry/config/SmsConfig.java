package com.konkerlabs.platform.registry.config;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.konkerlabs.platform.registry.integration.gateways.SMSMessageGateway;
import com.konkerlabs.platform.registry.integration.gateways.SMSMessageGatewayTwilioImpl;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

@Configuration
public class SmsConfig {

    private static final Config smsServiceConfig = ConfigFactory.load().getConfig("sms");

    @Bean(name = "smsRestTemplate")
    public RestTemplate smsRestTemplate() {
        return new RestTemplate();
    }

    @Bean
    public SMSMessageGateway smsMessageGateway() throws URISyntaxException {
        SMSMessageGatewayTwilioImpl smsMessageGateway = new SMSMessageGatewayTwilioImpl();
        smsMessageGateway.setApiUri(new URI(smsServiceConfig.getString("uri")));
        smsMessageGateway.setUsername(smsServiceConfig.getString("username"));
        smsMessageGateway.setPassword(smsServiceConfig.getString("password"));
        smsMessageGateway.setFromPhoneNumber(smsServiceConfig.getString("from"));
        smsMessageGateway.setRestTemplate(this.smsRestTemplate());

        return smsMessageGateway;
    }

}
