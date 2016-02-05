package com.konkerlabs.platform.registry.test.integration.processors;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import com.konkerlabs.platform.registry.business.services.rules.api.EventRuleExecutor;
import com.konkerlabs.platform.registry.integration.processors.DeviceEventProcessor;
import com.konkerlabs.platform.registry.test.base.IntegrationLayerTestContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;
import java.text.MessageFormat;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
    IntegrationLayerTestContext.class,
    DeviceEventProcessorTest.BusinessLayerConfiguration.class
})
public class DeviceEventProcessorTest {

    private static final String DEVICE_TOPIC_TEMPLATE = "konker/device/{0}/{1}";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private String sourceDeviceId = "0000000000000004";
    private String destinationDeviceId = "0000000000000005";
    private String originalPayload = MessageFormat.format("{0}LEDSwitch",destinationDeviceId);
    private String topic = MessageFormat.format(DEVICE_TOPIC_TEMPLATE, sourceDeviceId, "command");
    private String destinationTopic = MessageFormat.format(DEVICE_TOPIC_TEMPLATE, destinationDeviceId, "in");

    private Event event;
    @Autowired
    private DeviceEventProcessor subject;
    @Autowired
    private DeviceEventService deviceEventService;
    @Autowired
    private EventRuleExecutor eventRuleExecutor;

    @Before
    public void setUp() throws Exception {
        event = Event.builder()
            .channel(topic)
            .payload(originalPayload)
            .build();
    }

    @After
    public void tearDown() throws Exception {
        reset(deviceEventService,eventRuleExecutor);
    }

    @Test
    public void shouldRaiseAnExceptionIfDeviceIdIsUnknown() throws Exception {
        //Device ID is expected to be found on third level
        topic = "konker/device";

        thrown.expect(BusinessException.class);
        thrown.expectMessage("Device ID cannot be retrieved");

        subject.process(topic, originalPayload);
    }

    @Test
    public void shouldLogIncomingEvent() throws Exception {
        subject.process(topic, originalPayload);

        verify(deviceEventService).logEvent(event, sourceDeviceId);
    }

    @Test
    public void shouldForwardIncomingMessageToDestinationDevice() throws Exception {
        subject.process(topic, originalPayload);


        verify(eventRuleExecutor).execute(event,new URI("device://" + sourceDeviceId));
    }

    @Configuration
    static class BusinessLayerConfiguration {
        @Bean
        public DeviceEventService deviceEventService() {
            return mock(DeviceEventService.class);
        }
        @Bean
        public EventRuleExecutor eventRuleExecutor() {
            return mock(EventRuleExecutor.class);
        }
    }
}