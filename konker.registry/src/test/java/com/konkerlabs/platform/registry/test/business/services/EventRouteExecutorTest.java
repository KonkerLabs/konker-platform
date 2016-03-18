package com.konkerlabs.platform.registry.test.business.services;

import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.behaviors.DeviceURIDealer;
import com.konkerlabs.platform.registry.business.services.routes.api.EventRouteExecutor;
import com.konkerlabs.platform.registry.integration.gateways.HttpGateway;
import com.konkerlabs.platform.registry.test.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;
import com.konkerlabs.platform.registry.test.base.SolrTestConfiguration;
import com.konkerlabs.platform.utilities.config.UtilitiesConfig;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        MongoTestConfiguration.class,
        UtilitiesConfig.class,
        BusinessTestConfiguration.class,
        SolrTestConfiguration.class
})
public class EventRouteExecutorTest extends BusinessLayerTestSupport {

    private static final String REGISTERED_TENANT_DOMAIN = "konker";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Autowired
    private EventRouteExecutor subject;
    @Autowired
    private HttpGateway httpGateway;

    private Event event;
    private URI uri;

    private String inactiveRouteDeviceId = "0000000000000001";
    private String malformedRouteDeviceId = "0000000000000002";
    private String matchingRouteDeviceId = "0000000000000004";
    private String nonMatchingFilterDeviceId = "0000000000000007";
    private String nonMatchingRouteDeviceId = "0000000000000009";

    private String payload = "{\"metric\":\"temperature\",\"deviceId\":\"0000000000000004\",\"value\":30,\"ts\":1454900000,\"data\":{\"sn\":1234,\"test\":1,\"foo\":2}}";
    private String transformationResponse = "{\"okToGo\" : true }";

    @Before
    public void setUp() throws Exception {
        event = spy(Event.builder().channel("data").timestamp(Instant.now()).payload(payload).build());
        uri = new DeviceURIDealer() {}.toDeviceRouteURI(REGISTERED_TENANT_DOMAIN,matchingRouteDeviceId);

        when(
            httpGateway.request(
                eq(HttpMethod.POST),
                Mockito.any(URI.class),
                Mockito.any(Supplier.class),
                Mockito.anyString(),
                Mockito.anyString()
            )
        ).thenReturn(transformationResponse);
    }

    @After
    public void tearDown() throws Exception {
        Mockito.reset(httpGateway);
    }

    @Test
    @UsingDataSet(locations = {
        "/fixtures/tenants.json",
        "/fixtures/devices.json",
        "/fixtures/transformations.json",
        "/fixtures/sms-destinations.json",
        "/fixtures/event-routes.json"})
    public void shouldSendEventsForAMatchingRoute() throws ExecutionException, InterruptedException {
        Future<List<Event>> eventFuture = subject.execute(event, uri);
        assertThat(eventFuture.get(), notNullValue());
        assertThat(eventFuture.get(), hasSize(3));
        assertThat(eventFuture.get().get(0).getPayload(), equalTo(transformationResponse));
        assertThat(eventFuture.get().get(1).getPayload(), equalTo(transformationResponse));
        assertThat(eventFuture.get().get(2).getPayload(), equalTo(transformationResponse));
    }

    @Test
    @UsingDataSet(locations = "/fixtures/event-routes.json")
    public void shouldntSendAnyEventsForANonmatchingRoute() throws ExecutionException, InterruptedException, URISyntaxException {
        URI nonMatchingFilterURI = new URI("device",nonMatchingFilterDeviceId,null,null,null);
        Future<List<Event>> eventFuture = subject.execute(event, nonMatchingFilterURI);
        assertThat(eventFuture.get(), notNullValue());
        assertThat(eventFuture.get(), hasSize(0));
    }

    @Test
    @UsingDataSet(locations = "/fixtures/event-routes.json")
    public void shouldntSendAnyEventsForANonMatchingIncomingDevice() throws ExecutionException, InterruptedException, URISyntaxException {
        URI nonMatchingDeviceURI = new URI("device",nonMatchingRouteDeviceId,null,null,null);
        Future<List<Event>> eventFuture = subject.execute(event, nonMatchingDeviceURI);
        assertThat(eventFuture.get(), notNullValue());
        assertThat(eventFuture.get(), hasSize(0));
    }

    @Test
    @UsingDataSet(locations = "/fixtures/event-routes.json")
    public void shouldntSendAnyEventsForANonMatchingIncomingChannel() throws ExecutionException, InterruptedException, URISyntaxException {
        URI nonMatchingDeviceURI = new URI("device",matchingRouteDeviceId,null,null,null);
        event.setChannel("non_matching_channel");
        Future<List<Event>> eventFuture = subject.execute(event, nonMatchingDeviceURI);
        assertThat(eventFuture.get(), notNullValue());
        assertThat(eventFuture.get(), hasSize(0));
    }

    @Test
    @UsingDataSet(locations = "/fixtures/event-routes.json")
    public void shouldntSendAnyEventsForANonActiveRoute() throws ExecutionException, InterruptedException, URISyntaxException {
        URI inactiveRouteDeviceURI = new URI("device",inactiveRouteDeviceId,null,null,null);
        Future<List<Event>> eventFuture = subject.execute(event, inactiveRouteDeviceURI);
        assertThat(eventFuture, notNullValue());
        assertThat(eventFuture.get(), hasSize(0));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/event-routes.json"})
    public void shouldntSendAnyEventsForMalformedExpressionFilter() throws ExecutionException, InterruptedException, URISyntaxException {
        URI nonBooleanRouteDeviceURI = new URI("device", malformedRouteDeviceId,null,null,null);
        Future<List<Event>> eventFuture = subject.execute(event, nonBooleanRouteDeviceURI);
        assertThat(eventFuture, notNullValue());
        assertThat(eventFuture.get(), hasSize(0));
    }
}
