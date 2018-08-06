package com.konkerlabs.platform.registry.test.data.services.publishers;

import com.amazonaws.services.kinesis.AbstractAmazonKinesis;
import com.amazonaws.services.kinesis.model.PutRecordRequest;
import com.amazonaws.services.kinesis.model.PutRecordResult;
import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.model.behaviors.URIDealer;
import com.konkerlabs.platform.registry.business.repositories.DeviceModelRepository;
import com.konkerlabs.platform.registry.business.services.api.LocationSearchService;
import com.konkerlabs.platform.registry.config.EventStorageConfig;
import com.konkerlabs.platform.registry.config.PubServerConfig;
import com.konkerlabs.platform.registry.data.services.api.DeviceLogEventService;
import com.konkerlabs.platform.registry.data.services.publishers.EventPublisherAmazonKinesis;
import com.konkerlabs.platform.registry.data.services.publishers.EventPublisherDevice;
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
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;
import java.time.Instant;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        BusinessTestConfiguration.class,
        MongoTestConfiguration.class,
        RedisTestConfiguration.class,
        PubServerConfig.class,
        EventStorageConfig.class
})
@UsingDataSet(locations = {"/fixtures/tenants.json","/fixtures/devices.json", "/fixtures/applications.json"})
public class EventPublisherAmazonKinesisTest extends BusinessLayerTestSupport {

    private static final String REGISTERED_TENANT_DOMAIN = "konker";
    private static final String REGISTERED_DEVICE_GUID = "7d51c242-81db-11e6-a8c2-0746f010e945";

    private static final AmazonKinesis amazonKinesis = AmazonKinesis.builder().build();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    @Qualifier("amazonKinesis")
    private EventPublisher subject;

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

    private final String eventPayload = "{\n" +
            "    \"field\" : \"value\",\n" +
            "    \"count\" : 34,\n" +
            "    \"amount\" : 21.45,\n" +
            "    \"valid\" : true\n" +
            "  }";
    private Map<String, String> data;

    private Tenant tenant;
    private Application application;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        tenant = Tenant.builder()
                 .name("k4vrikyjc8")
                 .domainName("k4vrikyjc8")
                 .id("k4vrikyjc8")
                 .build();
        
        application = Application.builder()
                      .name("k4vrikyjc8")
                      .tenant(tenant)
                      .build();

        event = Event.builder()
            .incoming(
                    Event.EventActor.builder()
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

        amazonKinesis.setKey("key-test");
        amazonKinesis.setSecret("secret-test");
        amazonKinesis.setRegion("region-test");
        amazonKinesis.setStreamName("stream-test");

        data = amazonKinesis.getValues();
    }

    @After
    public void tearDown() {
        Mockito.reset(deviceLogEventService);
        Mockito.reset(deviceModelRepository);
        Mockito.reset(locationSearchService);
        Mockito.reset(eventPublisherDevice);
    }

    @Test
    public void shouldRaiseAnExceptionIfEventIsNull() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Event cannot be null");

        subject.send(null,destinationUri,data,tenant,application);
    }

    @Test
    public void shouldRaiseAnExceptionIfURIIsNull() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Destination URI cannot be null or empty");

        subject.send(event,null,data,tenant,application);
    }

    @Test
    public void shouldRaiseAnExceptionIfDataIsNull() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Data cannot be null");

        subject.send(event,destinationUri,null,tenant,application);
    }

    @Test
    public void shouldRaiseAnExceptionIfTenantIsNull() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Tenant cannot be null");

        subject.send(event,destinationUri,data,null,null);
    }

    @Test
    public void shouldSendMessages() {

        destinationUri = new URIDealer() {
            @Override
            public String getUriScheme() {
                return AmazonKinesis.URI_SCHEME;
            }

            @Override
            public String getContext() {
                return REGISTERED_TENANT_DOMAIN;
            }

            @Override
            public String getGuid() {
                return "guid";
            }
        }.toURI();

        AmazonKinesisClientBuilderMock clientBuilderMock = new AmazonKinesisClientBuilderMock();
        ((EventPublisherAmazonKinesis) subject).setClientBuilder(clientBuilderMock);

        subject.send(event,destinationUri,data,tenant,application);
    }

    public class AmazonKinesisClientBuilderMock implements EventPublisherAmazonKinesis.AmazonKinesisClientBuilder {

        public com.amazonaws.services.kinesis.AmazonKinesis build(AmazonKinesis kinesisProperties) {
            return new AbstractAmazonKinesis() {
                public PutRecordResult putRecord(PutRecordRequest request) {
                    // do nothing
                    return new PutRecordResult();
                }
            };
        }

    }

}