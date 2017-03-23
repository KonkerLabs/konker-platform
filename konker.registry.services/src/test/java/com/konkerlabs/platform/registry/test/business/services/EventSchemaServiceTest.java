package com.konkerlabs.platform.registry.test.business.services;

import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.isResponseOk;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.EventSchema;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.repositories.DeviceRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.repositories.events.api.EventRepository;
import com.konkerlabs.platform.registry.business.services.api.EventSchemaService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.config.PubServerConfig;
import com.konkerlabs.platform.registry.test.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;
import com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
    MongoTestConfiguration.class,
    BusinessTestConfiguration.class,
    PubServerConfig.class
})
@UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json"})
public class EventSchemaServiceTest extends BusinessLayerTestSupport {

    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private DeviceRepository deviceRepository;
    @Autowired
    @Qualifier("mongoEvents")
    private EventRepository eventRepository;
    @Autowired
    private EventSchemaService eventSchemaService;

    private String deviceGuid = "8363c556-84ea-11e6-92a2-4b01fea7e243";

    private Tenant tenant;
    private String firstChannel;
    private String secondChannel;

    private EventSchema firstEventSchema;
    private EventSchema secondEventSchema;

    private String firstField = "root.fieldOne";
    private String secondField = "root.fieldTwo";

    private String firstJson = "{\n" +
            "    \"root\" : {\n" +
            "        \"fieldOne\" : \"valueOne\",\n" +
            "        \"fieldTwo\" : \"valueTwo\"\n" +
            "    }\n" +
            "}";

    private String secondJson = "{\n" +
            "    \"root\" : {\n" +
            "        \"fieldOne\" : \"valueOne\",\n" +
            "        \"fieldTwo\" : 22.2\n" +
            "    }\n" +
            "}";

    private Event incomingEvent;

    @Before
    public void setUp() throws Exception {
        tenant = tenantRepository.findByDomainName("konker");
        firstChannel = "command";
        secondChannel = "data";

        firstEventSchema = EventSchema.builder()
            .deviceGuid(deviceGuid)
            .channel(firstChannel)
            .field(
                EventSchema.SchemaField.builder()
                        .path(firstField)
                        .knownType(JsonNodeType.OBJECT)
                        .knownType(JsonNodeType.STRING).build())
            .field(
                EventSchema.SchemaField.builder()
                        .path(secondField)
                        .knownType(JsonNodeType.OBJECT)
                        .knownType(JsonNodeType.STRING).build())
            .build();

        secondEventSchema = EventSchema.builder()
                .deviceGuid(deviceGuid)
                .channel(secondChannel)
                .field(
                        EventSchema.SchemaField.builder()
                                .path(firstField)
                                .knownType(JsonNodeType.OBJECT)
                                .knownType(JsonNodeType.STRING).build())
                .field(
                        EventSchema.SchemaField.builder()
                                .path(secondField)
                                .knownType(JsonNodeType.OBJECT)
                                .knownType(JsonNodeType.NUMBER).build())
                .build();

        incomingEvent = Event.builder()
                .payload(firstJson)
                .timestamp(Instant.now())
                .incoming(
                        Event.EventActor.builder()
                                .deviceGuid(deviceGuid)
                                .channel(firstChannel)
                                .tenantDomain(tenant.getDomainName())
                                .build()).build();
    }

    @Test
    public void shouldSaveIncomingSchema() throws Exception {
        ServiceResponse<EventSchema> schema = eventSchemaService.appendIncomingSchema(incomingEvent);

        assertThat(schema,isResponseOk());
        schema.getResult().setId(null);
        assertThat(schema.getResult(),equalTo(firstEventSchema));

        incomingEvent.setPayload(secondJson);
        incomingEvent.getIncoming().setChannel(secondChannel);
        incomingEvent.setTimestamp(Instant.now().plus(2,ChronoUnit.MINUTES));

        schema = eventSchemaService.appendIncomingSchema(incomingEvent);

        assertThat(schema,isResponseOk());
        schema.getResult().setId(null);
        assertThat(schema.getResult(),equalTo(secondEventSchema));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json", "/fixtures/eventSchemas.json"})
    public void shouldRetrieveAllDistinctChannelByDeviceGuid() throws Exception {
        List<String> knownChannels = Arrays.asList(new String[]{"command","data"});

        ServiceResponse<List<String>> response = eventSchemaService.findKnownIncomingChannelsBy(tenant,deviceGuid);

        assertThat(response,isResponseOk());
        assertThat(response.getResult(),equalTo(knownChannels));
    }
    
    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json", "/fixtures/eventSchemas.json"})
    public void shouldFindKnownIncomingMetrics() throws Exception {
        List<String> knownMetrics = Arrays.asList(new String[]{"temperature"});

        ServiceResponse<List<String>> response = eventSchemaService.findKnownIncomingMetricsBy(tenant, deviceGuid, "data", JsonNodeType.NUMBER);

        assertThat(response,isResponseOk());
        assertThat(response.getResult(), equalTo(knownMetrics));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json", "/fixtures/eventSchemas.json"})
    public void shouldFindLastIncomingBy() throws Exception {

        Tenant tenant = tenantRepository.findByDomainName("konker");
        String channel = "tDs8hinlkT";
        String deviceGuid = "dde1129e-4c6c-4ec4-89dc-425857b68009";

        deviceRepository.save(Device.builder().tenant(tenant).guid(deviceGuid).name("b2cwPd7QgQ").build());

        // Non numeric event
        Event incomingEvent = Event.builder()
                .payload("{\"city\": \"RJ\"}")
                .timestamp(Instant.now())
                .incoming(
                        Event.EventActor.builder()
                                .deviceGuid(deviceGuid)
                                .channel(channel)
                                .tenantDomain(tenant.getDomainName())
                                .build()).build();

        eventSchemaService.appendIncomingSchema(incomingEvent);
        eventRepository.saveIncoming(tenant, incomingEvent);

        // Numeric event
        Event incomingEventSnd = Event.builder()
                .payload(secondJson)
                .timestamp(Instant.now().minusSeconds(10))
                .incoming(
                        Event.EventActor.builder()
                                .deviceGuid(deviceGuid)
                                .channel(channel)
                                .tenantDomain(tenant.getDomainName())
                                .build()).build();

        eventSchemaService.appendIncomingSchema(incomingEventSnd);
        eventRepository.saveIncoming(tenant, incomingEventSnd);


        ServiceResponse<EventSchema> response = eventSchemaService.findLastIncomingBy(tenant, deviceGuid, JsonNodeType.NUMBER);

        assertThat(response, isResponseOk());
        assertThat(response.getResult().getChannel(), equalTo(channel));
        assertThat(response.getResult().getFields().iterator().next().getPath(), equalTo(secondField));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json", "/fixtures/eventSchemas.json"})
    public void shouldFindIncomingByGeviceGuidAndChannel() throws Exception {
        ServiceResponse<EventSchema> response = eventSchemaService.findIncomingBy(tenant, deviceGuid, "command");

        assertThat(response,isResponseOk());
        assertThat(response.getResult().getChannel(), equalTo("command"));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json", "/fixtures/eventSchemas.json"})
    public void shouldFindIncomingByGeviceGuidAndChannelWithInvalidTenant() throws Exception {
        Tenant otherTenant = tenantRepository.findByDomainName("inm");
        
        ServiceResponse<EventSchema> response = eventSchemaService.findIncomingBy(otherTenant, deviceGuid, "command");

        assertThat(response, ServiceResponseMatchers.hasErrorMessage("service.device.guid.does_not_exist"));
    }

}