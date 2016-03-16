package com.konkerlabs.platform.registry.test.business.services.publishers;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.behaviors.DeviceURIDealer;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.repositories.solr.EventRepository;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.publishers.api.EventPublisher;
import com.konkerlabs.platform.registry.business.services.publishers.EventPublisherMqtt;
import com.konkerlabs.platform.registry.integration.gateways.MqttMessageGateway;
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
import org.mockito.InOrder;
import org.mockito.Mock;
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
import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
    MongoTestConfiguration.class,
    BusinessTestConfiguration.class,
    SolrTestConfiguration.class
})
@UsingDataSet(locations = {"/fixtures/tenants.json","/fixtures/devices.json"})
public class EventPublisherMqttTest extends BusinessLayerTestSupport {

    private static final String THE_DEVICE_ID = "71fc0d48-674a-4d62-b3e5-0216abca63af";
    private static final String REGISTERED_TENANT_DOMAIN = "konker";
    private static final String REGISTERED_DEVICE_ID = "95c14b36ba2b43f1";

    private static final String MQTT_OUTGOING_TOPIC_TEMPLATE = "iot/{0}/{1}";

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
    private EventRepository eventRepository;

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

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        ((EventPublisherMqtt)subject).setEventRepository(eventRepository);

        device = deviceRegisterService.findByTenantDomainNameAndDeviceId(REGISTERED_TENANT_DOMAIN,REGISTERED_DEVICE_ID);
        event = Event.builder()
            .channel("channel")
            .payload(eventPayload)
            .timestamp(Instant.now()).build();

        destinationUri = new DeviceURIDealer() {}.toDeviceRouteURI(REGISTERED_TENANT_DOMAIN,REGISTERED_DEVICE_ID);

        data = new HashMap<String,String>() {{
            put("channel",event.getChannel());
        }};
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
        data.remove("channel");

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("A valid MQTT channel is required");

        subject.send(event,destinationUri,data,device.getTenant());
    }

    @Test
    public void shouldRaiseAnExceptionIfMqttChannelIsEmpty() throws Exception {
        data.put("channel","");

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
        destinationUri = new DeviceURIDealer() {}.toDeviceRouteURI(REGISTERED_TENANT_DOMAIN,"unknown_device");

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(
            MessageFormat.format("Device is unknown : {0}", destinationUri.getPath())
        );

        subject.send(event,destinationUri,data,device.getTenant());
    }

    @Test
    public void shouldNotSendAnyEventThroughGatewayIfDeviceIsDisabled() throws Exception {
        Tenant tenant = tenantRepository.findByName("Konker");
        
        Optional.of(deviceRegisterService.findByTenantDomainNameAndDeviceId(REGISTERED_TENANT_DOMAIN,REGISTERED_DEVICE_ID))
            .filter(device -> !device.isActive())
            .orElseGet(() -> deviceRegisterService.switchEnabledDisabled(tenant, THE_DEVICE_ID).getResult());

        subject.send(event,destinationUri,data,device.getTenant());

        verify(mqttMessageGateway,never()).send(anyString(),anyString());
        verify(eventRepository,never()).push(device.getTenant(),event);
    }

    @Test
    public void shouldSendAnEventThroughGatewayIfDeviceIsEnabled() throws Exception {
        Tenant tenant = tenantRepository.findByName("Konker");

        Optional.of(deviceRegisterService.findByTenantDomainNameAndDeviceId(REGISTERED_TENANT_DOMAIN,REGISTERED_DEVICE_ID))
                .filter(Device::isActive)
                .orElseGet(() -> deviceRegisterService.switchEnabledDisabled(tenant, THE_DEVICE_ID).getResult());

        String expectedMqttTopic = MessageFormat
            .format(MQTT_OUTGOING_TOPIC_TEMPLATE, destinationUri.getPath().replaceAll("/",""),
                    data.get("channel"));

        subject.send(event,destinationUri,data,device.getTenant());

        InOrder inOrder = inOrder(mqttMessageGateway,eventRepository);

        inOrder.verify(mqttMessageGateway).send(event.getPayload(),expectedMqttTopic);
        inOrder.verify(eventRepository).push(device.getTenant(),event);
    }
}