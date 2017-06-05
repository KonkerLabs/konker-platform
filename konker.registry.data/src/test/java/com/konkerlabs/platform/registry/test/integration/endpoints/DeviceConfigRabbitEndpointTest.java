package com.konkerlabs.platform.registry.test.integration.endpoints;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.services.api.DeviceConfigSetupService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.integration.endpoints.DeviceConfigRabbitEndpoint;
import com.konkerlabs.platform.registry.integration.gateways.RabbitGateway;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {
        DeviceConfigRabbitEndpointTest.DeviceConfigRabbitEndpointTestConfig.class
})
public class DeviceConfigRabbitEndpointTest {

    private DeviceConfigRabbitEndpoint deviceConfigRabbitEndpoint;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitGateway rabbitGateway;

    @Autowired
    private DeviceConfigSetupService deviceConfigSetupService;

    @Autowired
    private DeviceRegisterService deviceRegisterService;

    @Before
    public void setUp() throws Exception {
        deviceConfigRabbitEndpoint = new DeviceConfigRabbitEndpoint();
        deviceConfigRabbitEndpoint.setDeviceConfigSetupService(deviceConfigSetupService);
        deviceConfigRabbitEndpoint.setDeviceRegisterService(deviceRegisterService);
        deviceConfigRabbitEndpoint.setRabbitGateway(rabbitGateway);
    }

    @After
    public void tearDown() {
        Mockito.reset(rabbitTemplate);
    }

    @Test
    public void shouldReceiveOnConfigPub() throws Exception {

        final String apiKey = "jV5bnJWK";

        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setHeader("apiKey", apiKey);

        Device device = Device.builder().build();

        when(deviceRegisterService.findByApiKey(apiKey)).thenReturn(device);

        when(deviceConfigSetupService
                    .findByModelAndLocation(
                            device.getTenant(),
                            device.getApplication(),
                            device.getDeviceModel(),
                            device.getLocation())).thenReturn(ServiceResponseBuilder.<String>ok().withResult("{ 'a: 4 }").build());

        Message message = new Message("{ 'a: 4 }".getBytes("UTF-8"), messageProperties);
        deviceConfigRabbitEndpoint.onConfigPub(message);

        verify(rabbitTemplate, times(1)).convertAndSend("mgmt.config.sub", message);

    }

    @Test
    public void shouldReceiveOnConfigPubWithoutConfig() throws Exception {

        final String apiKey = "jV5bnJWK";

        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setHeader("apiKey", apiKey);

        Device device = Device.builder().build();

        when(deviceRegisterService.findByApiKey(apiKey)).thenReturn(device);

        when(deviceConfigSetupService
                    .findByModelAndLocation(
                            device.getTenant(),
                            device.getApplication(),
                            device.getDeviceModel(),
                            device.getLocation())).thenReturn(ServiceResponseBuilder.<String>ok().withResult(null).build());

        Message message = new Message("{ }".getBytes("UTF-8"), messageProperties);
        deviceConfigRabbitEndpoint.onConfigPub(message);

        verify(rabbitTemplate, times(1)).convertAndSend("mgmt.config.sub", message);

    }

    @Configuration
    @ComponentScan(basePackages = {
            "com.konkerlabs.platform.registry.integration.gateways"
    },lazyInit = true)
    static class DeviceConfigRabbitEndpointTestConfig {

        @Bean
        public DeviceConfigSetupService deviceConfigSetupService() {
            return Mockito.mock(DeviceConfigSetupService.class);
        }

        @Bean
        public DeviceRegisterService deviceRegisterService() {
            return Mockito.mock(DeviceRegisterService.class);
        }

        @Bean
        public RabbitTemplate rabbitTemplate() {
            return Mockito.mock(RabbitTemplate.class);
        }

    }

}
