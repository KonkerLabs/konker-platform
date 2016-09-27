package com.konkerlabs.platform.registry.test.business.services;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.DeviceRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.repositories.events.EventRepository;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.NewServiceResponse;
import com.konkerlabs.platform.registry.test.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;
import com.konkerlabs.platform.registry.test.base.RedisTestConfiguration;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static com.konkerlabs.platform.registry.test.base.matchers.NewServiceResponseMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        MongoTestConfiguration.class,
        BusinessTestConfiguration.class,
        RedisTestConfiguration.class
})
@UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json"})
public class DeviceEventServiceTest extends BusinessLayerTestSupport {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private DeviceEventService deviceEventService;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

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
    private Instant firstEventTimestamp;
    private Instant lastEventTimestamp;

    @Before
    public void setUp() throws Exception {
        firstEventTimestamp = Instant.ofEpochMilli(1474562670340L);
        lastEventTimestamp = Instant.ofEpochMilli(1474562674450L);

        tenant = tenantRepository.findByDomainName("konker");
        device = deviceRepository.findByTenantAndGuid(tenant.getId(), userDefinedDeviceGuid);
        event = Event.builder().channel(topic).payload(payload).deviceGuid(device.getGuid()).build();
    }

    @Test
    public void shouldRaiseAnExceptionIfDeviceIsNull() throws Exception {
        thrown.expect(BusinessException.class);
        thrown.expectMessage("Device cannot be null");

        deviceEventService.logEvent(null, channel, event);
    }

    @Test
    public void shouldRaiseAnExceptionIfEventIsNull() throws Exception {
        thrown.expect(BusinessException.class);
        thrown.expectMessage("Event cannot be null");

        deviceEventService.logEvent(device, channel, null);
    }

    @Test
    public void shouldRaiseAnExceptionIfPayloadIsNull() throws Exception {
        event.setPayload(null);

        thrown.expect(BusinessException.class);
        thrown.expectMessage("Event payload cannot be null or empty");

        deviceEventService.logEvent(device, channel, event);
    }

    @Test
    public void shouldRaiseAnExceptionIfPayloadIsEmpty() throws Exception {
        event.setPayload("");

        thrown.expect(BusinessException.class);
        thrown.expectMessage("Event payload cannot be null or empty");

        deviceEventService.logEvent(device, channel, event);
    }

    @Test
    public void shouldLogFirstDeviceEvent() throws Exception {
        event.setChannel("otherChannel");
        deviceEventService.logEvent(device, channel, event);

        Event last = eventRepository.findBy(tenant, device.getGuid(), event.getTimestamp(), null, 1).get(0);

        assertThat(last, notNullValue());

        long gap = Duration.between(last.getTimestamp(), Instant.now()).abs().getSeconds();

        assertThat(gap, not(greaterThan(60L)));
    }

    @Test
    public void shouldReturnAnErrorMessageIfTenantIsNullWhenFindingBy() throws Exception {

        NewServiceResponse<List<Event>> serviceResponse = deviceEventService.findEventsBy(
                null,
                device.getId(),
                firstEventTimestamp,
                null,
                null
        );

        assertThat(serviceResponse, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
    }

    @Test
    public void shouldReturnAnErrorMessageIfDeviceIdIsNullWhenFindingBy() throws Exception {

        NewServiceResponse<List<Event>> serviceResponse = deviceEventService.findEventsBy(
                tenant,
                null,
                firstEventTimestamp,
                null,
                null
        );

        assertThat(serviceResponse, hasErrorMessage(DeviceRegisterService.Validations.DEVICE_GUID_NULL.getCode()));
    }

    @Test
    public void shouldReturnAnErrorMessageIfStartInstantIsNullAndLimitIsNullWhenFindingBy() throws Exception {

        NewServiceResponse<List<Event>> serviceResponse = deviceEventService.findEventsBy(
                tenant,
                device.getId(),
                null,
                null,
                null
        );

        assertThat(serviceResponse, hasErrorMessage(DeviceEventService.Validations.LIMIT_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json", "/fixtures/deviceEvents.json"})
    public void shouldFindAllRequestEvents() throws Exception {
        NewServiceResponse<List<Event>> serviceResponse = deviceEventService.findEventsBy(
                tenant,
                device.getGuid(),
                firstEventTimestamp,
                null,
                null
        );

        assertThat(serviceResponse.getResult(), notNullValue());
        assertThat(serviceResponse.getResult(), hasSize(3));

        assertThat(serviceResponse.getResult().get(0).getTimestamp().toEpochMilli(),
                equalTo(lastEventTimestamp.toEpochMilli()));
    }


    @Test
    public void shouldPublishOnRedisWhenEventsAreLogged() throws Exception {
        Jedis jedis = (Jedis) redisTemplate.getConnectionFactory().getConnection().getNativeConnection();
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        CompletableFuture<String> completableFuture = new CompletableFuture<>();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Future<?> future = executorService.submit(() -> {
            jedis.subscribe(new JedisPubSub() {
                @Override
                public void onMessage(String channel, String message) {
                    completableFuture.complete(message);
                    unsubscribe();
                }
            }, device.getApiKey() + "." + channel);
        });
        countDownLatch.await(1, TimeUnit.SECONDS);
        try {
            future.get(3, TimeUnit.SECONDS);
        } catch (TimeoutException e){
            future.cancel(true);
        } finally {
            executorService.shutdown();
            deviceEventService.logEvent(device, channel, event);
            assertThat(device.getGuid(), equalTo(completableFuture.get()));
        }



    }
}