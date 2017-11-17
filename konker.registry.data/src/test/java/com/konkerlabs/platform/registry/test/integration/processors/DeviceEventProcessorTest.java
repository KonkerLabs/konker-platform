package com.konkerlabs.platform.registry.test.integration.processors;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import com.konkerlabs.platform.registry.config.EventStorageConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.data.config.RedisConfig;
import com.konkerlabs.platform.registry.data.services.api.DeviceLogEventService;
import com.konkerlabs.platform.registry.data.services.routes.api.EventRouteExecutor;
import com.konkerlabs.platform.registry.integration.processors.DeviceEventProcessor;
import com.konkerlabs.platform.registry.test.data.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.data.base.IntegrationTestConfiguration;
import com.konkerlabs.platform.registry.test.data.base.MongoTestConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        MongoTestConfiguration.class,
        BusinessTestConfiguration.class,
        IntegrationTestConfiguration.class,
        DeviceEventProcessorTest.BusinessLayerConfiguration.class,
        RedisConfig.class,
        EventStorageConfig.class
})
public class DeviceEventProcessorTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private String sourceApiKey = "84399b2e-d99e-11e5-86bc-34238775bac9";
    private String originalPayload = "LEDSwitch";
    private String incomingChannel = "command";

    private Event event;
    private Event eventNewTimestamp;
    private Event eventOldTimestamp;
    private Device device;
    private ServiceResponse<Event> enrichmentResponse;
    private ServiceResponse<List<Event>> eventResponse;

    @Autowired
    private DeviceEventProcessor subject;
    @Autowired
    private DeviceEventService deviceEventService;
    @Autowired
    private DeviceLogEventService deviceLogEventService;
    @Autowired
    private EventRouteExecutor eventRouteExecutor;
    @Autowired
    private DeviceRegisterService deviceRegisterService;

    private Instant firstEventTimestamp;
    private Instant secondEventTimestamp;

    @Before
    public void setUp() throws Exception {
        firstEventTimestamp = Instant.ofEpochMilli(1474562670340L);
        secondEventTimestamp = Instant.ofEpochMilli(1474562672395L);

        event = Event.builder()
                .incoming(
                        Event.EventActor.builder()
                                .channel(incomingChannel)
                                .deviceGuid("device_guid")
                                .deviceId("device_id")
                                .tenantDomain("tenantDomain")
                                .applicationName("applicationName")
                                .build()

                )
                .payload(originalPayload)
                .build();

        device = spy(Device.builder()
                .tenant(
                        Tenant.builder()
                                .domainName("tenantDomain")
                                .name("tenantName")
                                .build()
                        )
                .application(
                        Application.builder()
                                .name("applicationName")
                                .friendlyName("applicationName")
                                .build()
                        )
                .apiKey(sourceApiKey)
                .id("id")
                .guid("device_guid")
                .deviceId("device_id")
                .active(true)
                .name("device_name").build());

        enrichmentResponse = spy(ServiceResponseBuilder.<Event>ok()
                .withResult(event)
                .build());

        eventOldTimestamp = Event.builder()
                .incoming(
                        Event.EventActor.builder()
                                .channel(incomingChannel)
                                .deviceGuid("device_guid").build()
                )
                .payload(originalPayload)
                .creationTimestamp(firstEventTimestamp)
                .build();

        eventNewTimestamp = Event.builder()
                .incoming(
                        Event.EventActor.builder()
                                .channel(incomingChannel)
                                .deviceGuid("device_guid").build()
                )
                .payload(originalPayload)
                .creationTimestamp(secondEventTimestamp)
                .build();

        eventResponse = spy(ServiceResponseBuilder.<List<Event>>ok()
                .withResult(Arrays.asList(eventNewTimestamp, eventOldTimestamp))
                .build());
    }

    @After
    public void tearDown() throws Exception {
        reset(deviceEventService, eventRouteExecutor, deviceRegisterService, deviceLogEventService);
    }

    @Test
    public void shouldRaiseAnExceptionIfDeviceApiKeyIsUnknown() throws Exception {
        thrown.expect(BusinessException.class);
        thrown.expectMessage(DeviceEventProcessor.Messages.APIKEY_MISSING.getCode());

        subject.process(null, incomingChannel, originalPayload);
    }

    @Test
    public void shouldRaiseAnExceptionIfDeviceDoesNotExist() throws Exception {
        thrown.expect(BusinessException.class);
        thrown.expectMessage(DeviceEventProcessor.Messages.DEVICE_NOT_FOUND.getCode());

        subject.process(sourceApiKey, incomingChannel, originalPayload);
    }

    @Test
    public void shouldRaiseAnExceptionIfEventChannelIsUnknown() throws Exception {
        thrown.expect(BusinessException.class);
        thrown.expectMessage(DeviceEventProcessor.Messages.CHANNEL_MISSING.getCode());

        subject.process(sourceApiKey, null, originalPayload);
    }

    @Test
    public void shouldNotFireRouteExecutionIfIncomingDeviceIsDisabled() throws Exception {
        device.setActive(false);
        when(deviceRegisterService.findByApiKey(sourceApiKey)).thenReturn(device);

        subject.process(sourceApiKey, incomingChannel, originalPayload);

        verify(eventRouteExecutor, never()).execute(any(Event.class), any(Device.class));
    }

    @Test
    public void shouldFireRouteExecution() throws Exception {
        Instant timestamp = Instant.now();
        event.setCreationTimestamp(timestamp);
        event.setIngestedTimestamp(timestamp);

        when(deviceRegisterService.findByApiKey(sourceApiKey)).thenReturn(device);

        when(deviceLogEventService.logIncomingEvent(eq(device), eq(event))).thenReturn(
                ServiceResponseBuilder.<Event>ok().withResult(event).build()
        );

        subject.process(sourceApiKey, incomingChannel, originalPayload, timestamp);

        verify(eventRouteExecutor, times(1)).execute(any(Event.class), any(Device.class));
        verify(deviceLogEventService, times(1)).logIncomingEvent(any(Device.class), any(Event.class));
    }

    @Configuration
    static class BusinessLayerConfiguration {
        @Bean
        public DeviceEventService deviceEventService() {
            return mock(DeviceEventService.class);
        }

        @Bean
        public EventRouteExecutor eventRouteExecutor() {
            return mock(EventRouteExecutor.class);
        }

        @Bean
        public DeviceRegisterService deviceRegisterService() {
            return mock(DeviceRegisterService.class);
        }

        @Bean
        public DeviceLogEventService deviceLogEventService() {
            return mock(DeviceLogEventService.class);
        }
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