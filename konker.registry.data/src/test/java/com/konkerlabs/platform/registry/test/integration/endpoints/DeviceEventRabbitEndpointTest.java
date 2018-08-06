package com.konkerlabs.platform.registry.test.integration.endpoints;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Instant;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.konkerlabs.platform.registry.integration.endpoints.DeviceEventRabbitEndpoint;
import com.konkerlabs.platform.registry.integration.processors.DeviceEventProcessor;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {
        DeviceEventRabbitEndpointTest.DeviceConfigRabbitEndpointTestConfig.class
})
public class DeviceEventRabbitEndpointTest {

    private DeviceEventRabbitEndpoint deviceEventRabbitEndpoint;

    @Autowired
    private DeviceEventProcessor deviceEventProcessor;

    @Before
    public void setUp() {
        deviceEventRabbitEndpoint = new DeviceEventRabbitEndpoint(deviceEventProcessor);

    }

    @Test
    public void shouldReceiveOnConfigPub() throws Exception {

        final String apiKey = "jV5bnJWK";
        final String channel = "temp";
        final String payload = "{ 'a' : '52T' }";
        final Long epochMilli = 1490001001000L;
        Instant timestamp = Instant.ofEpochMilli(epochMilli);

        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setHeader("apiKey", apiKey);
        messageProperties.setHeader("channel", channel);
        messageProperties.setHeader("ts", epochMilli);

        Message message = new Message(payload.getBytes("UTF-8"), messageProperties);
        deviceEventRabbitEndpoint.onDataPub(message);

        verify(deviceEventProcessor, times(1)).process(apiKey, channel, payload.getBytes("UTF-8"), timestamp);

    }

    @Configuration
    static class DeviceConfigRabbitEndpointTestConfig {

        @Bean
        public DeviceEventProcessor deviceEventProcessor() {
            return Mockito.mock(DeviceEventProcessor.class);
        }

    }

}
