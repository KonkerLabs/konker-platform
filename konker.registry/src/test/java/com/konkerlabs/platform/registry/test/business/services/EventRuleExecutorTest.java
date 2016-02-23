package com.konkerlabs.platform.registry.test.business.services;

import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.services.rules.api.EventRuleExecutor;
import com.konkerlabs.platform.registry.test.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.spy;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MongoTestConfiguration.class, BusinessTestConfiguration.class})
public class EventRuleExecutorTest extends BusinessLayerTestSupport {

    @Autowired
    private EventRuleExecutor subject;
    private Event event;
    private URI uri;

    private String matchingRuleDeviceId = "0000000000000004";
    private String nonMatchingRuleDeviceId = "0000000000000009";

    private String payload = "{\"metric\":\"temperature\",\"device\":\"0000000000000004\",\"value\":30,\"timestamp\":1454900000,\"data\":{\"sn\":1234,\"test\":1,\"foo\":2}}";

    @Before
    public void setUp() throws Exception {
        event = spy(Event.builder().channel("data").timestamp(Instant.now()).payload(payload).build());
        uri = new URI("device",matchingRuleDeviceId,null,null,null);
    }

    @Test
    @UsingDataSet(locations = "/fixtures/event-rules.json")
    public void shouldSendOneEvent() throws ExecutionException, InterruptedException {
        Future<List<Event>> eventFuture = subject.execute(event, uri);
        assertThat(eventFuture.get(), notNullValue());
        assertThat(eventFuture.get(), hasSize(2));
        assertThat(eventFuture.get().get(0).getPayload(), equalTo(payload));
    }

    @Test
    @UsingDataSet(locations = "/fixtures/event-rules.json")
    public void shouldntSendAnyEventsForANonMatchingIncomingDevice() throws ExecutionException, InterruptedException, URISyntaxException {
        URI nonMatchingDeviceURI = new URI("device",nonMatchingRuleDeviceId,null,null,null);
        Future<List<Event>> eventFuture = subject.execute(event, nonMatchingDeviceURI);
        assertThat(eventFuture.get(), notNullValue());
        assertThat(eventFuture.get(), hasSize(0));
    }

    @Test
    @UsingDataSet(locations = "/fixtures/event-rules.json")
    public void shouldntSendAnyEventsForANonMatchingIncomingChannel() throws ExecutionException, InterruptedException, URISyntaxException {
        URI nonMatchingDeviceURI = new URI("device",matchingRuleDeviceId,null,null,null);
        event.setChannel("non_matching_channel");
        Future<List<Event>> eventFuture = subject.execute(event, nonMatchingDeviceURI);
        assertThat(eventFuture.get(), notNullValue());
        assertThat(eventFuture.get(), hasSize(0));
    }
}
