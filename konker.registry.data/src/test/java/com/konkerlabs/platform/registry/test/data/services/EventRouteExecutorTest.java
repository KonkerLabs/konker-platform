package com.konkerlabs.platform.registry.test.data.services;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.DeviceModel;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Location;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.repositories.ApplicationRepository;
import com.konkerlabs.platform.registry.business.repositories.DeviceModelRepository;
import com.konkerlabs.platform.registry.business.repositories.LocationRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.config.EventStorageConfig;
import com.konkerlabs.platform.registry.config.PubServerConfig;
import com.konkerlabs.platform.registry.data.services.routes.api.EventRouteExecutor;
import com.konkerlabs.platform.registry.integration.gateways.HttpGateway;
import com.konkerlabs.platform.registry.test.data.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.data.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.data.base.MongoTestConfiguration;
import com.konkerlabs.platform.registry.test.data.base.*;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
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
        BusinessTestConfiguration.class,
        MongoTestConfiguration.class,
        RedisTestConfiguration.class,
        UtilitiesConfig.class,
        PubServerConfig.class,
        EventStorageConfig.class

})
@UsingDataSet(locations = {
        "/fixtures/tenants.json",
        "/fixtures/applications.json",
        "/fixtures/devices.json",
        "/fixtures/event-routes.json"})
public class EventRouteExecutorTest extends BusinessLayerTestSupport {

    private static final String REGISTERED_TENANT_DOMAIN = "konker";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Autowired
    private EventRouteExecutor subject;
    @Autowired
    private HttpGateway httpGateway;

    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private DeviceModelRepository deviceModelRepository;
    @Autowired
    private LocationRepository locationRepository;

    private Tenant tenant;
    private Application application;
    private DeviceModel deviceModel;
    private Location locationParent;
    private Location locationChild;

    private Event event;

    private String inactiveRouteDeviceId = "0000000000000001";
    private String malformedRouteDeviceId = "0000000000000002";
    private String matchingRouteDeviceGuid = "1af9be20-441e-419b-84a9-cb84efd4f49d";
    private String nonMatchingFilterDeviceId = "0000000000000007";
    private String nonMatchingRouteDeviceId = "0000000000000009";

    private String payload = "{\"metric\":\"temperature\",\"deviceGuid\":\"1af9be20-441e-419b-84a9-cb84efd4f49d\",\"value\":30,\"ts\":1454900000,\"data\":{\"sn\":1234,\"test\":1,\"foo\":2}}";
    private String transformationResponse = "{\"okToGo\" : true }";

