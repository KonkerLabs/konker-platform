package com.konkerlabs.platform.registry.test.integration.endpoints;

import com.konkerlabs.platform.registry.business.services.api.DeviceConfigSetupService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.integration.endpoints.DeviceConfigMqttEndpoint;
import com.konkerlabs.platform.registry.integration.endpoints.DeviceEventMqttEndpoint;
import com.konkerlabs.platform.registry.integration.gateways.MqttMessageGateway;
import com.konkerlabs.platform.registry.integration.processors.DeviceEventProcessor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.context.MessageSource;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;

import java.text.MessageFormat;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class DeviceConfigMqttEndpointTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    public DeviceConfigMqttEndpoint subject;
    private Message<String> message;
    private DeviceConfigSetupService deviceConfigSetupService;
    private DeviceRegisterService deviceRegisterService;
    private MessageSource messageSource;
    private MqttMessageGateway mqttMessageGateway;

    private String deviceId = "95c14b36ba2b43f1";
    private String channel = "data";
    private String payload = "message";
    private String topic = MessageFormat.format("mgmt/{0}/pub/cfg", deviceId);

    @Before
    public void setUp() throws Exception {
    	deviceConfigSetupService = mock(DeviceConfigSetupService.class);
    	deviceRegisterService = mock(DeviceRegisterService.class);
    	messageSource = mock(MessageSource.class);
    	mqttMessageGateway = mock(MqttMessageGateway.class);
        subject = new DeviceConfigMqttEndpoint(deviceConfigSetupService, 
        										deviceRegisterService, 
        										messageSource, 
        										mqttMessageGateway);

        message = MessageBuilder.withPayload(payload).setHeader(MqttHeaders.TOPIC,topic).build();
    }
    
    @Test
    public void shouldRaiseAnExceptionIfTopicIsNull() throws Exception {
        message = MessageBuilder.withPayload(payload).build();

        thrown.expect(MessagingException.class);
        thrown.expectMessage("Topic cannot be null or empty");

        subject.onEvent(message);
    }
    
    @Test
    public void shouldRaiseAnExceptionIfTopicIsEmpty() throws Exception {
        message = MessageBuilder.withPayload(payload).setHeader(MqttHeaders.TOPIC,"").build();

        thrown.expect(MessagingException.class);
        thrown.expectMessage("Topic cannot be null or empty");

        subject.onEvent(message);
    }
    
    @Test
    public void shouldRaiseAnExceptionIfDeviceNotExists() throws Exception {
        thrown.expect(MessagingException.class);
        thrown.expectMessage("Topic cannot be null or empty");

        subject.onEvent(message);
    }
    
    @Test
    public void shouldDelegateEventToItsProcessor() throws Exception {
        subject.onEvent(message);

//        verify(processor).process(deviceId,channel,payload);
    }
}