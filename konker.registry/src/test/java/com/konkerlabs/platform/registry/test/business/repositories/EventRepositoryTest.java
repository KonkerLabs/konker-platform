package com.konkerlabs.platform.registry.test.business.repositories;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.repositories.DeviceRepository;
import com.konkerlabs.platform.registry.business.repositories.solr.EventRepository;
import com.konkerlabs.platform.registry.test.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;
import com.konkerlabs.platform.registry.test.base.SolrTestConfiguration;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.Instant;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
    BusinessTestConfiguration.class,
    MongoTestConfiguration.class,
    SolrTestConfiguration.class
})
@UsingDataSet(locations = {"/fixtures/tenants.json","/fixtures/devices.json"})
public class EventRepositoryTest extends BusinessLayerTestSupport {

    private static final String DEVICE_ID_IN_USE = "95c14b36ba2b43f1";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private EventRepository eventRepository;

    private Device device;

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
    private Event event;

    @Before
    public void setUp() throws Exception {
        device = deviceRepository.findByDeviceId(DEVICE_ID_IN_USE);

        event = Event.builder()
            .channel("channel")
            .timestamp(Instant.now())
            .payload(payload).build();
    }

    @Test
    public void shouldRaiseAnExceptionIfDeviceIsNull() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Device cannot be null");

        eventRepository.push(null,event);
    }

    @Test
    public void shouldRaiseAnExceptionIfEventIsNull() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Event cannot be null");

        eventRepository.push(device,null);
    }

    @Test
    public void shouldRaiseAnExceptionIfEventTimestampIsNull() throws Exception {
        event.setTimestamp(null);

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Event timestamp cannot be null");

        eventRepository.push(device,event);
    }

    @Test
    public void shouldPushTheIncomingEvent() throws Exception {}
}