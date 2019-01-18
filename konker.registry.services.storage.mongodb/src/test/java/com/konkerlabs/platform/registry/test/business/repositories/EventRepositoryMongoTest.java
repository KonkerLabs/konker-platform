package com.konkerlabs.platform.registry.test.business.repositories;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.Supplier;

import com.konkerlabs.platform.registry.business.repositories.ApplicationRepository;
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

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Event.EventGeolocation;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.DeviceRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.repositories.events.EventRepositoryMongoImpl;
import com.konkerlabs.platform.registry.business.repositories.events.api.EventRepository;
import com.konkerlabs.platform.registry.test.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

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
    private ApplicationRepository applicationRepository;
    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    @Qualifier("mongoEvents")
    private EventRepository eventRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

    private Tenant tenant;
    private Application application;
    private Application applicationSmartff;
    private String incomingPayload;
    private Event incomingEvent;
    private Event outgoingEvent;
    private DBObject persisted;

    private String deviceGuid;
    private Instant firstEventTimestamp;
    private Instant secondEventTimestamp;
    private Instant thirdEventTimestamp;


    @Before
    public void setUp() {
        firstEventTimestamp = Instant.ofEpochMilli(1474562670340L);
        secondEventTimestamp = Instant.ofEpochMilli(1474562672395L);
        thirdEventTimestamp = Instant.ofEpochMilli(1474562674450L);

        deviceGuid = "7d51c242-81db-11e6-a8c2-0746f010e945";
        tenant = tenantRepository.findByDomainName("konker");
        application = applicationRepository.findByTenantAndName(tenant.getId(), "konker");
        applicationSmartff = applicationRepository.findByTenantAndName(tenant.getId(), "smartffkonker");

        incomingPayload = "{\n" +
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

        incomingEvent = Event.builder()
                .incoming(
                        Event.EventActor.builder()
                                .channel("command")
                                .deviceGuid(deviceGuid)
                                .tenantDomain(tenant.getDomainName())
                                .build())
                .geolocation(
                		EventGeolocation.builder()
                			.lat(new Double(-23.5746571))
                			.lon(new Double(-46.6910183))
                			.hdop(new Long(1))
                			.elev(new Double(3.66))
                			.build())
                .creationTimestamp(firstEventTimestamp)
                .ingestedTimestamp(secondEventTimestamp)
                .payload(incomingPayload).build();

        persisted = new BasicDBObject();
        persisted.put("ts", incomingEvent.getCreationTimestamp().toEpochMilli());
        persisted.put("ingestedTimestamp", incomingEvent.getIngestedTimestamp().toEpochMilli());
        persisted.put("incoming", ((Supplier<DBObject>) () -> {
            DBObject incoming = new BasicDBObject();
            incoming.put("deviceGuid",incomingEvent.getIncoming().getDeviceGuid());
            incoming.put("deviceId", incomingEvent.getIncoming().getDeviceId());
            incoming.put("tenantDomain",incomingEvent.getIncoming().getTenantDomain());
            incoming.put("applicationName",incomingEvent.getIncoming().getApplicationName());
            incoming.put("channel",incomingEvent.getIncoming().getChannel());
            return incoming;
        }).get());
        persisted.put("geolocation", ((Supplier<DBObject>) () -> {
        	DBObject geolocation = new BasicDBObject();
        	geolocation.put("lat", incomingEvent.getGeolocation().getLat());
        	geolocation.put("lon", incomingEvent.getGeolocation().getLon());
        	geolocation.put("hdop", incomingEvent.getGeolocation().getHdop());
        	geolocation.put("elev", incomingEvent.getGeolocation().getElev());
        	return geolocation;
        }).get());
        persisted.put("payload", incomingEvent.getPayload());
    }

    @Test
    public void shouldRaiseAnExceptionIfTenantIsNullWhenSavingAnIncomingEvent() throws Exception {
        thrown.expect(BusinessException.class);
        thrown.expectMessage(CommonValidations.TENANT_NULL.getCode());

        eventRepository.saveIncoming(null, null, incomingEvent);
    }

    @Test
    public void shouldRaiseAnExceptionIfTenantDoesNotExistsWhenSavingAnIncomingEvent() throws Exception {
        thrown.expect(BusinessException.class);
        thrown.expectMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode());

        eventRepository.saveIncoming(Tenant.builder().domainName("fake").build(),
                                     Application.builder().name("fake").build(),
                                     incomingEvent);
    }

    @Test
    public void shouldRaiseAnExceptionIfEventIsNullWhenSavingAnIncomingEvent() throws Exception {
        thrown.expect(BusinessException.class);
        thrown.expectMessage(CommonValidations.RECORD_NULL.getCode());

        eventRepository.saveIncoming(tenant, application, null);
    }

    @Test
    public void shouldRaiseAnExceptionIfEventIncomingActorIsNullWhenSavingAnIncomingEvent() throws Exception {
        incomingEvent.setIncoming(null);

        thrown.expect(BusinessException.class);
        thrown.expectMessage(EventRepository.Validations.EVENT_INCOMING_NULL.getCode());

        eventRepository.saveIncoming(tenant, application, incomingEvent);
    }

    @Test
    public void shouldRaiseAnExceptionIfDeviceIdIsNullWhenSavingAnIncomingEvent() throws Exception {
        incomingEvent.getIncoming().setDeviceGuid(null);

        thrown.expect(BusinessException.class);
        thrown.expectMessage(EventRepository.Validations.INCOMING_DEVICE_GUID_NULL.getCode());

        eventRepository.saveIncoming(tenant, application, incomingEvent);
    }

    @Test
    public void shouldRaiseAnExceptionIfDeviceIdIsEmptyWhenSavingAnIncomingEvent() throws Exception {
        incomingEvent.getIncoming().setDeviceGuid("");

        thrown.expect(BusinessException.class);
        thrown.expectMessage(EventRepository.Validations.INCOMING_DEVICE_GUID_NULL.getCode());

        eventRepository.saveIncoming(tenant, application, incomingEvent);
    }

    @Test
    public void shouldRaiseAnExceptionIfEventIncomingChannelIsNullWhenSavingAnIncomingEvent() throws Exception {
        incomingEvent.getIncoming().setChannel(null);

        thrown.expect(BusinessException.class);
        thrown.expectMessage(EventRepository.Validations.EVENT_INCOMING_CHANNEL_NULL.getCode());

        eventRepository.saveIncoming(tenant, application, incomingEvent);
    }

    @Test
    public void shouldRaiseAnExceptionIfEventIncomingChannelIsEmptyWhenSavingAnIncomingEvent() throws Exception {
        incomingEvent.getIncoming().setChannel("");

        thrown.expect(BusinessException.class);
        thrown.expectMessage(EventRepository.Validations.EVENT_INCOMING_CHANNEL_NULL.getCode());

        eventRepository.saveIncoming(tenant, application, incomingEvent);
    }

    @Test
    public void shouldRaiseAnExceptionIfDeviceDoesNotExistsWhenSavingAnIncomingEvent() throws Exception {
        incomingEvent.getIncoming().setDeviceGuid("unknown_device");
        
        application = Application.builder().name("fake").build();

        thrown.expect(BusinessException.class);
        thrown.expectMessage(EventRepository.Validations.INCOMING_DEVICE_ID_DOES_NOT_EXIST.getCode());

        eventRepository.saveIncoming(tenant, application, incomingEvent);
    }

    @Test
    public void shouldRaiseAnExceptionIfEventTimestampIsNullWhenSavingAnIncomingEvent() throws Exception {
        incomingEvent.setCreationTimestamp(null);
        
        application = Application.builder().name("fake").build();

        thrown.expect(BusinessException.class);
        thrown.expectMessage(EventRepository.Validations.EVENT_TIMESTAMP_NULL.getCode());

        eventRepository.saveIncoming(tenant, application, incomingEvent);
    }

    @Test
    public void shouldSaveTheIncomingEvent() throws Exception {
    	application = Application.builder().name("fake").build();
        eventRepository.saveIncoming(tenant, application, incomingEvent);
        

        DBObject saved = mongoTemplate.findOne(
                Query.query(Criteria.where("incoming.deviceGuid").is(deviceGuid)
                        .andOperator(Criteria.where("ts").is(firstEventTimestamp.toEpochMilli()))),
                DBObject.class,
                EventRepositoryMongoImpl.EVENTS_INCOMING_COLLECTION_NAME
        );
        saved.removeField("_id");
        saved.removeField("_class");

        assertThat(saved, equalTo(persisted));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json","/fixtures/devices.json","/fixtures/deviceEvents.json","/fixtures/applications.json"})
    public void shouldRetrieveLastTwoEventsByTenantAndDeviceWhenFindingIncomingBy() throws Exception {
        List<Event> events = eventRepository.findIncomingBy(tenant, application, deviceGuid, "command",
                firstEventTimestamp.plus(1,ChronoUnit.SECONDS),
                null,false,2);

        assertThat(events,notNullValue());
        assertThat(events,hasSize(2));

        assertThat(events.get(0).getCreationTimestamp().toEpochMilli(),equalTo(thirdEventTimestamp.toEpochMilli()));
        assertThat(events.get(1).getCreationTimestamp().toEpochMilli(),equalTo(secondEventTimestamp.toEpochMilli()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json","/fixtures/devices.json","/fixtures/deviceEvents.json","/fixtures/applications.json"})
    public void shouldRemoveEvents() throws Exception {
        List<Event> events = eventRepository.findIncomingBy(tenant, application, deviceGuid, "command",
                firstEventTimestamp.plus(1,ChronoUnit.SECONDS),
                null,false,2);

        assertThat(events,notNullValue());
        assertThat(events,hasSize(2));

        eventRepository.removeBy(tenant, application, deviceGuid);

        events = eventRepository.findIncomingBy(tenant, application, deviceGuid, "command",
                firstEventTimestamp.plus(1,ChronoUnit.SECONDS),
                null,false,2);

        assertThat(events,notNullValue());
        assertThat(events,hasSize(0));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json","/fixtures/applications.json","/fixtures/devices.json","/fixtures/deviceEvents.json", "/fixtures/events-incoming.json"})
    public void shouldCopyEvents() throws Exception {
        String destinationGuid = "8363c556-84ea-11e6-92a2-4b01fea7e243";

        List<Event> events = eventRepository.findIncomingBy(tenant, application, destinationGuid, "command",
                firstEventTimestamp.plus(1,ChronoUnit.SECONDS),
                null,false,2);

        assertThat(events,notNullValue());
        assertThat(events,hasSize(0));

        Device originDevice = deviceRepository.findByTenantAndApplicationAndGuid(tenant.getId(), application.getName(), "8363c556-84ea-11e6-92a2-4b01fea7e259");
        Device destDevice = deviceRepository.findByTenantAndApplicationAndGuid(tenant.getId(), applicationSmartff.getName(), destinationGuid);

        eventRepository.copy(tenant, originDevice, destDevice);

        // checks if events were copied
        events = eventRepository.findIncomingBy(tenant, applicationSmartff, destinationGuid, "e4399b2ed998.testchannel",
                null,
                null,
                false,
                2);

        assertThat(events,notNullValue());
        assertThat(events,hasSize(2));

    }

    @Test
    public void shouldRaiseAnExceptionIfTenantIsNullWhenFindingIncomingBy() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Tenant cannot be null");

        eventRepository.findIncomingBy(null,null,deviceGuid,null,firstEventTimestamp,null,false,null);
    }

    @Test
    public void shouldRaiseAnExceptionIfStartingOffsetAndLimitAreNullWhenFindingIncomingBy() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Limit cannot be null when start instant isn't provided");


        eventRepository.findIncomingBy(tenant,application,deviceGuid,null,null,null,false,null);
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json","/fixtures/devices.json","/fixtures/deviceEvents.json","/fixtures/applications.json"})
    public void shouldRetrieveTheOnlyFirstEventByTenantAndDeviceWhenFindingIncomingBy() throws Exception {
        List<Event> events = eventRepository.findIncomingBy(tenant, application,
                deviceGuid,"command",
                firstEventTimestamp,
                secondEventTimestamp.minus(1, ChronoUnit.SECONDS),true,
                1);

        assertThat(events,notNullValue());
        assertThat(events,hasSize(1));

        assertThat(events.get(0).getCreationTimestamp().toEpochMilli(),equalTo(firstEventTimestamp.toEpochMilli() + 1));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json","/fixtures/devices.json","/fixtures/deviceEvents.json","/fixtures/applications.json"})
    public void shouldLimitResultsAccordingToLimitParameterWhenFindingIncomingBy() throws Exception {
        List<Event> events = eventRepository.findIncomingBy(tenant, application,
                deviceGuid,"command",
                firstEventTimestamp,
                thirdEventTimestamp,false,
                1);

        assertThat(events,notNullValue());
        assertThat(events,hasSize(1));

        assertThat(events.get(0).getCreationTimestamp().toEpochMilli(),equalTo(thirdEventTimestamp.toEpochMilli()));
    }
}