package com.konkerlabs.platform.registry.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import lombok.Data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.ExecutorChannel;
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
import java.util.concurrent.Executor;

@Configuration
@EnableIntegration
@ComponentScan(basePackages = "com.konkerlabs.platform.registry.integration")
@IntegrationComponentScan(basePackages = "com.konkerlabs.platform.registry.integration")
@Data
public class MqttConfig {

    private String[] subcribeUris = {"tcp://dev-server:1883"};
    private String[] subcribeTopics = {"pub/+/+"};
    private String subcribeUsername = "user";
    private String subcribePassword = "pass";
    
    private String[] publishUris = {"tcp://dev-server:1883"};
    private String publishUsername = "user";
    private String publishPassword = "pass";

    //MQTT stuff

    @Autowired
    private Executor executor;
    
    public MqttConfig() {
    	if (ConfigFactory.load().hasPath("mqtt") && ConfigFactory.load().getConfig("mqtt").hasPath("subcribe")) {
    		Config inboundBrokerConfig = ConfigFactory.load().getConfig("mqtt").getConfig("subcribe");
    		setSubcribeUris(inboundBrokerConfig.getStringList("uris").toArray(new String[] {}));
    		setSubcribeTopics(inboundBrokerConfig.getStringList("topics").toArray(new String[] {}));
    		setSubcribeUsername(inboundBrokerConfig.getString("username"));
    		setSubcribePassword(inboundBrokerConfig.getString("password"));
    	}

    	if (ConfigFactory.load().hasPath("mqtt") && ConfigFactory.load().getConfig("mqtt").hasPath("publish")) {
    		Config outboundBrokerConfig = ConfigFactory.load().getConfig("mqtt").getConfig("publish");
    		setPublishUris(outboundBrokerConfig.getStringList("uris").toArray(new String[] {}));
    		setPublishUsername(outboundBrokerConfig.getString("username"));
    		setPublishPassword(outboundBrokerConfig.getString("password"));
    	}
    }

    @Bean(name = "konkerMqttInputChannel")
    public MessageChannel inputChannel() {
        return new ExecutorChannel(executor);
    }

    @Bean(name = "konkerMqttOutputChannel")
    public MessageChannel outputChannel() {
        return new DirectChannel();
    }

    @Bean
    public DefaultMqttPahoClientFactory mqttInboudClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        factory.setServerURIs(getSubcribeUris());
        factory.setUserName(getSubcribeUsername());
        factory.setPassword(getSubcribePassword());
        return factory;
    }

    @Bean
    public DefaultMqttPahoClientFactory mqttOutboundClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        factory.setServerURIs(getPublishUris());
        factory.setUserName(getPublishUsername());
        factory.setPassword(getPublishPassword());
        return factory;
    }

    public String[] topicPatternList() {
        return getSubcribeTopics();
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
