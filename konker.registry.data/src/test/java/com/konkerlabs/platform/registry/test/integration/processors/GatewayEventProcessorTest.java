package com.konkerlabs.platform.registry.test.integration.processors;

import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.config.EventStorageConfig;
import com.konkerlabs.platform.registry.data.core.config.RedisConfig;
import com.konkerlabs.platform.registry.data.core.integration.converters.DefaultJsonConverter;
import com.konkerlabs.platform.registry.data.core.integration.converters.MessagePackJsonConverter;
import com.konkerlabs.platform.registry.data.core.integration.gateway.RabbitGateway;
import com.konkerlabs.platform.registry.data.core.services.api.DeviceLogEventService;
import com.konkerlabs.platform.registry.data.core.services.routes.api.EventRouteExecutor;
import com.konkerlabs.platform.registry.integration.processors.GatewayEventProcessor;
import com.konkerlabs.platform.registry.test.data.base.BusinessDataTestConfiguration;
import com.konkerlabs.platform.registry.test.data.base.IntegrationTestConfiguration;
import com.konkerlabs.platform.registry.test.data.base.MongoDataTestConfiguration;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        MongoDataTestConfiguration.class,
        BusinessDataTestConfiguration.class,
        IntegrationTestConfiguration.class,
        GatewayEventProcessorTest.BusinessLayerConfiguration.class,
        RedisConfig.class,
        EventStorageConfig.class
})
public class GatewayEventProcessorTest {

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

