package com.konkerlabs.platform.registry.test.data.services.publishers;

import static info.solidsoft.mockito.java8.LambdaMatcher.argLambda;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.net.URI;
import java.text.MessageFormat;
import java.time.Instant;

import com.konkerlabs.platform.registry.business.repositories.events.api.EventRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.RestDestination;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.behaviors.RESTDestinationURIDealer;
import com.konkerlabs.platform.registry.business.model.behaviors.URIDealer;
import com.konkerlabs.platform.registry.business.repositories.ApplicationRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.RestDestinationService;
import com.konkerlabs.platform.registry.data.services.publishers.api.EventPublisher;
import com.konkerlabs.platform.registry.integration.gateways.HttpGateway;
import com.konkerlabs.platform.registry.test.data.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.data.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.data.base.MongoTestConfiguration;
import com.konkerlabs.platform.registry.test.data.base.RedisTestConfiguration;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        MongoTestConfiguration.class,
        BusinessTestConfiguration.class,
        RedisTestConfiguration.class
})
@UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/rest-destinations.json"})
public class EventPublisherRestTest extends BusinessLayerTestSupport {

    private static final String REGISTERED_AND_ACTIVE_DESTINATION_GUID = "dda64780-eb81-11e5-958b-a73dab8b32ee";
    private static final String REGISTERED_AND_INACTIVE_DESTINATION_GUID = "e6d8e466-eb81-11e5-8a7c-eb0c7d7c235c";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private RestDestinationService destinationService;
    @Autowired
    @Qualifier(RESTDestinationURIDealer.REST_DESTINATION_URI_SCHEME)
    private EventPublisher subject;

    @Mock
    private EventRepository eventRepository;
    @Autowired
    private HttpGateway httpGateway;

    private Tenant tenant;
    private Application application;

    private final String invalidEventPayload = "{\n" +
            "    \"field\" : \"value\"\n" +
            "    \"count\" : 34,2,\n" +
            "    \"amount\" : 21.45.1,\n" +
            "    \"valid\" : tru\n" +
            "";

    private final String validEventPayload = "{\n" +
            "    \"field\" : \"value\",\n" +
            "    \"count\" : 34,\n" +
            "    \"amount\" : 21.45,\n" +
            "    \"valid\" : true\n" +
            "  }";

    private Event event;
    private RestDestination destination;
    private URI destinationUri;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        tenant = tenantRepository.findByDomainName("konker");
        application = applicationRepository.findByTenantAndName(tenant.getId(), "konker");
        destination = destinationService.getByGUID(tenant, application, REGISTERED_AND_ACTIVE_DESTINATION_GUID).getResult();

        destinationUri = new URIDealer() {
            @Override
            public String getUriScheme() {
                return RestDestination.URI_SCHEME;
            }

            @Override
            public String getContext() {
                return tenant.getDomainName();
            }

            @Override
            public String getGuid() {
                return destination.getGuid();
            }
        }.toURI();

        event = Event.builder()
//                .channel(DEVICE_MQTT_CHANNEL)
                .payload(validEventPayload)
                .creationTimestamp(Instant.now()).build();
    }

    @After
    public void tearDown() {
        Mockito.reset(eventRepository, httpGateway);
    }

    @Test
    public void shouldRaiseAnExceptionIfEventIsNull() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Event cannot be null");

        subject.send(null, destinationUri, null, tenant, application);
    }

    @Test
    public void shouldRaiseAnExceptionIfURIIsNull() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Destination URI cannot be null or empty");

        subject.send(event, null, null, tenant, application);
    }

    @Test
    public void shouldRaiseAnExceptionIfURIIsEmpty() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Destination URI cannot be null or empty");

        subject.send(event, new URI(null, null, null, null, null), null, tenant, application);
    }

    @Test
    public void shouldRaiseAnExceptionIfTenantIsNull() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Tenant cannot be null");

        subject.send(event, destinationUri, null, null, null);
    }

    @Test
    public void shouldRaiseAnExceptionIfDestinationIsUnknown() {
        destinationUri = new URIDealer() {
            @Override
            public String getUriScheme() {
                return RestDestination.URI_SCHEME;
            }

            @Override
            public String getContext() {
                return tenant.getDomainName();
            }

            @Override
            public String getGuid() {
                return "unknown_guid";
            }
        }.toURI();

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(
                MessageFormat.format("REST Destination is unknown : {0}", destinationUri)
        );

        subject.send(event, destinationUri, null, tenant, application);
    }

    @Test
    public void shouldNotSendAnyEventThroughGatewayIfDestinationIsDisabled() throws Exception {
        destinationUri = new URIDealer() {
            @Override
            public String getUriScheme() {
                return RestDestination.URI_SCHEME;
            }

            @Override
            public String getContext() {
                return tenant.getDomainName();
            }

            @Override
            public String getGuid() {
                return REGISTERED_AND_INACTIVE_DESTINATION_GUID;
            }
        }.toURI();

        subject.send(event, destinationUri, null, tenant, application);

        verify(httpGateway, never()).request(any(), any(), any(), any(), any(), any(), any());
        verify(eventRepository, never()).saveIncoming(tenant, application, event);
    }

    @Test
    public void shouldNotSendAnyEventThroughGatewayIfPayloadParsingFails() throws Exception {
        event.setPayload(invalidEventPayload);

        subject.send(event, destinationUri, null, tenant, application);

        verify(httpGateway, never()).request(any(), any(), any(), any(), any(), any(), any());
        verify(eventRepository, never()).saveIncoming(tenant, application, event);
    }

    @Test
    public void shouldSendAnEventThroughGatewayIfDestinationIsEnabled() throws Exception {
        subject.send(event, destinationUri, null, tenant, application);

        InOrder inOrder = Mockito.inOrder(eventRepository, httpGateway);

        inOrder.verify(httpGateway).request(
                eq(HttpMethod.POST),
                eq(new HttpHeaders()),
                eq(URI.create(destination.getServiceURI().replaceAll("\\@\\{.*}", "value"))),
                eq(MediaType.APPLICATION_JSON),
                argLambda(objectSupplier -> objectSupplier.get().equals(event.getPayload())),
                eq(destination.getServiceUsername()),
                eq(destination.getServicePassword())
        );
//        inOrder.verify(eventRepository).saveIncoming(eq(tenant),eq(event));
    }
}