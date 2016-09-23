package com.konkerlabs.platform.registry.test.business.services.publishers;

import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.SmsDestination;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.behaviors.RESTDestinationURIDealer;
import com.konkerlabs.platform.registry.business.model.behaviors.SmsDestinationURIDealer;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.repositories.events.EventRepository;
import com.konkerlabs.platform.registry.business.services.api.SmsDestinationService;
import com.konkerlabs.platform.registry.business.services.publishers.EventPublisherSms;
import com.konkerlabs.platform.registry.business.services.publishers.api.EventPublisher;
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
import java.text.MessageFormat;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static com.konkerlabs.platform.registry.business.services.publishers.EventPublisherMqtt.DEVICE_MQTT_CHANNEL;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        MongoTestConfiguration.class,
        BusinessTestConfiguration.class,
        SolrTestConfiguration.class
})
@UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/sms-destinations.json"})
public class EventPublisherSmsTest extends BusinessLayerTestSupport {

    private static final String REGISTERED_AND_ACTIVE_DESTINATION_GUID = "140307f9-7d50-4f37-ac67-80313776bef4";
    private static final String REGISTERED_AND_INACTIVE_DESTINATION_GUID = "0def8df0-9459-49c1-aa9b-82d5bf21d932";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private URI destinationUri;

    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private SmsDestinationService destinationService;
    @Autowired
    @Qualifier("sms")
    private EventPublisher subject;

    @Mock
    private EventRepository eventRepository;

    @Autowired
    private SMSMessageGateway smsMessageGateway;

    private Tenant tenant;

    private String invalidEventPayload = "{\n" +
            "    \"field\" : \"value\"\n" +
            "    \"count\" : 34,2,\n" +
            "    \"amount\" : 21.45.1,\n" +
            "    \"valid\" : tru\n" +
            "";

    private String eventPayload = "{\n" +
            "    \"field\" : \"value\",\n" +
            "    \"count\" : 34,\n" +
            "    \"amount\" : 21.45,\n" +
            "    \"valid\" : true\n" +
            "  }";

    private Event event;
    private SmsDestination destination;
    private Map<String, String> data;
    private String messageTemplate;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        messageTemplate = "Current field value is @{#amount}";

        EventPublisherSms.class.cast(subject).setEventRepository(eventRepository);
        tenant = tenantRepository.findByDomainName("konker");

        data = new HashMap<String,String>();
        data.put(EventPublisherSms.SMS_MESSAGE_STRATEGY_PARAMETER_NAME,
                 EventPublisherSms.SMS_MESSAGE_CUSTOM_STRATEGY_PARAMETER_VALUE);
        data.put(EventPublisherSms.SMS_MESSAGE_TEMPLATE_PARAMETER_NAME,
                 messageTemplate);

        destination = destinationService.getByGUID(tenant, REGISTERED_AND_ACTIVE_DESTINATION_GUID).getResult();
        destinationUri = new SmsDestinationURIDealer() {}.toSmsURI(tenant.getDomainName(), destination.getGuid());

        event = Event.builder()
                .channel(DEVICE_MQTT_CHANNEL)
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
    public void shouldRaiseAnExceptionIfDataIsNull() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Data cannot be null");

        subject.send(event,destinationUri,null,tenant);
    }

    @Test
    public void shouldRaiseAnExceptionIfMessageStrategyParameterIsNull() throws Exception {
        data.remove(EventPublisherSms.SMS_MESSAGE_STRATEGY_PARAMETER_NAME);

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("A SMS message strategy is required");

        subject.send(event,destinationUri,data,tenant);
    }

    @Test
    public void shouldRaiseAnExceptionIfMessageStrategyParameterIsEmpty() throws Exception {
        data.put(EventPublisherSms.SMS_MESSAGE_STRATEGY_PARAMETER_NAME,"");

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("A SMS message strategy is required");

        subject.send(event,destinationUri,data,tenant);
    }

    @Test
    public void shouldRaiseAnExceptionIfMessageTemplateParameterIsNullOnCustomMessageStrategy() throws Exception {
        data.put(EventPublisherSms.SMS_MESSAGE_STRATEGY_PARAMETER_NAME,
                 EventPublisherSms.SMS_MESSAGE_CUSTOM_STRATEGY_PARAMETER_VALUE);
        data.remove(EventPublisherSms.SMS_MESSAGE_TEMPLATE_PARAMETER_NAME);

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("A message template is required on custom strategy");

        subject.send(event,destinationUri,data,tenant);
    }

    @Test
    public void shouldRaiseAnExceptionIfTenantIsNull() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Tenant cannot be null");

        subject.send(event,destinationUri,data,null);
    }

    @Test
    public void shouldRaiseAnExceptionIfDestinationIsUnknown() throws Exception {
        destinationUri = new SmsDestinationURIDealer() {}.toSmsURI(
                tenant.getDomainName(),"unknown_guid"
        );

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(
                MessageFormat.format("SMS Destination is unknown : {0}", destinationUri)
        );

        subject.send(event,destinationUri,data,tenant);
    }

    @Test
    public void shouldNotSendAnyEventThroughGatewayIfDestinationIsDisabled() throws Exception {
        destinationUri = new RESTDestinationURIDealer() {}.toRestDestinationURI(
                tenant.getDomainName(),REGISTERED_AND_INACTIVE_DESTINATION_GUID
        );

        subject.send(event,destinationUri,data,tenant);

        verify(smsMessageGateway,never()).send(anyString(),anyString());
        verify(eventRepository,never()).push(tenant,event);
    }

    @Test
    public void shouldNotSendAnyEventThroughGatewayIfPayloadParsingFails() throws Exception {
        event.setPayload(invalidEventPayload);

        subject.send(event,destinationUri,data,tenant);

        verify(smsMessageGateway,never()).send(anyString(),anyString());
        verify(eventRepository,never()).push(tenant,event);
    }

    @Test
    public void onEnabledDestinationShouldSendInterpolatedTemplateThroughGatewayIfStrategyIsCustom() throws Exception {
        String expectedMessage = messageTemplate.replaceAll("\\@\\{.*}","21.45");

        subject.send(event,destinationUri,data,tenant);

        InOrder inOrder = Mockito.inOrder(eventRepository,smsMessageGateway);

        inOrder.verify(smsMessageGateway).send(eq(expectedMessage),eq(destination.getPhoneNumber()));
        inOrder.verify(eventRepository).push(tenant,event);
    }

    @Test
    public void onEnabledDestinationShouldSendReceivedPayloadThroughGatewayIfStrategyIsForward() throws Exception {
        String expectedMessage = event.getPayload();

        data.put(EventPublisherSms.SMS_MESSAGE_STRATEGY_PARAMETER_NAME,
                 EventPublisherSms.SMS_MESSAGE_FORWARD_STRATEGY_PARAMETER_VALUE);

        subject.send(event,destinationUri,data,tenant);

        InOrder inOrder = Mockito.inOrder(eventRepository,smsMessageGateway);

        inOrder.verify(smsMessageGateway).send(eq(expectedMessage),eq(destination.getPhoneNumber()));
        inOrder.verify(eventRepository).push(tenant,event);
    }

    @Test
    public void shouldNotLogEventThroughGatewayIfItCouldNotBeForwarded() throws Exception {
        doThrow(IntegrationException.class).when(smsMessageGateway).send(anyString(),anyString());

        subject.send(event,destinationUri,data,tenant);

        Mockito.verify(eventRepository,never()).push(any(Tenant.class),any(Event.class));
    }
}