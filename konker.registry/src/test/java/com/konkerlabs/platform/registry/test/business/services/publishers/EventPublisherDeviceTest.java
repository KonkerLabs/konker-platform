package com.konkerlabs.platform.registry.test.business.services.publishers;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.behaviors.DeviceURIDealer;
import com.konkerlabs.platform.registry.business.model.behaviors.URIDealer;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.business.services.publishers.EventPublisherDevice;
import com.konkerlabs.platform.registry.business.services.publishers.api.EventPublisher;
import com.konkerlabs.platform.registry.config.PubServerConfig;
import com.konkerlabs.platform.registry.integration.gateways.MqttMessageGateway;
import com.konkerlabs.platform.registry.test.base.*;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.konkerlabs.platform.registry.business.services.publishers.EventPublisherDevice.DEVICE_MQTT_CHANNEL;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        MongoTestConfiguration.class,
        BusinessTestConfiguration.class,
        SolrTestConfiguration.class,
        RedisTestConfiguration.class,
        PubServerConfig.class
})
@UsingDataSet(locations = {"/fixtures/tenants.json","/fixtures/devices.json"})
public class EventPublisherDeviceTest extends BusinessLayerTestSupport {

    private static final String THE_DEVICE_GUID = "7d51c242-81db-11e6-a8c2-0746f010e945";
    private static final String REGISTERED_TENANT_DOMAIN = "konker";
    private static final String REGISTERED_DEVICE_GUID = "7d51c242-81db-11e6-a8c2-0746f010e945";

    private static final String MQTT_OUTGOING_TOPIC_TEMPLATE = "sub/{0}/{1}";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private DeviceRegisterService deviceRegisterService;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private MqttMessageGateway mqttMessageGateway;

    @Autowired
    @Qualifier("device")
    private EventPublisher subject;

    @Mock
    private DeviceEventService deviceEventService;

    private Event event;
    private URI destinationUri;

    private String eventPayload = "{\n" +
            "    \"field\" : \"value\",\n" +
            "    \"count\" : 34,\n" +
            "    \"amount\" : 21.45,\n" +
            "    \"valid\" : true\n" +
            "  }";
    private Map<String, String> data;
    private Device device;
    
    private static String INPUT_CHANNEL = "input";
    private static String OUTPUT_CHANNEL = "output";
    

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        ((EventPublisherDevice)subject).setDeviceEventService(deviceEventService);

        device = deviceRegisterService.findByTenantDomainNameAndDeviceGuid(REGISTERED_TENANT_DOMAIN,REGISTERED_DEVICE_GUID);
        event = Event.builder()
            .incoming(
                    Event.EventActor.builder()
                    .channel(INPUT_CHANNEL)
                    .deviceGuid(device.getGuid()).build()
            )
            .payload(eventPayload)
            .timestamp(Instant.now()).build();

        destinationUri = new URIDealer() {
            @Override
            public String getUriScheme() {
                return Device.URI_SCHEME;
            }

            @Override
            public String getContext() {
                return REGISTERED_TENANT_DOMAIN;
            }

            @Override
            public String getGuid() {
                return REGISTERED_DEVICE_GUID;
            }
        }.toURI();