    @Before
    public void setUp() throws Exception {
        event = spy(Event.builder()
                .incoming(
                        Event.EventActor.builder()
                                .channel("data")
                                .deviceGuid(matchingRouteDeviceGuid).build()
                ).timestamp(Instant.now()).payload(payload).build());

        tenant = tenantRepository.findByDomainName(REGISTERED_TENANT_DOMAIN);
        application = applicationRepository.findByTenantAndName(tenant.getId(), "konker");

        deviceModel = DeviceModel.builder()
                                 .tenant(tenant)
                                 .application(application)
                                 .name("model-0002")
                                 .guid("96fbd654-8240-4003-b5f2-a4aa366b7b18")
                                 .build();
        deviceModel = deviceModelRepository.save(deviceModel);

        locationParent = Location.builder()
                .tenant(tenant)
                .application(application)
                .name("ny")
                .guid("d1e9beb7-046a-4796-b1dd-41aec85f4a94")
                .build();
        locationParent = locationRepository.save(locationParent);

        locationChild = Location.builder()
                .tenant(tenant)
                .application(application)
                .parent(locationParent)
                .name("5th ave")
                .guid("82751a08-deaf-482c-8609-24ca1203f915")
                .build();
        locationChild = locationRepository.save(locationChild);

        when(
                httpGateway.request(
                        eq(HttpMethod.POST),
                        Mockito.any(HttpHeaders.class),
                        Mockito.any(URI.class),
                        Mockito.any(MediaType.class),
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
            "/fixtures/applications.json",
            "/fixtures/devices.json",
            "/fixtures/transformations.json",
            "/fixtures/event-routes.json"})
    public void shouldSendEventsForAMatchingRoute() throws ExecutionException, InterruptedException {
        Device device = Device.builder()
                              .tenant(tenant)
                              .application(application)
                              .guid(matchingRouteDeviceGuid)
                              .build();

        Future<List<Event>> eventFuture = subject.execute(event, device);
        assertThat(eventFuture.get(), notNullValue());
        assertThat(eventFuture.get(), hasSize(1));
        assertThat(eventFuture.get().get(0).getPayload(), equalTo(transformationResponse));
    }

    @Test
    @UsingDataSet(locations = {
            "/fixtures/tenants.json",
            "/fixtures/applications.json",
            "/fixtures/devices.json",
            "/fixtures/transformations.json",
            "/fixtures/event-routes.json"})
    public void shouldSendAnyEventsForInvalidLocation() throws ExecutionException, InterruptedException {
        Device device = Device.builder()
                              .tenant(tenant)
                              .application(application)
                              .deviceModel(deviceModel)
                              .location(locationChild)
                              .guid(nonMatchingFilterDeviceId)
                              .build();

        Future<List<Event>> eventFuture = subject.execute(event, device);
        assertThat(eventFuture.get(), notNullValue());
        assertThat(eventFuture.get(), hasSize(1));
        assertThat(eventFuture.get().get(0).getPayload(), equalTo(payload));
    }

    @Test
    public void shouldntSendAnyEventsForANonmatchingRoute() throws ExecutionException, InterruptedException, URISyntaxException {
        Device device = Device.builder()
                .tenant(tenant)
                .application(application)
                .guid(nonMatchingFilterDeviceId)
                .build();

        Future<List<Event>> eventFuture = subject.execute(event, device);
        assertThat(eventFuture.get(), notNullValue());
        assertThat(eventFuture.get(), hasSize(0));
    }

    @Test
    public void shouldntSendAnyEventsForANonMatchingIncomingDevice() throws ExecutionException, InterruptedException, URISyntaxException {
        Device device = Device.builder()
                .tenant(tenant)
                .application(application)
                .guid(nonMatchingRouteDeviceId)
                .build();

        Future<List<Event>> eventFuture = subject.execute(event, device);
        assertThat(eventFuture.get(), notNullValue());
        assertThat(eventFuture.get(), hasSize(0));
    }

    @Test
    public void shouldntSendAnyEventsForANonMatchingIncomingChannel() throws ExecutionException, InterruptedException, URISyntaxException {
        Device device = Device.builder()
                .tenant(tenant)
                .application(application)
                .guid(matchingRouteDeviceGuid)
                .build();

        event.getIncoming().setChannel("non_matching_channel");
        Future<List<Event>> eventFuture = subject.execute(event, device);
        assertThat(eventFuture.get(), notNullValue());
        assertThat(eventFuture.get(), hasSize(0));
    }

    @Test
    public void shouldntSendAnyEventsForANonActiveRoute() throws ExecutionException, InterruptedException, URISyntaxException {
        Device device = Device.builder()
                .tenant(tenant)
                .application(application)
                .guid(inactiveRouteDeviceId)
                .build();

        Future<List<Event>> eventFuture = subject.execute(event, device);
        assertThat(eventFuture, notNullValue());
        assertThat(eventFuture.get(), hasSize(0));
    }

    @Test
    public void shouldntSendAnyEventsForMalformedExpressionFilter() throws ExecutionException, InterruptedException, URISyntaxException {
        Device device = Device.builder()
                .tenant(tenant)
                .application(application)
                .guid(malformedRouteDeviceId)
                .build();

        Future<List<Event>> eventFuture = subject.execute(event, device);
        assertThat(eventFuture, notNullValue());
        assertThat(eventFuture.get(), hasSize(0));
    }

}
