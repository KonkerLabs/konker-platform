package com.konkerlabs.platform.registry.test.integration.endpoints;

import com.konkerlabs.platform.registry.integration.endpoints.DeviceEventMqttEndpoint;
import com.konkerlabs.platform.registry.integration.processors.DeviceEventProcessor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;

import java.text.MessageFormat;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class DeviceEventMqttEndpointTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    public DeviceEventMqttEndpoint subject;
    private Message<String> message;
    private DeviceEventProcessor processor;

    private String deviceId = "95c14b36ba2b43f1";
    private String channel = "data";
    private String payload = "message";
    private String topic = MessageFormat.format("iot/{0}/{1}",deviceId,channel);

    @Before
    public void setUp() throws Exception {
        processor = mock(DeviceEventProcessor.class);
        subject = new DeviceEventMqttEndpoint(processor);

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
    public void shouldDelegateEventToItsProcessor() throws Exception {
        subject.onEvent(message);

        verify(processor).process(deviceId,channel,payload);
    }
}