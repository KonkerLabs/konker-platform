package com.konkerlabs.platform.registry.test.data.services.publishers;

import static com.konkerlabs.platform.registry.data.services.publishers.EventPublisherDevice.DEVICE_MQTT_CHANNEL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.konkerlabs.platform.registry.config.EventStorageConfig;

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
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.behaviors.URIDealer;
import com.konkerlabs.platform.registry.business.repositories.ApplicationRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.config.PubServerConfig;
import com.konkerlabs.platform.registry.data.config.RabbitMQConfig;
import com.konkerlabs.platform.registry.data.services.api.DeviceLogEventService;
import com.konkerlabs.platform.registry.data.services.publishers.EventPublisherDevice;
import com.konkerlabs.platform.registry.data.services.publishers.api.EventPublisher;
import com.konkerlabs.platform.registry.test.data.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.data.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.data.base.MongoTestConfiguration;
import com.konkerlabs.platform.registry.test.data.base.RedisTestConfiguration;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        BusinessTestConfiguration.class,
        MongoTestConfiguration.class,
        RedisTestConfiguration.class,
        PubServerConfig.class,
        EventStorageConfig.class
})
@UsingDataSet(locations = {"/fixtures/tenants.json","/fixtures/devices.json", "/fixtures/applications.json"})
public class EventPublisherDeviceTest extends BusinessLayerTestSupport {

    private static final String THE_DEVICE_GUID = "7d51c242-81db-11e6-a8c2-0746f010e945";
    private static final String REGISTERED_TENANT_DOMAIN = "konker";
    private static final String REGISTERED_DEVICE_GUID = "7d51c242-81db-11e6-a8c2-0746f010e945";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private DeviceRegisterService deviceRegisterService;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    @Qualifier("device")
    private EventPublisher subject;

    @Mock
    private DeviceLogEventService deviceLogEventService;

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

        ((EventPublisherDevice)subject).setDeviceLogEventService(deviceLogEventService);

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
        Mockito.reset(deviceLogEventService);
        Mockito.reset(rabbitTemplate);
    }

    @Test
    public void shouldRaiseAnExceptionIfEventIsNull() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Event cannot be null");

        subject.send(null,destinationUri,data,device.getTenant(),device.getApplication());
    }

    @Test
    public void shouldRaiseAnExceptionIfURIIsNull() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Destination URI cannot be null or empty");

        subject.send(event,null,data,device.getTenant(),device.getApplication());
    }

    @Test
    public void shouldRaiseAnExceptionIfURIIsEmpty() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Destination URI cannot be null or empty");

        subject.send(event,new URI(null,null,null,null,null),data,device.getTenant(),device.getApplication());
    }

    @Test
    public void shouldRaiseAnExceptionIfDataIsNull() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Data cannot be null");

        subject.send(event,destinationUri,null,device.getTenant(),device.getApplication());
    }

    @Test
    public void shouldRaiseAnExceptionIfMqttChannelIsNull() throws Exception {
        data.remove(DEVICE_MQTT_CHANNEL);

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("A valid MQTT channel is required");

        subject.send(event,destinationUri,data,device.getTenant(),device.getApplication());
    }

    @Test
    public void shouldRaiseAnExceptionIfMqttChannelIsEmpty() throws Exception {
        data.put(DEVICE_MQTT_CHANNEL,"");

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("A valid MQTT channel is required");

        subject.send(event,destinationUri,data,device.getTenant(),device.getApplication());
    }

    @Test
    public void shouldRaiseAnExceptionIfTenantIsNull() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Tenant cannot be null");

        subject.send(event,destinationUri,data,null,null);
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

        subject.send(event,destinationUri,data,device.getTenant(),device.getApplication());
    }

    @Test
    public void shouldNotSendAnyEventThroughGatewayIfDeviceIsDisabled() throws Exception {
        Tenant tenant = tenantRepository.findByName("Konker");
        Application application = applicationRepository.findByTenantAndName(tenant.getId(), "smartffkonker");

        Optional.of(deviceRegisterService.findByTenantDomainNameAndDeviceGuid(REGISTERED_TENANT_DOMAIN,REGISTERED_DEVICE_GUID))
            .filter(device -> !device.isActive())
            .orElseGet(() -> deviceRegisterService.switchEnabledDisabled(tenant, application, THE_DEVICE_GUID).getResult());

        subject.send(event,destinationUri,data,device.getTenant(),device.getApplication());

        MessageProperties properties = new MessageProperties();
        properties.setHeader(RabbitMQConfig.MSG_HEADER_APIKEY, device.getApiKey());
        properties.setHeader(RabbitMQConfig.MSG_HEADER_CHANNEL, data.get(DEVICE_MQTT_CHANNEL));

        Message message = new Message(event.getPayload().getBytes("UTF-8"), properties);

        verify(rabbitTemplate,never()).convertAndSend("data.sub", message);
        verify(deviceLogEventService,never()).logIncomingEvent(Mockito.any() , Mockito.any());
        verify(deviceLogEventService,never()).logOutgoingEvent(Mockito.any() , Mockito.any());
    }

    @Test
    public void shouldSendAnEventThroughGatewayIfDeviceIsEnabled() throws Exception {
        when(deviceLogEventService.logOutgoingEvent(Mockito.any(Device.class), Mockito.any(Event.class))).thenReturn(
                ServiceResponseBuilder.<Event>ok().build()
        );

        Tenant tenant = tenantRepository.findByName("Konker");
        Application application = applicationRepository.findByTenantAndName(tenant.getId(), "smartffkonker");

        Optional.of(deviceRegisterService.findByTenantDomainNameAndDeviceGuid(REGISTERED_TENANT_DOMAIN,REGISTERED_DEVICE_GUID))
                .filter(Device::isActive)
                .orElseGet(() -> deviceRegisterService.switchEnabledDisabled(tenant, application, THE_DEVICE_GUID).getResult());

        device = deviceRegisterService.findByTenantDomainNameAndDeviceGuid(REGISTERED_TENANT_DOMAIN,REGISTERED_DEVICE_GUID);

        assertThat(event.getIncoming().getChannel(), equalTo(INPUT_CHANNEL));
        subject.send(event,destinationUri,data,device.getTenant(),device.getApplication());

        InOrder inOrder = inOrder(rabbitTemplate, deviceLogEventService);

        MessageProperties properties = new MessageProperties();
        properties.setHeader(RabbitMQConfig.MSG_HEADER_APIKEY, device.getApiKey());
        properties.setHeader(RabbitMQConfig.MSG_HEADER_CHANNEL, data.get(DEVICE_MQTT_CHANNEL));

        Message message = new Message(event.getPayload().getBytes("UTF-8"), properties);

        inOrder.verify(rabbitTemplate).convertAndSend("data.sub", message);
        inOrder.verify(deviceLogEventService).logOutgoingEvent(eq(device), eq(event));
    }

}