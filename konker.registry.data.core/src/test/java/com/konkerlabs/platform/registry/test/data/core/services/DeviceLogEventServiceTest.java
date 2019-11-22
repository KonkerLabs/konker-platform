package com.konkerlabs.platform.registry.test.data.core.services;

import com.konkerlabs.platform.registry.billing.repositories.TenantDailyUsageRepository;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.repositories.ApplicationRepository;
import com.konkerlabs.platform.registry.business.repositories.DeviceRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.repositories.events.api.EventRepository;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.config.EmailConfig;
import com.konkerlabs.platform.registry.config.EventStorageConfig;
import com.konkerlabs.platform.registry.config.PubServerConfig;
import com.konkerlabs.platform.registry.data.core.services.JedisTaskService;
import com.konkerlabs.platform.registry.data.core.services.api.DeviceLogEventService;
import com.konkerlabs.platform.registry.test.data.core.base.*;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.thymeleaf.spring4.SpringTemplateEngine;

import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.doNothing;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        MongoTestConfiguration.class,
        RedisTestConfiguration.class,
        BusinessTestConfiguration.class,
        PubServerConfig.class,
        EventStorageConfig.class,
        EmailConfig.class,
        DeviceLogEventServiceTest.DeviceLogEventServiceTestConfig.class
})
@UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/devices.json"})
public class DeviceLogEventServiceTest extends BusinessLayerTestSupport {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private DeviceLogEventService deviceEventService;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private JedisTaskService jedisTaskService;

    @Autowired
    @Qualifier("mongoEvents")
    private EventRepository eventRepository;

    private final String userDefinedDeviceGuid = "7d51c242-81db-11e6-a8c2-0746f010e945";
    private String guid = "71fc0d48-674a-4d62-b3e5-0216abca63af";
    private final String apiKey = "84399b2e-d99e-11e5-86bc-34238775bac9";
    private final String payload = "{\n" +
            "    \"ts\" : \"2016-03-03T18:15:00Z\",\n" +
            "    \"value\" : 31.0,\n" +
            "    \"command\" : {\n" +
            "      \"type\" : \"ButtonPressed\"\n" +
            "      },\n" +
            "    \"data\" : {\n" +
            "      \"channels\" : [\n" +
            "        { \"name\" : \"channel_0\" }\n" +
            "      ]\n" +
            "    },\n" +
            "    \"time\" : 123\n" +
            "  }";
    private final String payloadLatLonInvalid = "{\n" +
            "    \"ts\" : \"2016-03-03T18:15:00Z\",\n" +
            "    \"value\" : 31.0,\n" +
            "    \"command\" : {\n" +
            "      \"type\" : \"ButtonPressed\"\n" +
            "      },\n" +
            "    \"data\" : {\n" +
            "      \"channels\" : [\n" +
            "        { \"name\" : \"channel_0\" }\n" +
            "      ]\n" +
            "    },\n" +
            "    \"time\" : 123,\n" +
            "	 \"_lat\" : 1234.56757777,\n" +
            "	 \"_lon\" : -46.6910183\n" +
            "  }";
    private final String payloadHdopInvalid = "{\n" +
            "    \"ts\" : \"2016-03-03T18:15:00Z\",\n" +
            "    \"value\" : 31.0,\n" +
            "    \"command\" : {\n" +
            "      \"type\" : \"ButtonPressed\"\n" +
            "      },\n" +
            "    \"data\" : {\n" +
            "      \"channels\" : [\n" +
            "        { \"name\" : \"channel_0\" }\n" +
            "      ]\n" +
            "    },\n" +
            "    \"time\" : 123,\n" +
            "	 \"_lat\" : -23.5746571,\n" +
            "	 \"_lon\" : -46.6910183,\n" +
            "	 \"_hdop\" : \"abc\"\n" +
            "  }";
    private final String payloadElevInvalid = "{\n" +
            "    \"ts\" : \"2016-03-03T18:15:00Z\",\n" +
            "    \"value\" : 31.0,\n" +
            "    \"command\" : {\n" +
            "      \"type\" : \"ButtonPressed\"\n" +
            "      },\n" +
            "    \"data\" : {\n" +
            "      \"channels\" : [\n" +
            "        { \"name\" : \"channel_0\" }\n" +
            "      ]\n" +
            "    },\n" +
            "    \"time\" : 123,\n" +
            "	 \"_lat\" : -23.5746571,\n" +
            "	 \"_lon\" : -46.6910183,\n" +
            "	 \"_hdop\" : 10,\n" +
            "	 \"_elev\" : \"abc\"\n" +
            "  }";
    private final String payloadValidGeo = "{\n" +
            "    \"ts\" : \"2016-03-03T18:15:00Z\",\n" +
            "    \"value\" : 31.0,\n" +
            "    \"command\" : {\n" +
            "      \"type\" : \"ButtonPressed\"\n" +
            "      },\n" +
            "    \"data\" : {\n" +
            "      \"channels\" : [\n" +
            "        { \"name\" : \"channel_0\" }\n" +
            "      ]\n" +
            "    },\n" +
            "    \"time\" : 123,\n" +
            "	 \"_lat\" : -23.5746571,\n" +
            "	 \"_lon\" : -46.6910183,\n" +
            "	 \"_hdop\" : 10,\n" +
            "	 \"_elev\" : 3.66\n" +
            "  }";
    private final String channel = "data";
    private String topic = MessageFormat.format("iot/{0}/{1}", apiKey, channel);
    private Event event;
    private Device device;
    private Tenant tenant;
    private Application application;
    private Instant firstEventTimestamp;
    private Instant lastEventTimestamp;

