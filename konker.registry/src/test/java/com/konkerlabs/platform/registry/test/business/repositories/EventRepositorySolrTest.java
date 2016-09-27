package com.konkerlabs.platform.registry.test.business.repositories;

import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.repositories.events.EventRepository;
import com.konkerlabs.platform.registry.test.base.*;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.Instant;

import static com.konkerlabs.platform.registry.business.services.publishers.EventPublisherDevice.DEVICE_MQTT_CHANNEL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        BusinessTestConfiguration.class,
        MongoTestConfiguration.class,
        SolrTestConfiguration.class,
        RedisTestConfiguration.class
})
@UsingDataSet(locations = {"/fixtures/tenants.json"})
public class EventRepositorySolrTest extends BusinessLayerTestSupport {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    @Qualifier("solr")
    private EventRepository eventRepository;

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

    private Tenant tenant;
    private Event event;
    private SolrInputDocument toBeSent;

    @Captor
    private ArgumentCaptor<SolrInputDocument> inputCaptor;
    @Autowired
    private SolrTemplate solrTemplate;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        tenant = tenantRepository.findByDomainName("konker");

        event = Event.builder()
                .channel(DEVICE_MQTT_CHANNEL)
                .timestamp(Instant.now())
                .payload(payload).build();

        toBeSent = new SolrInputDocument();
        toBeSent.addField("data.channels.0.name", "channel_0");
        toBeSent.addField("command.type", "ButtonPressed");
        toBeSent.addField("time", 123L);
        toBeSent.addField("value", 31.0);
        toBeSent.addField("ts", event.getTimestamp().toString());
    }

    @Test
    public void shouldRaiseAnExceptionIfTenantIsNull() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Tenant cannot be null");

        eventRepository.push(null, event);
    }

    @Test
    public void shouldRaiseAnExceptionIfEventIsNull() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Event cannot be null");

        eventRepository.push(tenant, null);
    }

    @Test
    public void shouldRaiseAnExceptionIfEventTimestampIsNull() throws Exception {
        event.setTimestamp(null);

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Event timestamp cannot be null");

        eventRepository.push(tenant, event);
    }

    @Test
    public void shouldPushTheIncomingEvent() throws Exception {
        when(
                solrTemplate.saveDocument(inputCaptor.capture())
        ).thenReturn(new UpdateResponse());

        eventRepository.push(tenant, event);

        SolrInputDocument saved = inputCaptor.getValue();

        saved.getFieldNames().stream().forEach(sent -> {
            assertThat(saved.getFieldValue(sent), equalTo(toBeSent.getFieldValue(sent)));
        });
    }
}