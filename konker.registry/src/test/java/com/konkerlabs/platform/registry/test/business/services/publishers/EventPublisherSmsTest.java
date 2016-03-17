package com.konkerlabs.platform.registry.test.business.services.publishers;

import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.behaviors.SmsDestinationURIDealer;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.repositories.solr.EventRepository;
import com.konkerlabs.platform.registry.business.services.publishers.api.EventPublisher;
import com.konkerlabs.platform.registry.business.services.publishers.EventPublisherSms;
import com.konkerlabs.platform.registry.integration.exceptions.IntegrationException;
import com.konkerlabs.platform.registry.integration.gateways.SMSMessageGateway;
import com.konkerlabs.platform.registry.test.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;
import com.konkerlabs.platform.registry.test.base.SolrTestConfiguration;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;
import java.time.Instant;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        MongoTestConfiguration.class,
        BusinessTestConfiguration.class,
        SolrTestConfiguration.class
})
@UsingDataSet(locations = {"/fixtures/tenants.json"})
public class EventPublisherSmsTest extends BusinessLayerTestSupport {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private String smsPhoneNumber;
    private URI destinationUri;

    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    @Qualifier("sms")
    private EventPublisher subject;

    @Mock
    private EventRepository eventRepository;

    @Autowired
    private SMSMessageGateway smsMessageGateway;

    private Tenant tenant;

    private String eventPayload = "{\n" +
            "    \"field\" : \"value\",\n" +
            "    \"count\" : 34,\n" +
            "    \"amount\" : 21.45,\n" +
            "    \"valid\" : true\n" +
            "  }";
    private Event event;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        EventPublisherSms.class.cast(subject).setEventRepository(eventRepository);
        tenant = tenantRepository.findByDomainName("konker");

        smsPhoneNumber = "+5511987654321";
        destinationUri = new SmsDestinationURIDealer() {}.toSmsURI(tenant.getDomainName(), smsPhoneNumber);

        event = Event.builder()
                .channel("channel")
                .payload(eventPayload)
                .timestamp(Instant.now()).build();
    }

    @After
    public void tearDown() {
        Mockito.reset(eventRepository,smsMessageGateway);
    }

    @Test
    public void shouldRaiseAnExceptionIfEventIsNull() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Event cannot be null");

        subject.send(null,destinationUri,null,tenant);
    }

    @Test
    public void shouldRaiseAnExceptionIfURIIsNull() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Destination URI cannot be null or empty");

        subject.send(event,null,null,tenant);
    }

    @Test
    public void shouldRaiseAnExceptionIfURIIsEmpty() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Destination URI cannot be null or empty");

        subject.send(event,new URI(null,null,null,null,null),null,tenant);
    }

    @Test
    public void shouldRaiseAnExceptionIfTenantIsNull() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Tenant cannot be null");

        subject.send(event,destinationUri,null,null);
    }

    @Test
    public void shouldSendEventThroughGateway() throws Exception {
        String expectedMessage = "You have received a message from Konker device: " + event.getPayload();

        subject.send(event,destinationUri,null,tenant);

        InOrder inOrder = Mockito.inOrder(eventRepository,smsMessageGateway);

        inOrder.verify(smsMessageGateway).send(eq(expectedMessage),eq(smsPhoneNumber));
        inOrder.verify(eventRepository).push(tenant,event);
    }

    @Test
    public void shouldNotLogEventThroughGatewayIfItCouldNotBeForwarded() throws Exception {
        doThrow(IntegrationException.class).when(smsMessageGateway).send(anyString(),anyString());

        subject.send(event,destinationUri,null,tenant);

        Mockito.verify(eventRepository,never()).push(any(Tenant.class),any(Event.class));
    }
}