    @Before
    public void setUp() {
        firstEventTimestamp = Instant.ofEpochMilli(1474562670340L);
        lastEventTimestamp = Instant.ofEpochMilli(1474562674450L);

        tenant = tenantRepository.findByDomainName("konker");
        application = applicationRepository.findByTenantAndName(tenant.getId(), "konker");
        device = deviceRepository.findByTenantAndGuid(tenant.getId(), userDefinedDeviceGuid);
        event = Event.builder()
                .incoming(
                        Event.EventActor.builder()
                                .channel(channel)
                                .deviceGuid(device.getGuid())
                                .tenantDomain(tenant.getDomainName())
                                .applicationName(application.getName())
                                .build()
                )
                .ingestedTimestamp(Instant.now())
                .payload(payload).build();
    }

    @Test
    public void shouldRaiseAnExceptionIfDeviceIsNull() {
        ServiceResponse<Event> response = deviceEventService.logIncomingEvent(null, event);

        MatcherAssert.assertThat(response, ServiceResponseMatchers.hasErrorMessage(DeviceEventService.Validations.DEVICE_NULL.getCode()));
    }

    @Test
    public void shouldRaiseAnExceptionIfEventIsNull() {
        ServiceResponse<Event> response = deviceEventService.logIncomingEvent(device, null);

        MatcherAssert.assertThat(response,ServiceResponseMatchers.hasErrorMessage(DeviceEventService.Validations.EVENT_NULL.getCode()));
    }

    @Test
    public void shouldRaiseAnExceptionIfPayloadIsNull() {
        event.setPayload(null);

        ServiceResponse<Event> response = deviceEventService.logIncomingEvent(device, event);

        MatcherAssert.assertThat(response,ServiceResponseMatchers.hasErrorMessage(DeviceEventService.Validations.EVENT_PAYLOAD_NULL.getCode()));
    }

    @Test
    public void shouldRaiseAnExceptionIfPayloadIsEmpty() {
        event.setPayload("");

        ServiceResponse<Event> response = deviceEventService.logIncomingEvent(device, event);

        MatcherAssert.assertThat(response,ServiceResponseMatchers.hasErrorMessage(DeviceEventService.Validations.EVENT_PAYLOAD_NULL.getCode()));
    }

    @Test
    public void shouldLogFirstDeviceEvent() throws Exception {

        doNothing().when(jedisTaskService).registerLastEventTimestamp(event);

        deviceEventService.logIncomingEvent(device, event);

        Event last = eventRepository.findIncomingBy(tenant,application,device.getGuid(), null, channel, event.getCreationTimestamp().minusSeconds(1L), null, false, 1).get(0);

        assertThat(last, notNullValue());

        long gap = Duration.between(last.getCreationTimestamp(), Instant.now()).abs().getSeconds();

        assertThat(gap, not(greaterThan(60L)));

    }
    
