package com.konkerlabs.platform.registry.test.business.services;

import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.repositories.EventRuleRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import com.konkerlabs.platform.registry.business.services.rules.api.EventRuleExecutor;
import com.konkerlabs.platform.registry.business.services.rules.api.EventRuleService;
import com.konkerlabs.platform.registry.test.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MongoTestConfiguration.class, BusinessTestConfiguration.class})
public class EventRuleExecutorTest extends BusinessLayerTestSupport {

    @Autowired
    private EventRuleExecutor subject;

    private Event event;

    private URI uri;

    @Before
    public void setUp() throws Exception {
        event = spy(Event.builder().channel("data").timestamp(Instant.now()).payload("LEDSwitch").build());
        uri = new URI("device://0000000000000004/");
    }

    @Test
    @UsingDataSet(locations = "/fixtures/event-rules.json")
    public void shouldSendOneEvent() throws ExecutionException, InterruptedException {
        Future<List<Event>> eventFuture = subject.execute(event, uri);
        assertThat(eventFuture.get(), notNullValue());
        assertThat(eventFuture.get(), hasSize(2));
        assertThat(eventFuture.get().get(0).getPayload(), equalTo("LEDSwitch"));
    }

    @Test
    @UsingDataSet(locations = "/fixtures/event-rules.json")
    public void shouldntSendAnyEvents() throws ExecutionException, InterruptedException, URISyntaxException {
        Future<List<Event>> eventFuture = subject.execute(event, new URI("device://non_existing_device/"));
        assertThat(eventFuture.get(), notNullValue());
        assertThat(eventFuture.get(), hasSize(0));
    }
}
