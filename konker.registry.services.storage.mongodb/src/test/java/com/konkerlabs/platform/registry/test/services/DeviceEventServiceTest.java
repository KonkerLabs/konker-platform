package com.konkerlabs.platform.registry.test.services;

import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.ApplicationRepository;
import com.konkerlabs.platform.registry.business.repositories.DeviceRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.repositories.UserRepository;
import com.konkerlabs.platform.registry.business.repositories.events.api.EventRepository;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.config.EventStorageConfig;
import com.konkerlabs.platform.registry.config.PubServerConfig;
import com.konkerlabs.platform.registry.test.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.List;

import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.hasErrorMessage;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        MongoTestConfiguration.class,
        BusinessTestConfiguration.class,
        PubServerConfig.class, EventStorageConfig.class
})
@UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json"})
public class DeviceEventServiceTest extends BusinessLayerTestSupport {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private DeviceEventService deviceEventService;

    @Autowired
    private DeviceRepository deviceRepository;
    
    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    @Qualifier("mongoEvents")
    private EventRepository eventRepository;

    private String userDefinedDeviceGuid = "7d51c242-81db-11e6-a8c2-0746f010e945";
    private String guid = "71fc0d48-674a-4d62-b3e5-0216abca63af";
    private String apiKey = "84399b2e-d99e-11e5-86bc-34238775bac9";
    private String payload = "{\n" +
            "    \"ts\" : \"2016-03-03T18:15:00Z\",\n" +
            "    \"value\" : 31.0,\n" +
            "    \"command\" : {\n" +
            "      \"type\" : \"ButtonPressed\"\n" +
            "      },\n" +
            "    \"data\" : {\n" +
            "      \"channels\" : [\n" +
            "        { \"name\" : \"channel_0\" }\n" +
            "      ]\n" +
            "    },\n" +
            "    \"time\" : 123\n" +
            "  }";
    private String channel = "data";
    private String topic = MessageFormat.format("iot/{0}/{1}", apiKey, channel);
    private Event event;
    private Device device;
    private Tenant tenant;
    private Application application;
    private Application smartffApplication;
    private User user;
    private User userApplication;
    private User userLocation;
    private Instant firstEventTimestamp;
    private Instant lastEventTimestamp;

    @Before
    public void setUp() throws Exception {
        firstEventTimestamp = Instant.ofEpochMilli(1474562670340L);
        lastEventTimestamp = Instant.ofEpochMilli(1474562674450L);

        tenant = tenantRepository.findByDomainName("konker");
        device = deviceRepository.findByTenantAndGuid(tenant.getId(), userDefinedDeviceGuid);
        application = applicationRepository.findByTenantAndName(tenant.getId(), "konker");
        smartffApplication = applicationRepository.findByTenantAndName(tenant.getId(), "smartffkonker");
        user = userRepository.findAllByTenantIdAndEmail(tenant.getId(), "admin@konkerlabs.com");
        userApplication = userRepository.findAllByTenantIdAndEmail(tenant.getId(), "user.application@konkerlabs.com");
        userLocation = userRepository.findAllByTenantIdAndEmail(tenant.getId(), "user.location@konkerlabs.com");
        event = Event.builder()
                .incoming(
                        Event.EventActor.builder()
                                .channel(channel)
                                .deviceGuid(device.getGuid()).build()
                ).payload(payload).build();
    }

