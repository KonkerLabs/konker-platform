package com.konkerlabs.platform.registry.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.gateway.GatewayProxyFactoryBean;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.AbstractMqttMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Configuration
@EnableIntegration
@ComponentScan(basePackages = "com.konkerlabs.platform.registry.integration")
@IntegrationComponentScan(basePackages = "com.konkerlabs.platform.registry.integration")
public class IntegrationConfig {

    private static final Config inboundBrokerConfig = ConfigFactory.load().getConfig("mqtt").getConfig("subcribe");
    private static final Config outboundBrokerConfig = ConfigFactory.load().getConfig("mqtt").getConfig("publish");

    //MQTT stuff

    @Bean(name = "konkerMqttInputChannel")
    public MessageChannel inputChannel() {
        return new DirectChannel();
    }

    @Bean(name = "konkerMqttOutputChannel")
    public MessageChannel outputChannel() {
        return new DirectChannel();
    }

    @Bean
    public DefaultMqttPahoClientFactory mqttInboudClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        factory.setServerURIs(inboundBrokerConfig.getStringList("uris").toArray(new String[] {}));
        factory.setUserName(inboundBrokerConfig.getString("username"));
        factory.setPassword(inboundBrokerConfig.getString("password"));
        return factory;
    }

    @Bean
    public DefaultMqttPahoClientFactory mqttOutboundClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        factory.setServerURIs(outboundBrokerConfig.getStringList("uris").toArray(new String[] {}));
        factory.setUserName(outboundBrokerConfig.getString("username"));
        factory.setPassword(outboundBrokerConfig.getString("password"));
        return factory;
    }

    public String[] topicPatternList() {
        return inboundBrokerConfig.getStringList("topics").toArray(new String[] {});
    }

    @Bean
    public AbstractMqttMessageDrivenChannelAdapter inbound() {
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(UUID.randomUUID().toString(),
                        mqttInboudClientFactory(),topicPatternList());
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1);
        adapter.setOutputChannel(inputChannel());
        return adapter;
    }

    @Bean
    @ServiceActivator(inputChannel = "konkerMqttOutputChannel")
    public MessageHandler mqttOutbound() {
        MqttPahoMessageHandler messageHandler =
                new MqttPahoMessageHandler(UUID.randomUUID().toString(), mqttOutboundClientFactory());
        messageHandler.setAsync(true);
        messageHandler.setCompletionTimeout(5000);
        messageHandler.setDefaultTopic("testTopic");
        return messageHandler;
    }

    @Bean
    public GatewayProxyFactoryBean gatewayProxyFactoryBean() {
        GatewayProxyFactoryBean factoryBean = new GatewayProxyFactoryBean();
        factoryBean.setDefaultRequestChannel(outputChannel());
        factoryBean.setDefaultRequestTimeout(1L);
        return factoryBean;
    }

    //HTTP stuff

    @Bean
    public RestTemplate enrichmentRestTemplate() {
        return new RestTemplate();
    }
}
