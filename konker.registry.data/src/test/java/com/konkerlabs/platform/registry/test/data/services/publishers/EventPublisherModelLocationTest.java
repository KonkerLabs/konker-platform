package com.konkerlabs.platform.registry.test.data.services.publishers;

import com.konkerlabs.platform.registry.billing.repositories.TenantDailyUsageRepository;
import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.model.behaviors.URIDealer;
import com.konkerlabs.platform.registry.business.repositories.ApplicationRepository;
import com.konkerlabs.platform.registry.business.repositories.DeviceModelRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.LocationSearchService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.config.EmailConfig;
import com.konkerlabs.platform.registry.config.EventStorageConfig;
import com.konkerlabs.platform.registry.config.PubServerConfig;
import com.konkerlabs.platform.registry.data.config.RabbitMQConfig;
import com.konkerlabs.platform.registry.data.services.api.DeviceLogEventService;
import com.konkerlabs.platform.registry.data.services.publishers.EventPublisherDevice;
import com.konkerlabs.platform.registry.data.services.publishers.EventPublisherModelLocation;
import com.konkerlabs.platform.registry.data.services.publishers.api.EventPublisher;
import com.konkerlabs.platform.registry.test.data.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.data.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.data.base.MongoTestConfiguration;
import com.konkerlabs.platform.registry.test.data.base.RedisTestConfiguration;
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
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.thymeleaf.spring4.SpringTemplateEngine;

import java.net.URI;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.*;

import static com.konkerlabs.platform.registry.data.services.publishers.EventPublisherModelLocation.DEVICE_MQTT_CHANNEL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        BusinessTestConfiguration.class,
        MongoTestConfiguration.class,
        RedisTestConfiguration.class,
        PubServerConfig.class,
        EventStorageConfig.class,
        EmailConfig.class,
        EventPublisherModelLocationTest.EventPublisherModelLocationTestConfig.class
})
@UsingDataSet(locations = {"/fixtures/tenants.json","/fixtures/devices.json", "/fixtures/applications.json"})
public class EventPublisherModelLocationTest extends BusinessLayerTestSupport {

    private static final String THE_DEVICE_GUID = "7d51c242-81db-11e6-a8c2-0746f010e945";
    private static final String REGISTERED_TENANT_DOMAIN = "konker";
    private static final String REGISTERED_DEVICE_GUID = "7d51c242-81db-11e6-a8c2-0746f010e945";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    @Qualifier("modelLocation")
    private EventPublisher subject;

    @Mock
    private DeviceRegisterService deviceRegisterService;

    @Mock
    private DeviceLogEventService deviceLogEventService;

    @Mock
    private DeviceModelRepository deviceModelRepository;

    @Mock
    private LocationSearchService locationSearchService;

    @Mock
    private EventPublisherDevice eventPublisherDevice;

    private Event event;
    private URI destinationUri;

    private String eventPayload = "{\n" +
            "    \"field\" : \"value\",\n" +
            "    \"count\" : 34,\n" +
            "    \"amount\" : 21.45,\n" +
            "    \"valid\" : true\n" +
            "  }";
    private Map<String, String> data;


    private Tenant tenant;
    private Application application;

    private DeviceModel deviceModel;
    private Location locationBR;
    private Location locationSP;

    private static String INPUT_CHANNEL = "input";
    private static String OUTPUT_CHANNEL = "output";


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        ((EventPublisherModelLocation)subject).setDeviceRegisterService(deviceRegisterService);
        ((EventPublisherModelLocation)subject).setDeviceLogEventService(deviceLogEventService);
        ((EventPublisherModelLocation)subject).setDeviceModelRepository(deviceModelRepository);
        ((EventPublisherModelLocation)subject).setLocationSearchService(locationSearchService);
        ((EventPublisherModelLocation)subject).setEventPublisherDevice(eventPublisherDevice);

        tenant = Tenant.builder()
                 .name("k4vrikyjc8")
                 .domainName("k4vrikyjc8")
                 .build();
        
        application = Application.builder()
                      .name("k4vrikyjc8")
                      .tenant(tenant)
                      .build();

        deviceModel = DeviceModel.builder().guid("x6cxpizoo8").name("x6cxpizoo8").build();
        locationBR  = Location.builder().guid("br").name("BR").build();
        locationSP  = Location.builder().guid("sp").name("SP").build();