    @Test
    public void shouldReturnAnErrorMessageIfTenantIsNullWhenFindingBy() throws Exception {

        ServiceResponse<List<Event>> serviceResponse = deviceEventService.findIncomingBy(
                null,
                null,
                device.getId(),
                channel,
                firstEventTimestamp,
                null,
                false,
                null
        );

        assertThat(serviceResponse, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
    }
   
    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json","/fixtures/devices.json","/fixtures/deviceEvents.json","/fixtures/applications.json"})
    public void shouldReturnAnErrorMessageIfStartInstantIsNullAndLimitIsNullWhenFindingBy() throws Exception {

        ServiceResponse<List<Event>> serviceResponse = deviceEventService.findIncomingBy(
                tenant,
                application,
                device.getId(),
                channel,
                null,
                null,
                false,
                null
        );

        assertThat(serviceResponse, hasErrorMessage(DeviceEventService.Validations.LIMIT_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json","/fixtures/devices.json","/fixtures/deviceEvents.json","/fixtures/applications.json"})
    public void shouldFindAllRequestEvents() throws Exception {
        ServiceResponse<List<Event>> serviceResponse = deviceEventService.findIncomingBy(
                tenant,
                application,
                device.getGuid(),
                "command",
                firstEventTimestamp,
                null,
                false,
                null
        );

        assertThat(serviceResponse.getResult(),notNullValue());
        assertThat(serviceResponse.getResult(),hasSize(3));

        assertThat(serviceResponse.getResult().get(0).getCreationTimestamp().toEpochMilli(),
                equalTo(lastEventTimestamp.toEpochMilli()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json","/fixtures/devices.json","/fixtures/deviceEvents.json","/fixtures/applications.json","/fixtures/users.json"})
    public void shouldReturnAnErrorMessageIfTenantIsNullWhenFindingByWithUser() throws Exception {

        ServiceResponse<List<Event>> serviceResponse = deviceEventService.findIncomingBy(
                null,
                null,
                user,
                device.getId(),
                channel,
                firstEventTimestamp,
                null,
                false,
                null
        );

        assertThat(serviceResponse, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json","/fixtures/devices.json","/fixtures/deviceEvents.json","/fixtures/applications.json","/fixtures/users.json"})
    public void shouldReturnAnErrorMessageIfStartInstantIsNullAndLimitIsNullWhenFindingByWithUser() throws Exception {

        ServiceResponse<List<Event>> serviceResponse = deviceEventService.findIncomingBy(
                tenant,
                application,
                user,
                device.getId(),
                channel,
                null,
                null,
                false,
                null
        );

        assertThat(serviceResponse, hasErrorMessage(DeviceEventService.Validations.LIMIT_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json","/fixtures/devices.json","/fixtures/deviceEvents.json","/fixtures/applications.json","/fixtures/users.json"})
    public void shouldReturnAnErrorIfUserHasNoAccessToApplication() throws Exception {
        ServiceResponse<List<Event>> serviceResponse = deviceEventService.findIncomingBy(
                tenant,
                application,
                userApplication,
                device.getGuid(),
                "command",
                firstEventTimestamp,
                null,
                false,
                null
        );

        assertThat(serviceResponse, hasErrorMessage(ApplicationService.Validations.APPLICATION_HAS_NO_PERMISSION.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json",
                               "/fixtures/devices.json",
                               "/fixtures/deviceEvents.json",
                               "/fixtures/applications.json",
                               "/fixtures/users.json",
                               "/fixtures/events-incoming.json" })
    public void shouldFindAllRequestEventsWithUserApplication() throws Exception {
        ServiceResponse<List<Event>> serviceResponse = deviceEventService.findIncomingBy(
                tenant,
                smartffApplication,
                userApplication,
                null,
                null,
                null,
                null,
                false,
                10
        );

        assertThat(serviceResponse.getResult(),notNullValue());
        assertThat(serviceResponse.getResult(),hasSize(2));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json",
            "/fixtures/devices.json",
            "/fixtures/locations.json",
            "/fixtures/deviceEvents.json",
            "/fixtures/applications.json",
            "/fixtures/users.json",
            "/fixtures/events-incoming.json" })
    public void shouldFindAllRequestEventsWithUserLocation() throws Exception {
        ServiceResponse<List<Event>> serviceResponse = deviceEventService.findIncomingBy(
                tenant,
                smartffApplication,
                userLocation,
                null,
                null,
                null,
                null,
                false,
                10
        );

        assertThat(serviceResponse.getResult(),notNullValue());
        assertThat(serviceResponse.getResult(),hasSize(1));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json","/fixtures/devices.json","/fixtures/deviceEvents.json","/fixtures/applications.json","/fixtures/users.json"})
    public void shouldFindAllRequestEventsWithUser() throws Exception {
        ServiceResponse<List<Event>> serviceResponse = deviceEventService.findIncomingBy(
                tenant,
                application,
                user,
                device.getGuid(),
                "command",
                firstEventTimestamp,
                null,
                false,
                null
        );

        assertThat(serviceResponse.getResult(),notNullValue());
        assertThat(serviceResponse.getResult(),hasSize(3));

        assertThat(serviceResponse.getResult().get(0).getCreationTimestamp().toEpochMilli(),
                equalTo(lastEventTimestamp.toEpochMilli()));
    }

    @Test
    public void shouldReturnAnErrorMessageIfTenantIsNullWhenFindingByOutgoing() throws Exception {

        ServiceResponse<List<Event>> serviceResponse = deviceEventService.findOutgoingBy(
                null,
                null,
                device.getId(),
                channel,
                firstEventTimestamp,
                null,
                false,
                null
        );

        assertThat(serviceResponse, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json","/fixtures/devices.json","/fixtures/deviceEvents.json","/fixtures/applications.json"})
    public void shouldReturnAnErrorMessageIfStartInstantIsNullAndLimitIsNullWhenFindingByOutgoing() throws Exception {

        ServiceResponse<List<Event>> serviceResponse = deviceEventService.findOutgoingBy(
                tenant,
                application,
                device.getId(),
                channel,
                null,
                null,
                false,
                null
        );

        assertThat(serviceResponse, hasErrorMessage(DeviceEventService.Validations.LIMIT_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json",
                               "/fixtures/devices.json",
                               "/fixtures/deviceEvents.json",
                               "/fixtures/applications.json",
                               "/fixtures/events-outgoing.json"})
    public void shouldFindAllRequestEventsOutgoing() throws Exception {
        ServiceResponse<List<Event>> serviceResponse = deviceEventService.findOutgoingBy(
                tenant,
                application,
                device.getGuid(),
                "e4399b2ed998.testchannel",
                null,
                null,
                false,
                10
        );

        assertThat(serviceResponse.getResult(),notNullValue());
        assertThat(serviceResponse.getResult(),hasSize(2));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json",
                               "/fixtures/devices.json",
                               "/fixtures/deviceEvents.json",
                               "/fixtures/applications.json",
                               "/fixtures/users.json"})
    public void shouldReturnAnErrorMessageIfTenantIsNullWhenFindingByOutgoingWithUser() throws Exception {

        ServiceResponse<List<Event>> serviceResponse = deviceEventService.findOutgoingBy(
                null,
                null,
                user,
                device.getId(),
                channel,
                firstEventTimestamp,
                null,
                false,
                null
        );

        assertThat(serviceResponse, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json",
                               "/fixtures/devices.json",
                               "/fixtures/deviceEvents.json",
                               "/fixtures/applications.json",
                               "/fixtures/users.json"})
    public void shouldReturnAnErrorMessageIfStartInstantIsNullAndLimitIsNullWhenFindingByOutgoingWithUser() throws Exception {

        ServiceResponse<List<Event>> serviceResponse = deviceEventService.findOutgoingBy(
                tenant,
                application,
                user,
                device.getId(),
                channel,
                null,
                null,
                false,
                null
        );

        assertThat(serviceResponse, hasErrorMessage(DeviceEventService.Validations.LIMIT_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json",
                               "/fixtures/devices.json",
                               "/fixtures/deviceEvents.json",
                               "/fixtures/applications.json",
                               "/fixtures/users.json"})
    public void shouldReturnAnErrorIfUserHasNoAccessToApplicationFindingByOutgoing() throws Exception {
        ServiceResponse<List<Event>> serviceResponse = deviceEventService.findOutgoingBy(
                tenant,
                application,
                userApplication,
                device.getGuid(),
                "command",
                firstEventTimestamp,
                null,
                false,
                null
        );

        assertThat(serviceResponse, hasErrorMessage(ApplicationService.Validations.APPLICATION_HAS_NO_PERMISSION.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json",
                               "/fixtures/devices.json",
                               "/fixtures/deviceEvents.json",
                               "/fixtures/applications.json",
                               "/fixtures/users.json",
                               "/fixtures/events-outgoing.json" })
    public void shouldFindAllRequestEventsWithUserApplicationFindingByOutgoing() throws Exception {
        ServiceResponse<List<Event>> serviceResponse = deviceEventService.findOutgoingBy(
                tenant,
                smartffApplication,
                userApplication,
                null,
                null,
                null,
                null,
                false,
                10
        );

        assertThat(serviceResponse.getResult(),notNullValue());
        assertThat(serviceResponse.getResult(),hasSize(2));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json",
                               "/fixtures/devices.json",
                               "/fixtures/locations.json",
                               "/fixtures/deviceEvents.json",
                               "/fixtures/applications.json",
                               "/fixtures/users.json",
                               "/fixtures/events-outgoing.json" })
    public void shouldFindAllRequestEventsWithUserLocationFindingByOutgoing() throws Exception {
        ServiceResponse<List<Event>> serviceResponse = deviceEventService.findOutgoingBy(
                tenant,
                smartffApplication,
                userLocation,
                null,
                null,
                null,
                null,
                false,
                10
        );

        assertThat(serviceResponse.getResult(),notNullValue());
        assertThat(serviceResponse.getResult(),hasSize(1));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json",
                               "/fixtures/devices.json",
                               "/fixtures/locations.json",
                               "/fixtures/deviceEvents.json",
                               "/fixtures/applications.json",
                               "/fixtures/users.json",
                               "/fixtures/events-outgoing.json" })
    public void shouldFindAllRequestEventsWithUserFindingByOutgoing() throws Exception {
        ServiceResponse<List<Event>> serviceResponse = deviceEventService.findOutgoingBy(
                tenant,
                application,
                user,
                device.getGuid(),
                "e4399b2ed998.testchannel",
                null,
                null,
                false,
                10
        );

        assertThat(serviceResponse.getResult(),notNullValue());
        assertThat(serviceResponse.getResult(),hasSize(2));

    }

}