        data = new HashMap<String,String>() {{
            put(DEVICE_MQTT_CHANNEL, OUTPUT_CHANNEL);
        }};
    }

    @After
    public void tearDown() throws Exception {
        Mockito.reset(deviceEventService);
    }

    @Test
    public void shouldRaiseAnExceptionIfEventIsNull() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Event cannot be null");

        subject.send(null,destinationUri,data,device.getTenant());
    }

    @Test
    public void shouldRaiseAnExceptionIfURIIsNull() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Destination URI cannot be null or empty");

        subject.send(event,null,data,device.getTenant());
    }

    @Test
    public void shouldRaiseAnExceptionIfURIIsEmpty() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Destination URI cannot be null or empty");

        subject.send(event,new URI(null,null,null,null,null),data,device.getTenant());
    }

    @Test
    public void shouldRaiseAnExceptionIfDataIsNull() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Data cannot be null");

        subject.send(event,destinationUri,null,device.getTenant());
    }

    @Test
    public void shouldRaiseAnExceptionIfMqttChannelIsNull() throws Exception {
        data.remove(DEVICE_MQTT_CHANNEL);

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("A valid MQTT channel is required");

        subject.send(event,destinationUri,data,device.getTenant());
    }

    @Test
    public void shouldRaiseAnExceptionIfMqttChannelIsEmpty() throws Exception {
        data.put(DEVICE_MQTT_CHANNEL,"");

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("A valid MQTT channel is required");

        subject.send(event,destinationUri,data,device.getTenant());
    }

    @Test
    public void shouldRaiseAnExceptionIfTenantIsNull() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Tenant cannot be null");

        subject.send(event,destinationUri,data,null);
    }

    @Test
    public void shouldRaiseAnExceptionIfDeviceIsUnknown() throws Exception {
        destinationUri = new URIDealer(

        ) {
            @Override
            public String getUriScheme() {
                return Device.URI_SCHEME;
            }

            @Override
            public String getContext() {
                return REGISTERED_TENANT_DOMAIN;
            }

            @Override
            public String getGuid() {
                return "unknown_device";
            }
        }.toURI();


        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(
            MessageFormat.format("Device is unknown : {0}", destinationUri.getPath())
        );

        subject.send(event,destinationUri,data,device.getTenant());
    }

    @Test
    public void shouldNotSendAnyEventThroughGatewayIfDeviceIsDisabled() throws Exception {
        Tenant tenant = tenantRepository.findByName("Konker");
        
        Optional.of(deviceRegisterService.findByTenantDomainNameAndDeviceGuid(REGISTERED_TENANT_DOMAIN,REGISTERED_DEVICE_GUID))
            .filter(device -> !device.isActive())
            .orElseGet(() -> deviceRegisterService.switchEnabledDisabled(tenant, THE_DEVICE_GUID).getResult());

        subject.send(event,destinationUri,data,device.getTenant());

        verify(mqttMessageGateway,never()).send(anyString(),anyString());
        verify(deviceEventService,never()).logIncomingEvent(Mockito.any() , Mockito.any());
        verify(deviceEventService,never()).logOutgoingEvent(Mockito.any() , Mockito.any());
    }

    @Test
    public void shouldSendAnEventThroughGatewayIfDeviceIsEnabled() throws Exception {
        when(deviceEventService.logOutgoingEvent(Mockito.any(Device.class), Mockito.any(Event.class))).thenReturn(
                ServiceResponseBuilder.<Event>ok().build()
        );

        Tenant tenant = tenantRepository.findByName("Konker");
        
        Optional.of(deviceRegisterService.findByTenantDomainNameAndDeviceGuid(REGISTERED_TENANT_DOMAIN,REGISTERED_DEVICE_GUID))
                .filter(Device::isActive)
                .orElseGet(() -> deviceRegisterService.switchEnabledDisabled(tenant, THE_DEVICE_GUID).getResult());

        device = deviceRegisterService.findByTenantDomainNameAndDeviceGuid(REGISTERED_TENANT_DOMAIN,REGISTERED_DEVICE_GUID);

        String expectedMqttTopic = MessageFormat
            .format(MQTT_OUTGOING_TOPIC_TEMPLATE, device.getApiKey(),
                    data.get(DEVICE_MQTT_CHANNEL));

        assertThat(event.getIncoming().getChannel(), equalTo(INPUT_CHANNEL));
        subject.send(event,destinationUri,data,device.getTenant());

        InOrder inOrder = inOrder(mqttMessageGateway,deviceEventService);

        inOrder.verify(mqttMessageGateway).send(event.getPayload(),expectedMqttTopic);
        inOrder.verify(deviceEventService).logOutgoingEvent(eq(device), eq(event));
    }
}