        event = Event.builder()
            .incoming(
                    Event.EventActor.builder()
                    .channel(INPUT_CHANNEL)
                    .deviceGuid("device_guid").build()
            )
            .payload(eventPayload)
            .creationTimestamp(Instant.now()).build();

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
        Mockito.reset(deviceModelRepository);
        Mockito.reset(locationSearchService);
        Mockito.reset(eventPublisherDevice);
        Mockito.reset(rabbitTemplate);
    }

    @Test
    public void shouldRaiseAnExceptionIfEventIsNull() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Event cannot be null");

        subject.send(null,destinationUri,data,tenant,application);
    }

    @Test
    public void shouldRaiseAnExceptionIfURIIsNull() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Destination URI cannot be null or empty");

        subject.send(event,null,data,tenant,application);
    }

    @Test
    public void shouldRaiseAnExceptionIfDataIsNull() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Data cannot be null");

        subject.send(event,destinationUri,null,tenant,application);
    }

    @Test
    public void shouldRaiseAnExceptionIfMqttChannelIsNull() throws Exception {
        data.remove(DEVICE_MQTT_CHANNEL);

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("A valid MQTT channel is required");

        subject.send(event,destinationUri,data,tenant,application);
    }

    @Test
    public void shouldRaiseAnExceptionIfMqttChannelIsEmpty() throws Exception {
        data.put(DEVICE_MQTT_CHANNEL,"");

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("A valid MQTT channel is required");

        subject.send(event,destinationUri,data,tenant,application);
    }

    @Test
    public void shouldRaiseAnExceptionIfTenantIsNull() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Tenant cannot be null");

        subject.send(event,destinationUri,data,null,null);
    }

    @Test
    public void shouldSendMessages() throws Exception {

        List<Location> children = new ArrayList<>();
        children.add(locationSP);
        locationBR.setChildren(children);

        List<Device> devices = new ArrayList<>();

        devices.add(Device.builder().deviceModel(deviceModel).location(locationBR).active(true).build());
        devices.add(Device.builder().deviceModel(deviceModel).location(locationBR).active(true).build());
        // inactive device
        devices.add(Device.builder().deviceModel(deviceModel).location(locationBR).active(false).build());
        // same device
        devices.add(Device.builder().deviceModel(deviceModel).location(locationBR).guid(event.getIncoming().getDeviceGuid()).active(true).build());
        // other location
        devices.add(Device.builder().deviceModel(deviceModel).location(Location.builder().guid("jp").name("JP").build()).active(true).build());
        // other model
        devices.add(Device.builder().deviceModel(DeviceModel.builder().guid("dqfAnF7b").name("dqfAnF7b").build()).location(locationBR).active(true).build());

        when(
                deviceModelRepository.findByTenantIdApplicationNameAndGuid(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())
        ).thenReturn(
                deviceModel
        );

        when(
                locationSearchService.findByGuid(Mockito.any(Tenant.class), Mockito.any(Application.class), Mockito.anyString())
        ).thenReturn(
                ServiceResponseBuilder.<Location>ok().withResult(locationBR).build()
        );

        when(
                locationSearchService.findByName(Mockito.any(Tenant.class), Mockito.any(Application.class), Mockito.anyString(), Mockito.anyBoolean())
        ).thenReturn(
                ServiceResponseBuilder.<Location>ok().withResult(locationBR).build()
        );

        when(
                deviceRegisterService.findAll(Mockito.any(Tenant.class), Mockito.any(Application.class))
        ).thenReturn(
                ServiceResponseBuilder.<List<Device>>ok().withResult(devices).build()
        );

        destinationUri = new URIDealer() {
            @Override
            public String getUriScheme() {
                return DeviceModelLocation.URI_SCHEME;
            }

            @Override
            public String getContext() {
                return REGISTERED_TENANT_DOMAIN;
            }

            @Override
            public String getGuid() {
                return deviceModel.getGuid() + "/" + locationBR.getGuid();
            }
        }.toURI();

        subject.send(event,destinationUri,data,tenant,application);

        verify(eventPublisherDevice, Mockito.times(2)).sendMessage(Mockito.any(Event.class), Mockito.any(Map.class), Mockito.any(Device.class));
    }
    
    static class EventPublisherModelLocationTestConfig {
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