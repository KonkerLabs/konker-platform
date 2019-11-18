package com.konkerlabs.platform.registry.test.data.core.integration.processors;

import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.config.EventStorageConfig;
import com.konkerlabs.platform.registry.data.core.config.RedisConfig;
import com.konkerlabs.platform.registry.data.core.integration.converters.DefaultJsonConverter;
import com.konkerlabs.platform.registry.data.core.integration.converters.MessagePackJsonConverter;
import com.konkerlabs.platform.registry.data.core.integration.processors.DeviceEventProcessor;
import com.konkerlabs.platform.registry.data.core.services.api.DeviceLogEventService;
import com.konkerlabs.platform.registry.data.core.services.routes.api.EventRouteExecutor;
import com.konkerlabs.platform.registry.test.data.core.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.data.core.base.IntegrationTestConfiguration;
import com.konkerlabs.platform.registry.test.data.core.base.MongoTestConfiguration;
import com.konkerlabs.platform.utilities.parsers.json.JsonParsingService;
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

import java.time.Instant;
import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

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

    private final String sourceApiKey = "84399b2e-d99e-11e5-86bc-34238775bac9";
    private final String originalPayload = "LEDSwitch";
    private final String incomingChannel = "command";
    private String listJson;
    private String listDataJson;

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
    @Autowired
    private JsonParsingService jsonParsingService;
    @Autowired
    private DefaultJsonConverter defaultJsonConverter;
    @Autowired
    private MessagePackJsonConverter messagePackJsonConverter;

    private Instant firstEventTimestamp;
    private Instant secondEventTimestamp;
    private List<Map<String, Object>> devicesEvent;
    private List<Map<String, Object>> devicesDataEvent;
    private Gateway gateway;

	@Before
    public void setUp() {
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
                                .locationGuid("84399b2e-d99e-11e5-86bc-34238775abcd")
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
                .location(Location.builder().guid("84399b2e-d99e-11e5-86bc-34238775abcd").build())
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
        
        listJson = "[ "+
            	"{ "+
    			" \"deviceId\": \"CurrentSensor\", "+
    			" \"channel\": \"in\", "+
    			" \"payload\": { "+
    			" 	\"_lon\": -46.6910183, "+ 
    			"	\"_lat\": -23.5746571,  "+
    			"	\"_hdop\": 10,  "+
    			"	\"_elev\": 3.66,  "+
    			"	\"_ts\": \"1510847419000\", "+ 
    			"	\"volts\": 12  "+
    			"	} "+
                '}' +
    			", { "+
    			" \"deviceId\": \"TempSensor\", "+
    			" \"channel\": \"temp\", "+
    			" \"payload\": { "+
    			"	\"_lon\": -46.6910183, "+ 
    			"	\"_lat\": -23.5746571,  "+
    			"	\"_hdop\": 10,  "+
    			"	\"_elev\": 3.66,  "+
    			"	\"_ts\": \"1510847419000\", "+ 
    			"	\"temperature\": 27  "+
    			"	} "+
    			"} "+
                ']';

        listDataJson = "[ "+
                "{ "+
                " \"imei\": \"CurrentSensor\", "+
                " \"canal\": \"in\", "+
                " \"_lon\": -46.6910183, "+
                " \"_lat\": -23.5746571,  "+
                " \"_hdop\": 10,  "+
                " \"_elev\": 3.66,  "+
                " \"_ts\": \"1510847419000\", "+
                " \"volts\": 12  "+
                '}' +
                ", { "+
                " \"imei\": \"TempSensor\", "+
                " \"canal\": \"temp\", "+
                " \"_lon\": -46.6910183, "+
                " \"_lat\": -23.5746571,  "+
                " \"_hdop\": 10,  "+
                " \"_elev\": 3.66,  "+
                " \"_ts\": \"1510847419000\", "+
                " \"temperature\": 27  "+
                "} "+
                ']';
        
        devicesEvent = new ArrayList<>();
        Map<String, Object> map1 = new LinkedHashMap<>();
        map1.put("deviceId", "CurrentSensor");
        map1.put("channel", "in");
        Map<String, Object> payloadMap1 = new LinkedHashMap<>();
        payloadMap1.put("_lon", -46.6910183);
        payloadMap1.put("_lat", -23.5746571);
        payloadMap1.put("_hdop", 10);
        payloadMap1.put("_elev", 3.66);
        payloadMap1.put("volts", 12);
        map1.put("payload", payloadMap1);
        
        Map<String, Object> map2 = new LinkedHashMap<>();
        map2.put("deviceId", "TempSensor");
        map2.put("channel", "temp");
        Map<String, Object> payloadMap2 = new LinkedHashMap<>();
        payloadMap2.put("_lon", -46.6910183);
        payloadMap2.put("_lat", -23.5746571);
        payloadMap2.put("_hdop", 10);
        payloadMap2.put("_elev", 3.66);
        payloadMap2.put("temperature", 27);
        map2.put("payload", payloadMap2);
		devicesEvent.add(map1);
        devicesEvent.add(map2);

        devicesDataEvent = new ArrayList<>();
        Map<String, Object> map3 = new LinkedHashMap<>();
        map3.put("imei", "CurrentSensor");
        map3.put("canal", "in");
        map3.put("_lon", -46.6910183);
        map3.put("_lat", -23.5746571);
        map3.put("_hdop", 10);
        map3.put("_elev", 3.66);
        map3.put("volts", 12);

        Map<String, Object> map4 = new LinkedHashMap<>();
        map4.put("imei", "TempSensor");
        map4.put("canal", "temp");
        map4.put("_lon", -46.6910183);
        map4.put("_lat", -23.5746571);
        map4.put("_hdop", 10);
        map4.put("_elev", 3.66);
        map4.put("temperature", 27);
        devicesDataEvent.add(map3);
        devicesDataEvent.add(map4);
        
        gateway = Gateway.builder()
        		.active(true)
        		.application(Application.builder().name("default").build())
        		.description("GW smart")
        		.guid("7d51c242-81db-11e6-a8c2-0746f010e945")
        		.id("gateway1")
        		.location(Location.builder().defaultLocation(true).id("BR").name("default").build())
        		.name("Gateway 1")
        		.tenant(Tenant.builder().id("commonTenant").domainName("common").build())
        		.build();
    }

    @After
    public void tearDown() {
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
        
        when(deviceRegisterService.findByApiKey(sourceApiKey)).thenReturn(device);

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
    
    @Test
    public void shouldRaiseAnExceptionNoChannelProcessDevice() throws Exception {
        thrown.expect(BusinessException.class);
        thrown.expectMessage(DeviceEventProcessor.Messages.CHANNEL_MISSING.getCode());
        
        when(deviceRegisterService.findByApiKey(sourceApiKey)).thenReturn(device);

        Instant ingestedTimestamp = Instant.now();
		subject.process(device, null, originalPayload, ingestedTimestamp, ingestedTimestamp);
    }
    
    @Test
    public void shouldProcessDevice() throws Exception {
    	when(deviceLogEventService.logIncomingEvent(eq(device), any()))
    		.thenReturn(ServiceResponseBuilder.<Event>ok().withResult(event).build());
    	
    	Instant ingestedTimestamp = Instant.now();
    	subject.process(device, incomingChannel, originalPayload, ingestedTimestamp, ingestedTimestamp);
    	
    	verify(eventRouteExecutor, times(1)).execute(any(Event.class), any(Device.class));
        verify(deviceLogEventService, times(1)).logIncomingEvent(any(Device.class), any(Event.class));
    }

    @Test
    public void shouldProcessDeviceWithMessagePack() throws Exception {
	    device.setDeviceModel(
	            DeviceModel
                        .builder()
                        .contentType(DeviceModel.ContentType.APPLICATION_MSGPACK)
                        .build()
        );

        byte[] messagePackBytes = {(byte) -109, (byte) 1, (byte) 2, (byte) 3};

        when(deviceRegisterService.findByApiKey(sourceApiKey)).thenReturn(device);
        when(messagePackJsonConverter.toJson(messagePackBytes)).thenReturn(ServiceResponseBuilder.<String>ok().build());
        when(deviceLogEventService.logIncomingEvent(eq(device), any()))
                .thenReturn(ServiceResponseBuilder.<Event>ok().withResult(event).build());

        subject.process(sourceApiKey, incomingChannel, messagePackBytes, Instant.now());

        verify(messagePackJsonConverter, times(1)).toJson(messagePackBytes);
        verify(deviceLogEventService, times(1)).logIncomingEvent(any(Device.class), any(Event.class));

	}


    @Test
    public void shouldProcessDeviceWithJson() throws Exception {
        device.setDeviceModel(
                DeviceModel
                        .builder()
                        .contentType(DeviceModel.ContentType.APPLICATION_JSON)
                        .build()
        );

        byte[] messagePackBytes = "[1,2]".getBytes();

        when(deviceRegisterService.findByApiKey(sourceApiKey)).thenReturn(device);
        when(defaultJsonConverter.toJson(messagePackBytes)).thenReturn(ServiceResponseBuilder.<String>ok().build());
        when(deviceLogEventService.logIncomingEvent(eq(device), any()))
                .thenReturn(ServiceResponseBuilder.<Event>ok().withResult(event).build());

        subject.process(sourceApiKey, incomingChannel, messagePackBytes, Instant.now());

        verify(defaultJsonConverter, times(1)).toJson(messagePackBytes);
        verify(deviceLogEventService, times(1)).logIncomingEvent(any(Device.class), any(Event.class));

    }

    @Test
    public void shouldRouteBatteryToMgmtChannel() throws Exception {
        when(deviceLogEventService.logIncomingEvent(eq(device), any()))
                .thenReturn(ServiceResponseBuilder.<Event>ok().withResult(event).build());

        String payloadWithBattery = "{\"_battery\":50.0}";

        when(jsonParsingService.toFlatMap(payloadWithBattery))
                .thenReturn(new HashMap<String,JsonParsingService.JsonPathData>(){
                    {
                        put("_battery", JsonParsingService.JsonPathData
                                .builder()
                                .types(Collections.singletonList(JsonNodeType.NUMBER))
                                .value(18.0)
                                .build());
                    }
                });

        Instant ingestedTimestamp = Instant.now();
        subject.process(device, incomingChannel, payloadWithBattery, ingestedTimestamp, ingestedTimestamp);

        verify(eventRouteExecutor, times(2)).execute(any(Event.class), any(Device.class));
        verify(deviceLogEventService, times(2)).logIncomingEvent(any(Device.class), any(Event.class));
    }

    @Test
    public void shouldProcessDeviceDeactivated() throws Exception {
    	when(deviceLogEventService.logIncomingEvent(eq(device), any()))
    		.thenReturn(ServiceResponseBuilder.<Event>ok().withResult(event).build());
    	
    	Instant ingestedTimestamp = Instant.now();
    	device.setActive(false);
    	subject.process(device, incomingChannel, originalPayload, ingestedTimestamp, ingestedTimestamp);
    	
    	verify(eventRouteExecutor, times(0)).execute(any(Event.class), any(Device.class));
        verify(deviceLogEventService, times(0)).logIncomingEvent(any(Device.class), any(Event.class));
    }
    
    @Test
    public void shouldRaiseExcetionInvalidJsonProcessDevice() throws Exception {
    	thrown.expect(BusinessException.class);
        thrown.expectMessage(DeviceEventProcessor.Messages.INVALID_PAYLOAD.getCode());
        
    	when(deviceLogEventService.logIncomingEvent(eq(device), any()))
    		.thenReturn(ServiceResponseBuilder.<Event>error().build());
    	
    	Instant ingestedTimestamp = Instant.now();
    	subject.process(device, incomingChannel, originalPayload, ingestedTimestamp, ingestedTimestamp);
    	
    	verify(eventRouteExecutor, times(0)).execute(any(Event.class), any(Device.class));
        verify(deviceLogEventService, times(1)).logIncomingEvent(any(Device.class), any(Event.class));
    }
    
    @Test
    public void shouldFireApplicationRouteExecution() throws Exception {
    	Application application = Application.builder()
    			.name("fake")
    			.tenant(Tenant.builder().domainName("fakedomain").build())
    			.build();
        subject.process(application, originalPayload);

        verify(eventRouteExecutor, times(1)).execute(any(Event.class), any(Device.class));
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
        
        @Bean
        public JsonParsingService jsonParsingService() {
            return mock(JsonParsingService.class);
        }

        @Bean
        public MessagePackJsonConverter messagePackJsonConverter() {
            return mock(MessagePackJsonConverter.class);
        }

        @Bean
        public DefaultJsonConverter defaultJsonConverter() {
            return mock(DefaultJsonConverter.class);
        }

    }

    static class ResultCaptor<T> implements Answer {
        private T result;

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