    @Test
    public void shouldLogDeviceEventWithInvalidLatLon() throws Exception {
    	event.setPayload(payloadLatLonInvalid);
    	
        doNothing().when(jedisTaskService).registerLastEventTimestamp(event);
        deviceEventService.logIncomingEvent(device, event);
        Event last = eventRepository.findIncomingBy(tenant,application,device.getGuid(), null, channel, event.getCreationTimestamp().minusSeconds(1L), null, false, 1).get(0);

        assertThat(last, notNullValue());
        assertThat(last.getGeolocation(), nullValue());
       
        long gap = Duration.between(last.getCreationTimestamp(), Instant.now()).abs().getSeconds();
        assertThat(gap, not(greaterThan(60L)));

    }
    
    @Test
    public void shouldLogDeviceEventWithInvalidHdop() throws Exception {
    	event.setPayload(payloadHdopInvalid);
    	
        doNothing().when(jedisTaskService).registerLastEventTimestamp(event);
        deviceEventService.logIncomingEvent(device, event);
        Event last = eventRepository.findIncomingBy(tenant,application,device.getGuid(), null, channel, event.getCreationTimestamp().minusSeconds(1L), null, false, 1).get(0);

        assertThat(last, notNullValue());
        assertThat(last.getGeolocation().getHdop(), nullValue());
       
        long gap = Duration.between(last.getCreationTimestamp(), Instant.now()).abs().getSeconds();
        assertThat(gap, not(greaterThan(60L)));

    }
    
    @Test
    public void shouldLogDeviceEventWithInvalidElev() throws Exception {
    	event.setPayload(payloadElevInvalid);
    	
        doNothing().when(jedisTaskService).registerLastEventTimestamp(event);
        deviceEventService.logIncomingEvent(device, event);
        Event last = eventRepository.findIncomingBy(tenant,application,device.getGuid(), null, channel, event.getCreationTimestamp().minusSeconds(1L), null, false, 1).get(0);

        assertThat(last, notNullValue());
        assertThat(last.getGeolocation().getElev(), nullValue());
       
        long gap = Duration.between(last.getCreationTimestamp(), Instant.now()).abs().getSeconds();
        assertThat(gap, not(greaterThan(60L)));

    }
    
    @Test
    public void shouldLogDeviceEventWithValidGeo() throws Exception {
    	event.setPayload(payloadValidGeo);
    	
        doNothing().when(jedisTaskService).registerLastEventTimestamp(event);
        deviceEventService.logIncomingEvent(device, event);
        Event last = eventRepository.findIncomingBy(tenant,application,device.getGuid(), null, channel, event.getCreationTimestamp().minusSeconds(1L), null, false, 1).get(0);

        assertThat(last, notNullValue());
        assertThat(last.getGeolocation(), notNullValue());
        assertThat(last.getGeolocation().getLat(), notNullValue());
        assertThat(last.getGeolocation().getLon(), notNullValue());
        assertThat(last.getGeolocation().getHdop(), notNullValue());
        assertThat(last.getGeolocation().getElev(), notNullValue());
       
        long gap = Duration.between(last.getCreationTimestamp(), Instant.now()).abs().getSeconds();
        assertThat(gap, not(greaterThan(60L)));

    }

    @Configuration
    static class DeviceLogEventServiceTestConfig {

        @Bean
        public JedisTaskService jedisTaskService() {
            return Mockito.mock(JedisTaskService.class);
        }

        @Bean
    	public TenantDailyUsageRepository tenantDailyUsageRepository() {
    		return Mockito.mock(TenantDailyUsageRepository.class);
    	}
    	
    	@Bean
    	public JavaMailSender javaMailSender() {
    		return Mockito.mock(JavaMailSender.class);
    	}
    	
    	@Bean
    	public SpringTemplateEngine springTemplateEngine() {
    		return Mockito.mock(SpringTemplateEngine.class);
    	}
    }

}