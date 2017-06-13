package com.konkerlabs.platform.registry.test.data.services;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.doNothing;

import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;

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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.repositories.ApplicationRepository;
import com.konkerlabs.platform.registry.business.repositories.DeviceRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.repositories.events.EventRepository;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.config.EventStorageConfig;
import com.konkerlabs.platform.registry.config.PubServerConfig;
import com.konkerlabs.platform.registry.data.services.JedisTaskService;
import com.konkerlabs.platform.registry.data.services.api.DeviceLogEventService;
import com.konkerlabs.platform.registry.test.data.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.data.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.data.base.MongoTestConfiguration;
import com.konkerlabs.platform.registry.test.data.base.RedisTestConfiguration;
import com.konkerlabs.platform.registry.test.data.base.ServiceResponseMatchers;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        MongoTestConfiguration.class,
        RedisTestConfiguration.class,
        BusinessTestConfiguration.class,
        BusinessTestConfiguration.class,
        PubServerConfig.class,
        EventStorageConfig.class,
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

    private String userDefinedDeviceGuid = "7d51c242-81db-11e6-a8c2-0746f010e945";
    private String guid = "71fc0d48-674a-4d62-b3e5-0216abca63af";
    private String apiKey = "84399b2e-d99e-11e5-86bc-34238775bac9";
    private String payload = "{\n" +
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
    private String channel = "data";
    private String topic = MessageFormat.format("iot/{0}/{1}", apiKey, channel);
    private Event event;
    private Device device;
    private Tenant tenant;
    private Application application;
    private Instant firstEventTimestamp;
    private Instant lastEventTimestamp;

    @Before
    public void setUp() throws Exception {
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
                ).payload(payload).build();
    }

    @Test
    public void shouldRaiseAnExceptionIfDeviceIsNull() throws Exception {
        ServiceResponse<Event> response = deviceEventService.logIncomingEvent(null, event);

        assertThat(response,ServiceResponseMatchers.hasErrorMessage(DeviceEventService.Validations.DEVICE_NULL.getCode()));
    }

    @Test
    public void shouldRaiseAnExceptionIfEventIsNull() throws Exception {
        ServiceResponse<Event> response = deviceEventService.logIncomingEvent(device, null);

        assertThat(response,ServiceResponseMatchers.hasErrorMessage(DeviceEventService.Validations.EVENT_NULL.getCode()));
    }

    @Test
    public void shouldRaiseAnExceptionIfPayloadIsNull() throws Exception {
        event.setPayload(null);

        ServiceResponse<Event> response = deviceEventService.logIncomingEvent(device, event);

        assertThat(response,ServiceResponseMatchers.hasErrorMessage(DeviceEventService.Validations.EVENT_PAYLOAD_NULL.getCode()));
    }

    @Test
    public void shouldRaiseAnExceptionIfPayloadIsEmpty() throws Exception {
        event.setPayload("");

        ServiceResponse<Event> response = deviceEventService.logIncomingEvent(device, event);

        assertThat(response,ServiceResponseMatchers.hasErrorMessage(DeviceEventService.Validations.EVENT_PAYLOAD_NULL.getCode()));
    }

    @Test
    public void shouldLogFirstDeviceEvent() throws Exception {

        doNothing().when(jedisTaskService).registerLastEventTimestamp(event);

        deviceEventService.logIncomingEvent(device, event);

        Event last = eventRepository.findIncomingBy(tenant,application,device.getGuid(),channel,event.getTimestamp().minusSeconds(1l), null, false, 1).get(0);

        assertThat(last, notNullValue());

        long gap = Duration.between(last.getTimestamp(), Instant.now()).abs().getSeconds();

        assertThat(gap, not(greaterThan(60L)));

    }

    @Configuration
    static class DeviceLogEventServiceTestConfig {

        @Bean
        @SuppressWarnings("unchecked")
        public JedisTaskService jedisTaskService() {
            return Mockito.mock(JedisTaskService.class);
        }

    }

}