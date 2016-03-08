package com.konkerlabs.platform.registry.config;

import com.konkerlabs.platform.registry.integration.gateways.MqttMessageGateway;
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

import java.util.UUID;

@Configuration
@EnableIntegration
@ComponentScan(basePackages = "com.konkerlabs.platform.registry.integration")
@IntegrationComponentScan(basePackageClasses = MqttMessageGateway.class)
public class MqttConfig {

    private static final Config brokerConfig = ConfigFactory.load().getConfig("mqtt");

    @Bean(name = "konkerMqttInputChannel")
    public MessageChannel inputChannel() {
        return new DirectChannel();
    }

    @Bean(name = "konkerMqttOutputChannel")
    public MessageChannel outputChannel() {
        return new DirectChannel();
    }

    @Bean
    public DefaultMqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        factory.setServerURIs(brokerConfig.getStringList("uris").toArray(new String[] {}));
        factory.setUserName(brokerConfig.getString("username"));
        factory.setPassword(brokerConfig.getString("password"));
        return factory;
    }

    //TODO Make this pattern collection configurable via application.conf
    public String[] topicPatternList() {
        return new String[] {
            "iot/+/data",
            "iot/+/command",
            "iot/+/ack"
        };
    }

    @Bean
    public AbstractMqttMessageDrivenChannelAdapter inbound() {
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(UUID.randomUUID().toString(),
                        mqttClientFactory(),topicPatternList());
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
                new MqttPahoMessageHandler("konkerPublisher", mqttClientFactory());
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
}
