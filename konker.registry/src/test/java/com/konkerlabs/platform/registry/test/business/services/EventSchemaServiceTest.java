package com.konkerlabs.platform.registry.test.business.services;

import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.EventSchema;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.repositories.DeviceRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.EventSchemaService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.config.PubServerConfig;
import com.konkerlabs.platform.registry.test.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;
import com.konkerlabs.platform.registry.test.base.RedisTestConfiguration;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.isResponseOk;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
    MongoTestConfiguration.class,
    BusinessTestConfiguration.class,
    RedisTestConfiguration.class,
    PubServerConfig.class
})
@UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json"})
public class EventSchemaServiceTest extends BusinessLayerTestSupport {

    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private DeviceRepository deviceRepository;
    @Autowired
    private EventSchemaService eventSchemaService;
    @Autowired
    private EventSchemaService subject;

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
}