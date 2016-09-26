package com.konkerlabs.platform.registry.test.integration.processors;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.*;
import com.konkerlabs.platform.registry.business.services.routes.api.EventRouteExecutor;
import com.konkerlabs.platform.registry.config.RedisConfig;
import com.konkerlabs.platform.registry.integration.processors.DeviceEventProcessor;
import com.konkerlabs.platform.registry.test.base.IntegrationLayerTestContext;
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
import org.springframework.web.context.request.async.DeferredResult;

import java.net.URI;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
    IntegrationLayerTestContext.class,
    DeviceEventProcessorTest.BusinessLayerConfiguration.class, RedisConfig.class
})
public class DeviceEventProcessorTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private String sourceApiKey = "84399b2e-d99e-11e5-86bc-34238775bac9";
    private String originalPayload = "LEDSwitch";
    private String incomingChannel = "command";

    private Event event;
    private Device device;
    private NewServiceResponse<Event> enrichmentResponse;
    private NewServiceResponse<List<Event>> eventResponse;

    @Autowired
    private DeviceEventProcessor subject;
    @Autowired
    private DeviceEventService deviceEventService;
    @Autowired
    private EventRouteExecutor eventRouteExecutor;
    @Autowired
    private DeviceRegisterService deviceRegisterService;
    @Autowired
    private EnrichmentExecutor enrichmentExecutor;

    @Before
    public void setUp() throws Exception {
        event = Event.builder()
            .channel(incomingChannel)
            .deviceGuid("device_guid")
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
            .guid("device_guid")
            .deviceId("device_id")
            .active(true)
            .name("device_name").build());

        enrichmentResponse = spy(ServiceResponseBuilder.<Event>ok()
                .withResult(event)
                .build());
        
        eventResponse = spy(ServiceResponseBuilder.<List<Event>>ok()
        		.withResult(Arrays.asList(event))
        		.build());
    }

    @After
    public void tearDown() throws Exception {
        reset(deviceEventService, eventRouteExecutor,deviceRegisterService);
    }

    @Test
    public void shouldRaiseAnExceptionIfDeviceApiKeyIsUnknown() throws Exception {
        thrown.expect(BusinessException.class);
        thrown.expectMessage(DeviceEventProcessor.Messages.APIKEY_MISSING.getCode());

        subject.process(null, incomingChannel,originalPayload);
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
    public void shouldLogIncomingEvent() throws Exception {
        when(deviceRegisterService.findByApiKey(sourceApiKey)).thenReturn(device);
        when(enrichmentExecutor.enrich(event, device)).thenReturn(enrichmentResponse);

        subject.process(sourceApiKey, incomingChannel, originalPayload);

        verify(deviceEventService).logEvent(device, incomingChannel, event);
    }

    @Test
    public void shouldForwardIncomingMessageToDestinationDevice() throws Exception {
        when(deviceRegisterService.findByApiKey(sourceApiKey)).thenReturn(device);
        when(enrichmentExecutor.enrich(event, device)).thenReturn(enrichmentResponse);

        ResultCaptor<URI> returnCaptor = new ResultCaptor<URI>();

        doAnswer(returnCaptor).when(device).toURI();
        subject.process(sourceApiKey, incomingChannel, originalPayload);

        verify(eventRouteExecutor).execute(eq(event),same(returnCaptor.getResult()));
    }

    @Test
    public void shouldNotLogAnyEventIfIncomingDeviceIsDisabled() throws Exception {
        device.setActive(false);
        when(deviceRegisterService.findByApiKey(sourceApiKey)).thenReturn(device);

        subject.process(sourceApiKey, incomingChannel, originalPayload);

        verify(deviceEventService,never()).logEvent(any(), any(), any());
    }

    @Test
    public void shouldNotFireRouteExecutionIfIncomingDeviceIsDisabled() throws Exception {
        device.setActive(false);
        when(deviceRegisterService.findByApiKey(sourceApiKey)).thenReturn(device);

        subject.process(sourceApiKey, incomingChannel, originalPayload);

        verify(eventRouteExecutor,never()).execute(any(Event.class),any(URI.class));
    }
    
    @Test
    public void shouldThrowABusinessExceptionIfDeviceApiKeyIsUnknown() throws Exception {
        thrown.expect(BusinessException.class);
        thrown.expectMessage(DeviceEventProcessor.Messages.APIKEY_MISSING.getCode());

        subject.process(null, incomingChannel, Instant.now(), new Long("30000"), new DeferredResult<>());
    }
    @Test
    public void shouldThrowABusinessExceptionIfDeviceDoesNotExist() throws Exception {
        thrown.expect(BusinessException.class);
        thrown.expectMessage(DeviceEventProcessor.Messages.DEVICE_NOT_FOUND.getCode());

        subject.process(sourceApiKey, incomingChannel, Instant.now(), new Long("30000"), new DeferredResult<>());
    }
    @Test
    public void shouldThrowABusinessExceptionIfEventChannelIsUnknown() throws Exception {
        thrown.expect(BusinessException.class);
        thrown.expectMessage(DeviceEventProcessor.Messages.CHANNEL_MISSING.getCode());

        subject.process(sourceApiKey, null, Instant.now(), new Long("30000"), new DeferredResult<>());
    }
    
    @Test
    public void shouldReturnEventList() throws Exception {
        when(deviceEventService.findEventsBy(device.getTenant(), device.getGuid(), 
        		Instant.now(), null, null)).thenReturn(eventResponse);

        subject.process(sourceApiKey, incomingChannel, Instant.now(), new Long("30000"), new DeferredResult<>());

        verify(deviceEventService).logEvent(device, incomingChannel, event);
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
        public EnrichmentExecutor enrichmentExecutor() {
            return mock(EnrichmentExecutor.class);
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