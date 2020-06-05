package com.konkerlabs.platform.registry.test.data.core.services;

import com.konkerlabs.platform.registry.billing.repositories.TenantDailyUsageRepository;
import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.repositories.ApplicationRepository;
import com.konkerlabs.platform.registry.business.repositories.DeviceModelRepository;
import com.konkerlabs.platform.registry.business.repositories.LocationRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.config.EmailConfig;
import com.konkerlabs.platform.registry.config.EventStorageConfig;
import com.konkerlabs.platform.registry.config.PubServerConfig;
import com.konkerlabs.platform.registry.data.core.integration.gateway.HttpGateway;
import com.konkerlabs.platform.registry.data.core.services.routes.api.EventRouteExecutor;
import com.konkerlabs.platform.registry.test.data.core.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.data.core.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.data.core.base.MongoTestConfiguration;
import com.konkerlabs.platform.registry.test.data.core.base.RedisTestConfiguration;
import com.konkerlabs.platform.utilities.config.UtilitiesConfig;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.thymeleaf.spring4.SpringTemplateEngine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        BusinessTestConfiguration.class,
        MongoTestConfiguration.class,
        RedisTestConfiguration.class,
        UtilitiesConfig.class,
        PubServerConfig.class,
        EventStorageConfig.class,
        EmailConfig.class,
        EventRouteExecutorTest.EventRouteExecutorTestConfig.class

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
    private RabbitTemplate rabbitTemplate;
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

    private final String inactiveRouteDeviceId = "0000000000000001";
    private final String malformedRouteDeviceId = "0000000000000002";
    private final String matchingRouteDeviceGuid = "1af9be20-441e-419b-84a9-cb84efd4f49d";
    private final String nonMatchingFilterDeviceId = "0000000000000007";
    private final String nonMatchingRouteDeviceId = "0000000000000009";

    private final String payload = "{\"metric\":\"temperature\",\"deviceGuid\":\"1af9be20-441e-419b-84a9-cb84efd4f49d\",\"value\":30,\"ts\":1454900000,\"data\":{\"sn\":1234,\"test\":1,\"foo\":2}}";
    private final String transformationResponse = "{\"okToGo\" : true }";

    @Before
    public void setUp() throws Exception {
        event = spy(Event.builder()
                .incoming(
                        Event.EventActor.builder()
                                .channel("data")
                                .deviceGuid(matchingRouteDeviceGuid).build()
                )
                .outgoing(
                        Event.EventActor.builder()
                            .channel("in")
                            .deviceGuid("7d51c242-81db-11e6-a8c2-0746f010e945").build()
                )
                .creationTimestamp(Instant.now()).payload(payload).build());

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
    public void tearDown() {
        Mockito.reset(httpGateway);
        Mockito.reset(rabbitTemplate);
    }

    @Test
    @UsingDataSet(locations = {
            "/fixtures/tenants.json",
            "/fixtures/applications.json",
            "/fixtures/devices.json",
            "/fixtures/transformations.json",
            "/fixtures/event-routes.json"})
    public void shouldSendEventsForAMatchingRoute() throws Exception {
        Device device = Device.builder()
                              .tenant(tenant)
                              .application(application)
                              .guid(matchingRouteDeviceGuid)
                              .build();

        Future<List<Event>> eventFuture = subject.execute(event, device);
        assertThat(eventFuture.get(), notNullValue());

        verify(rabbitTemplate, times(2)).convertAndSend(Mockito.anyString(), Mockito.any(Message.class));
    }

    @Test
    @UsingDataSet(locations = {
            "/fixtures/tenants.json",
            "/fixtures/applications.json",
            "/fixtures/devices.json",
            "/fixtures/transformations.json",
            "/fixtures/event-routes.json"})
    public void shouldSendAnyEventsForInvalidLocation() throws Exception {
        Device device = Device.builder()
                              .tenant(tenant)
                              .application(application)
                              .deviceModel(deviceModel)
                              .location(locationChild)
                              .guid(nonMatchingFilterDeviceId)
                              .build();

        Future<List<Event>> eventFuture = subject.execute(event, device);
        assertThat(eventFuture.get(), notNullValue());

        verify(rabbitTemplate, times(2)).convertAndSend(Mockito.anyString(), Mockito.any(Message.class));
    }

    @Test
    @UsingDataSet(locations = {
            "/fixtures/tenants.json",
            "/fixtures/applications.json",
            "/fixtures/devices.json"})
    public void shouldSendAnyEventsForEchoChannel() throws Exception {
        Device device = Device.builder()
                .tenant(tenant)
                .application(application)
                .deviceModel(deviceModel)
                .guid(nonMatchingFilterDeviceId)
                .build();

        event.getIncoming().setChannel("_echo");

        Future<List<Event>> eventFuture = subject.execute(event, device);
        assertThat(eventFuture.get(), notNullValue());

        verify(rabbitTemplate, times(1)).convertAndSend(Mockito.anyString(), Mockito.any(Message.class));
    }

    private byte[] toByteArray(Event event) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(event);
        oos.flush();

        return baos.toByteArray();
    }

    @Test
    public void shouldNotSendAnyEventsForANonmatchingRoute() throws Exception {
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
    public void shouldNotSendAnyEventsForANonMatchingIncomingDevice() throws Exception {
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
    public void shouldNotSendAnyEventsForANonMatchingIncomingChannel() throws Exception {
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
    public void shouldNotSendAnyEventsForANonActiveRoute() throws Exception {
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
    public void shouldNotSendAnyEventsForMalformedExpressionFilter() throws Exception {
        Device device = Device.builder()
                .tenant(tenant)
                .application(application)
                .guid(malformedRouteDeviceId)
                .build();

        Future<List<Event>> eventFuture = subject.execute(event, device);
        assertThat(eventFuture, notNullValue());
        assertThat(eventFuture.get(), hasSize(0));
    }

    static class EventRouteExecutorTestConfig {
    	@Bean
    	public TenantDailyUsageRepository tenantDailyUsageRepository() {
    		return Mockito.mock(TenantDailyUsageRepository.class);
    	}
    	
    	@Bean
    	public JavaMailSender javaMailSender() {
    		return Mockito.mock(JavaMailSender.class);
    	}
    	
    	@Bean
    	public SpringTemplateEngine springTemplateEngine() {
    		return Mockito.mock(SpringTemplateEngine.class);
    	}

    }

}
