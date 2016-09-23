package com.konkerlabs.platform.registry.test.business.repositories;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.repositories.events.EventRepository;
import com.konkerlabs.platform.registry.business.repositories.events.EventRepositoryMongoImpl;
import com.konkerlabs.platform.registry.test.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.time.Instant;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        BusinessTestConfiguration.class,
        MongoTestConfiguration.class
})
@UsingDataSet(locations = {"/fixtures/tenants.json","/fixtures/devices.json"})
public class EventRepositoryMongoTest extends BusinessLayerTestSupport {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    @Qualifier("mongoEvents")
    private EventRepository eventRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

    private Tenant tenant;
    private String payload;
    private Event event;
    private DBObject persisted;

    private String deviceId;
    private Instant firstEventTimestamp;
    private Instant secondEventTimestamp;
    private Instant thirdEventTimestamp;

    @Before
    public void setUp() {
        firstEventTimestamp = Instant.ofEpochMilli(1474562670340L);
        secondEventTimestamp = Instant.ofEpochMilli(1474562672395L);
        thirdEventTimestamp = Instant.ofEpochMilli(1474562674450L);

        deviceId = "95c14b36ba2b43f1";
        tenant = tenantRepository.findByDomainName("konker");

        payload = "{\n" +
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

        tenant = tenantRepository.findByDomainName("konker");

        event = Event.builder()
                .channel("command")
                .deviceId(deviceId)
                .timestamp(firstEventTimestamp)
                .payload(payload).build();

        persisted = (DBObject) JSON.parse(payload);
        persisted.put("ts", firstEventTimestamp.toEpochMilli());
        persisted.put("deviceId", deviceId);
        persisted.put("tenantDomain", tenant.getDomainName());
    }

    @Test
    public void shouldRaiseAnExceptionIfTenantIsNull() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Tenant cannot be null");

        eventRepository.push(null,event);
    }

    @Test
    public void shouldRaiseAnExceptionIfTenantDoesNotExists() throws Exception {
        thrown.expect(BusinessException.class);
        thrown.expectMessage("Tenant does not exists");

        eventRepository.push(Tenant.builder().domainName("fake").build(),event);
    }

    @Test
    public void shouldRaiseAnExceptionIfEventIsNull() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Event cannot be null");

        eventRepository.push(tenant,null);
    }

    @Test
    public void shouldRaiseAnExceptionIfDeviceIdIsNull() throws Exception {
        event.setDeviceId(null);

        thrown.expect(BusinessException.class);
        thrown.expectMessage("Device ID cannot be null or empty");

        eventRepository.push(tenant,event);
    }

    @Test
    public void shouldRaiseAnExceptionIfDeviceIdIsEmpty() throws Exception {
        event.setDeviceId("");

        thrown.expect(BusinessException.class);
        thrown.expectMessage("Device ID cannot be null or empty");

        eventRepository.push(tenant,event);
    }

    @Test
    public void shouldRaiseAnExceptionIfDeviceDoesNotExists() throws Exception {
        event.setDeviceId("unknown_device");

        thrown.expect(BusinessException.class);
        thrown.expectMessage("Device does not exists");

        eventRepository.push(tenant,event);
    }

    @Test
    public void shouldRaiseAnExceptionIfEventTimestampIsNull() throws Exception {
        event.setTimestamp(null);

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Event timestamp cannot be null");

        eventRepository.push(tenant,event);
    }

    @Test
    public void shouldPushTheIncomingEvent() throws Exception {
        eventRepository.push(tenant,event);

        DBObject saved = mongoTemplate.findOne(
                Query.query(Criteria.where("deviceId").is(deviceId)
                        .andOperator(Criteria.where("ts").is(firstEventTimestamp.toEpochMilli()))),
                DBObject.class,
                EventRepositoryMongoImpl.EVENTS_COLLECTION_NAME
        );
        saved.removeField("_id");
        saved.removeField("_class");

        assertThat(saved,equalTo(persisted));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json","/fixtures/devices.json","/fixtures/deviceEvents.json"})
    public void shouldRetrieveLastTwoEventsByTenantAndDevice() throws Exception {
        List<Event> events = eventRepository.findBy(tenant,deviceId,
                firstEventTimestamp.plus(1,ChronoUnit.SECONDS),
                null,null);

        assertThat(events,notNullValue());
        assertThat(events,hasSize(2));

        assertThat(events.get(0).getTimestamp().toEpochMilli(),equalTo(secondEventTimestamp.toEpochMilli()));
        assertThat(events.get(1).getTimestamp().toEpochMilli(),equalTo(thirdEventTimestamp.toEpochMilli()));
    }

    @Test
    public void shouldRaiseAnExceptionIfTenantIsNullWhenFindingBy() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Tenant cannot be null");

        eventRepository.findBy(null,deviceId,firstEventTimestamp,null,null);
    }

    @Test
    public void shouldRaiseAnExceptionIfDeviceIdWhenFindingBy() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Device ID cannot be null or empty");

        eventRepository.findBy(tenant,null,firstEventTimestamp,null,null);
    }

    @Test
    public void shouldRaiseAnExceptionIfStartingOffsetIsNullWhenFindingBy() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Starting offset cannot be null");


        eventRepository.findBy(tenant,deviceId,null,null,null);
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json","/fixtures/devices.json","/fixtures/deviceEvents.json"})
    public void shouldRetrieveTheOnlyFirstEventByTenantAndDevice() throws Exception {
        List<Event> events = eventRepository.findBy(tenant,
                deviceId,
                firstEventTimestamp,
                secondEventTimestamp.minus(1, ChronoUnit.SECONDS),
                null);

        assertThat(events,notNullValue());
        assertThat(events,hasSize(1));

        assertThat(events.get(0).getTimestamp().toEpochMilli(),equalTo(firstEventTimestamp.toEpochMilli()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json","/fixtures/devices.json","/fixtures/deviceEvents.json"})
    public void shouldLimitResultsAccordingToLimitParameterWhenFindingBy() throws Exception {
        List<Event> events = eventRepository.findBy(tenant,
                deviceId,
                firstEventTimestamp,
                thirdEventTimestamp,
                1);

        assertThat(events,notNullValue());
        assertThat(events,hasSize(1));

        assertThat(events.get(0).getTimestamp().toEpochMilli(),equalTo(firstEventTimestamp.toEpochMilli()));
    }
}