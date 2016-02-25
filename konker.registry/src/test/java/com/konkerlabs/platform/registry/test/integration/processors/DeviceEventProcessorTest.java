package com.konkerlabs.platform.registry.test.integration.processors;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.rules.api.EventRuleExecutor;
import com.konkerlabs.platform.registry.integration.processors.DeviceEventProcessor;
import com.konkerlabs.platform.registry.test.base.IntegrationLayerTestContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;
import java.text.MessageFormat;

import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
    IntegrationLayerTestContext.class,
    DeviceEventProcessorTest.BusinessLayerConfiguration.class
})
public class DeviceEventProcessorTest {

    private static final String DEVICE_TOPIC_TEMPLATE = "iot/{0}/{1}";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private String sourceApiKey = "84399b2e-d99e-11e5-86bc-34238775bac9";
    private String originalPayload = "LEDSwitch";
    private String incomingChannel = "command";
    private String topic = MessageFormat.format(DEVICE_TOPIC_TEMPLATE, sourceApiKey, incomingChannel);

    private Event event;
    @Autowired
    private DeviceEventProcessor subject;
    @Autowired
    private DeviceEventService deviceEventService;
    @Autowired
    private EventRuleExecutor eventRuleExecutor;
    @Autowired
    private DeviceRegisterService deviceRegisterService;
    private Device device;

    @Before
    public void setUp() throws Exception {
        event = Event.builder()
            .channel(incomingChannel)
            .payload(originalPayload)
            .build();

        device = spy(Device.builder()
            .tenant(
                Tenant.builder()
                    .domainName("tenantDomain")
                    .name("tenantName")
                    .build()
            )
            .apiKey(originalPayload)
            .id("id")
            .deviceId("device_id")
            .active(true)
            .name("device_name").build());
    }

    @After
    public void tearDown() throws Exception {
        reset(deviceEventService,eventRuleExecutor,deviceRegisterService);
    }

    @Test
    public void shouldRaiseAnExceptionIfDeviceApiKeyIsUnknown() throws Exception {
        //Device API Key is expected to be found on second level
        topic = "konker";

        thrown.expect(BusinessException.class);
        thrown.expectMessage("Device API Key cannot be retrieved");

        subject.process(topic, originalPayload);
    }
    @Test
    public void shouldRaiseAnExceptionIfDeviceDoesNotExist() throws Exception {
        thrown.expect(BusinessException.class);
        thrown.expectMessage("Incoming device does not exist");

        subject.process(topic, originalPayload);
    }

    @Test
    public void shouldRaiseAnExceptionIfEventChannelIsUnknown() throws Exception {
        //Event incoming channel is expected to be found on third level
        topic = "konker/device/";

        thrown.expect(BusinessException.class);
        thrown.expectMessage("Event incoming channel cannot be retrieved");

        subject.process(topic, originalPayload);
    }

    @Test
    public void shouldLogIncomingEvent() throws Exception {
        when(deviceRegisterService.findByApiKey(sourceApiKey)).thenReturn(device);

        subject.process(topic, originalPayload);

        verify(deviceEventService).logEvent(device, event);
    }

    @Test
    public void shouldForwardIncomingMessageToDestinationDevice() throws Exception {
        when(deviceRegisterService.findByApiKey(sourceApiKey)).thenReturn(device);

        ResultCaptor<URI> returnCaptor = new ResultCaptor<URI>();

        doAnswer(returnCaptor).when(device).toURI();
        subject.process(topic, originalPayload);

        verify(eventRuleExecutor).execute(eq(event),same(returnCaptor.getResult()));
    }

    @Test
    public void shouldNotLogAnyEventIfIncomingDeviceIsDisabled() throws Exception {
        device.setActive(false);
        when(deviceRegisterService.findByApiKey(sourceApiKey)).thenReturn(device);

        subject.process(topic, originalPayload);

        verify(deviceEventService,never()).logEvent(any(Device.class),any(Event.class));
    }

    @Test
    public void shouldNotFireRuleExecutionIfIncomingDeviceIsDisabled() throws Exception {
        device.setActive(false);
        when(deviceRegisterService.findByApiKey(sourceApiKey)).thenReturn(device);

        subject.process(topic, originalPayload);

        verify(eventRuleExecutor,never()).execute(any(Event.class),any(URI.class));
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
        @Bean
        public DeviceRegisterService deviceRegisterService() { return mock(DeviceRegisterService.class); }
    }

    static class ResultCaptor<T> implements Answer {
        private T result = null;
        public T getResult() {
            return result;
        }

        @Override
        public T answer(InvocationOnMock invocationOnMock) throws Throwable {
            result = (T) invocationOnMock.callRealMethod();
            return result;
        }
    }
}