    @Autowired
    private GatewayEventProcessor subject;
    @Autowired
    private DeviceEventService deviceEventService;
    @Autowired
    private DeviceLogEventService deviceLogEventService;
    @Autowired
    private EventRouteExecutor eventRouteExecutor;
    @Autowired
    private RabbitGateway rabbitGateway;
    @Autowired
    private DeviceRegisterService deviceRegisterService;
    @Autowired
    private JsonParsingService jsonParsingService;

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
        payloadMap1.put("imei", "CurrentSensor");
        payloadMap1.put("canal", "in");
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
        payloadMap2.put("imei", "TempSensor");
        payloadMap2.put("canal", "temp");
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
        reset(deviceEventService, eventRouteExecutor, deviceRegisterService, deviceLogEventService, rabbitGateway);
    }

    @Test
    public void shouldRaiseAnExceptionNoDeviceProcessGateway() throws Exception {

        when(jsonParsingService.toListMap(listJson)).thenReturn(devicesEvent);
        when(deviceRegisterService.findByDeviceId(gateway.getTenant(), gateway.getApplication(), "CurrentSensor"))
                .thenReturn(ServiceResponseBuilder.<Device>error().build());
        when(deviceRegisterService.findByDeviceId(gateway.getTenant(), gateway.getApplication(), "TempSensor"))
                .thenReturn(ServiceResponseBuilder.<Device>error().build());
        when(deviceLogEventService.logIncomingEvent(eq(device), any()))
                .thenReturn(ServiceResponseBuilder.<Event>ok().withResult(event).build());

        subject.process(gateway, listJson);

        verify(eventRouteExecutor, times(0)).execute(any(Event.class), any(Device.class));
        verify(deviceLogEventService, times(0)).logIncomingEvent(any(Device.class), any(Event.class));
    }

    @Test
    public void shouldRaiseAnExceptionDiferentLocationProcessGateway() throws Exception {
        when(jsonParsingService.toListMap(listJson)).thenReturn(devicesEvent);
        when(deviceRegisterService.findByDeviceId(gateway.getTenant(), gateway.getApplication(), "CurrentSensor"))
                .thenReturn(ServiceResponseBuilder.<Device>ok().withResult(device).build());
        when(deviceRegisterService.findByDeviceId(gateway.getTenant(), gateway.getApplication(), "TempSensor"))
                .thenReturn(ServiceResponseBuilder.<Device>ok().withResult(device).build());
        when(deviceLogEventService.logIncomingEvent(eq(device), any()))
                .thenReturn(ServiceResponseBuilder.<Event>ok().withResult(event).build());

        subject.process(gateway, listJson);

        verify(eventRouteExecutor, times(0)).execute(any(Event.class), any(Device.class));
        verify(deviceLogEventService, times(0)).logIncomingEvent(any(Device.class), any(Event.class));
    }

    @SuppressWarnings("unchecked")
	@Test
    public void shouldProcessGatewayEvent() throws Exception {
    	device.setLocation(gateway.getLocation());

    	when(jsonParsingService.toListMap(listJson)).thenReturn(devicesEvent);
    	when(jsonParsingService.toJsonString((Map<String, Object>) devicesEvent.get(0).get("payload"))).thenReturn("{ "+
    			" 	\"_lon\": -46.6910183, "+
    			"	\"_lat\": -23.5746571,  "+
    			"	\"_hdop\": 10,  "+
    			"	\"_elev\": 3.66,  "+
    			"	\"_ts\": \"1510847419000\", "+
    			"	\"volts\": 12  "+
    			"	}");
    	when(jsonParsingService.toJsonString((Map<String, Object>) devicesEvent.get(1).get("payload"))).thenReturn(" { "+
    			"	\"_lon\": -46.6910183, "+
    			"	\"_lat\": -23.5746571,  "+
    			"	\"_hdop\": 10,  "+
    			"	\"_elev\": 3.66,  "+
    			"	\"_ts\": \"1510847419000\", "+
    			"	\"temperature\": 27  "+
    			"	}");
    	when(deviceRegisterService.findByDeviceId(gateway.getTenant(), gateway.getApplication(), "CurrentSensor"))
    		.thenReturn(ServiceResponseBuilder.<Device>ok().withResult(device).build());
    	when(deviceRegisterService.findByDeviceId(gateway.getTenant(), gateway.getApplication(), "TempSensor"))
			.thenReturn(ServiceResponseBuilder.<Device>ok().withResult(device).build());
    	when(deviceLogEventService.logIncomingEvent(eq(device), any()))
    		.thenReturn(ServiceResponseBuilder.<Event>ok().withResult(event).build());

    	subject.process(gateway, listJson);

        verify(rabbitGateway, times(2)).queueEventDataPub(anyString(), anyString(), any(Long.class), any());
    }

    @Test
    public void shouldRaiseAnExceptionNoDeviceProcessGatewayData() throws Exception {
        when(jsonParsingService.toListMap(listDataJson)).thenReturn(devicesDataEvent);
        when(deviceRegisterService.findByDeviceId(gateway.getTenant(), gateway.getApplication(), "CurrentSensor"))
                .thenReturn(ServiceResponseBuilder.<Device>error().build());
        when(deviceRegisterService
                .register(
                        gateway.getTenant(),
                        gateway.getApplication(),
                        Device.builder()
                                .deviceId("CurrentSensor")
                                .name("CurrentSensor")
                                .location(gateway.getLocation())
                                .active(true)
                                .build()))
                .thenReturn(ServiceResponseBuilder.<Device>error().build());
        when(deviceRegisterService.findByDeviceId(gateway.getTenant(), gateway.getApplication(), "TempSensor"))
                .thenReturn(ServiceResponseBuilder.<Device>error().build());
        when(deviceRegisterService
                .register(
                        any(Tenant.class),
                        any(Application.class),
                        any(Device.class)))
                .thenReturn(ServiceResponseBuilder.<Device>error().build());
        when(deviceLogEventService.logIncomingEvent(eq(device), any()))
                .thenReturn(ServiceResponseBuilder.<Event>ok().withResult(event).build());

        subject.process(gateway, listDataJson, "imei", "name", "canal");

        verify(eventRouteExecutor, times(0)).execute(any(Event.class), any(Device.class));
        verify(deviceLogEventService, times(0)).logIncomingEvent(any(Device.class), any(Event.class));
    }

    @Test
    public void shouldRaiseAnExceptionDiferentLocationProcessGatewayData() throws Exception {
        when(jsonParsingService.toListMap(listDataJson)).thenReturn(devicesDataEvent);
        when(deviceRegisterService.findByDeviceId(gateway.getTenant(), gateway.getApplication(), "CurrentSensor"))
                .thenReturn(ServiceResponseBuilder.<Device>error().build());
        when(deviceRegisterService.findByDeviceId(gateway.getTenant(), gateway.getApplication(), "TempSensor"))
                .thenReturn(ServiceResponseBuilder.<Device>error().build());
        when(deviceRegisterService
                .register(
                        gateway.getTenant(),
                        gateway.getApplication(),
                        Device.builder()
                                .deviceId("CurrentSensor")
                                .name("CurrentSensor")
                                .location(gateway.getLocation())
                                .active(true)
                                .build()))
                .thenReturn(ServiceResponseBuilder.<Device>error().build());
        when(deviceRegisterService
                .register(
                        any(Tenant.class),
                        any(Application.class),
                        any(Device.class)))
                .thenReturn(ServiceResponseBuilder.<Device>error().build());
        when(deviceLogEventService.logIncomingEvent(eq(device), any()))
                .thenReturn(ServiceResponseBuilder.<Event>ok().withResult(event).build());

        subject.process(gateway, listDataJson, "imei", "name", "canal");

        verify(eventRouteExecutor, times(0)).execute(any(Event.class), any(Device.class));
        verify(deviceLogEventService, times(0)).logIncomingEvent(any(Device.class), any(Event.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldProcessGatewayEventData() throws Exception {
        device.setLocation(gateway.getLocation());

        when(jsonParsingService.toListMap(listDataJson)).thenReturn(devicesDataEvent);
        when(jsonParsingService.toJsonString((Map<String, Object>) devicesEvent.get(0).get("payload"))).thenReturn("{ "+
                " 	\"_lon\": -46.6910183, "+
                "	\"_lat\": -23.5746571,  "+
                "	\"_hdop\": 10,  "+
                "	\"_elev\": 3.66,  "+
                "	\"_ts\": \"1510847419000\", "+
                "	\"volts\": 12  "+
                "	}");
        when(jsonParsingService.toJsonString((Map<String, Object>) devicesEvent.get(1).get("payload"))).thenReturn(" { "+
                "	\"_lon\": -46.6910183, "+
                "	\"_lat\": -23.5746571,  "+
                "	\"_hdop\": 10,  "+
                "	\"_elev\": 3.66,  "+
                "	\"_ts\": \"1510847419000\", "+
                "	\"temperature\": 27  "+
                "	}");
        when(deviceRegisterService.findByDeviceId(gateway.getTenant(), gateway.getApplication(), "CurrentSensor"))
                .thenReturn(ServiceResponseBuilder.<Device>ok().withResult(device).build());
        when(deviceRegisterService.findByDeviceId(gateway.getTenant(), gateway.getApplication(), "TempSensor"))
                .thenReturn(ServiceResponseBuilder.<Device>ok().withResult(device).build());
        when(deviceLogEventService.logIncomingEvent(eq(device), any()))
                .thenReturn(ServiceResponseBuilder.<Event>ok().withResult(event).build());

        subject.process(gateway, listDataJson, "imei", "name", "canal");

        verify(rabbitGateway, times(2)).queueEventDataPub(anyString(), anyString(), any(Long.class), any());
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
        public RabbitGateway rabbitGateway() {
            return mock(RabbitGateway.class);
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
