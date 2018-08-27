package com.konkerlabs.platform.registry.test.data.services;

import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.model.EventRoute.RouteActor;
import com.konkerlabs.platform.registry.business.model.behaviors.URIDealer;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.ApplicationRepository;
import com.konkerlabs.platform.registry.business.repositories.DeviceModelRepository;
import com.konkerlabs.platform.registry.business.repositories.EventRouteRepository;
import com.konkerlabs.platform.registry.business.repositories.LocationRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.DeviceModelService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.EventRouteService;
import com.konkerlabs.platform.registry.business.services.api.LocationService;
import com.konkerlabs.platform.registry.business.services.api.RestDestinationService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.EventRouteService.Validations;
import com.konkerlabs.platform.registry.test.data.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.data.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.data.base.MongoTestConfiguration;
import com.konkerlabs.platform.registry.test.data.base.RedisTestConfiguration;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.konkerlabs.platform.registry.business.model.EventRoute.builder;
import static com.konkerlabs.platform.registry.data.services.publishers.EventPublisherDevice.DEVICE_MQTT_CHANNEL;
import static com.konkerlabs.platform.registry.test.data.base.ServiceResponseMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        BusinessTestConfiguration.class,
        MongoTestConfiguration.class,
        RedisTestConfiguration.class
})
public class EventRouteServiceTest extends BusinessLayerTestSupport {

    private static final String TRANSFORMATION_ID_IN_USE = "2747ec73-6910-43a1-8ddc-5a4a134ebab3";
    private static final String DEVICE_URI_FOR_DISPLAY_NAME = "device://konker/f067bfd0-3365-49e9-b7f5-fc5673f869a4";
    private static final String REST_URI_FOR_DISPLAY_NAME = "rest://konker/dda64780-eb81-11e5-958b-a73dab8b32ee";

    @Rule
    public ExpectedException thrown = none();

    @Autowired
    private EventRouteService subject;

    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private EventRouteRepository eventRouteRepository;
    @Autowired
    private DeviceModelRepository deviceModelRepository;
    @Autowired
    private LocationRepository locationRepository;

    private EventRoute route;

    private final String routeId = "01231829-4435-4eb0-abd6-7a7bae7812bd";
    private final String existingGuid = "bd923670-d888-472a-b6d9-b20af31253da";
    private Tenant tenant;
    private Application application;
    private Tenant emptyTenant;
    private Application emptyApplication;
    private Transformation transformation;

    @Before
    public void setUp() {
        tenant = tenantRepository.findByDomainName("konker");
        application = applicationRepository.findByTenantAndName(tenant.getId(), "konker");
        emptyTenant = tenantRepository.findByDomainName("empty");
        emptyApplication = applicationRepository.findByTenantAndName(emptyTenant.getId(), "empty");

        transformation = Transformation.builder().id(TRANSFORMATION_ID_IN_USE).build();

        route = spy(builder()
                .name("Route name")
                .description("Description")
                .incoming(
                        RouteActor.builder().uri(
                                new URIDealer() {
                                    @Override
                                    public String getUriScheme() {
                                        return Device.URI_SCHEME;
                                    }

                                    @Override
                                    public String getContext() {
                                        return tenant.getDomainName();
                                    }

                                    @Override
                                    public String getGuid() {
                                        return "f067bfd0-3365-49e9-b7f5-fc5673f869a4";
                                    }
                                }.toURI()
                        ).data(new HashMap<String, String>() {{
                            put(DEVICE_MQTT_CHANNEL, "data");
                        }}).build())
                .outgoing(
                        RouteActor.builder().uri(
                                new URIDealer() {
                                    @Override
                                    public String getUriScheme() {
                                        return Device.URI_SCHEME;
                                    }

                                    @Override
                                    public String getContext() {
                                        return tenant.getDomainName();
                                    }

                                    @Override
                                    public String getGuid() {
                                        return "dab14680-c079-4c84-a5ce-82659868f370";
                                    }
                                }.toURI()
                        ).data(new HashMap<String, String>() {{
                            put(DEVICE_MQTT_CHANNEL, "in");
                        }}).build())
                .filteringExpression("#command.type == 'ButtonPressed'")
                .transformation(transformation)
                .active(true)
                .build());

    }

    /* ----------------------------- save ------------------------------ */

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/transformations.json"})
    public void shouldReturnValidationMessageTenantIsNull() {
        ServiceResponse<EventRoute> response = subject.save(null, application, route);

        assertThat(response, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/transformations.json"})
    public void shouldReturnErrorMessageIfApplicationIsNullWhenSave() {
        ServiceResponse<EventRoute> response = subject.save(tenant, null, route);
        assertThat(response, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/transformations.json"})
    public void shouldReturnErrorMessageIfApplicationIsInvalidWhenSave() {
        ServiceResponse<EventRoute> response = subject.save(tenant, emptyApplication, route);
        assertThat(response, hasErrorMessage(ApplicationService.Validations.APPLICATION_NOT_FOUND.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/transformations.json"})
    public void shouldReturnValidationMessageIfRecordIsNull() {
        ServiceResponse<EventRoute> response = subject.save(tenant, application, null);

        assertThat(response, hasErrorMessage(CommonValidations.RECORD_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/transformations.json"})
    public void shouldReturnValidationMessageIfRecordIsInvalid() {
        Map<String, Object[]> errorMessages = new HashMap() {{
            put("some_error", new Object[]{"some_value"});
        }};
        when(route.applyValidations()).thenReturn(Optional.of(errorMessages));

        ServiceResponse<EventRoute> response = subject.save(tenant, application, route);

        assertThat(response, hasErrorMessage("some_error", "some_value"));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/transformations.json"})
    public void shouldReturnValidationMessageIfTenantDoesNotExist() {
        ServiceResponse<EventRoute> response = subject.save(Tenant.builder().id("unknown_id").name("name").build(), application, route);

        assertThat(response, hasErrorMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/transformations.json", "/fixtures/event-routes.json"})
    public void shouldReturnAValidationMessageIfRouteNameAlreadyExistsWithinTenant() {
        String existingRouteName = "Device event forwarding route";

        route.setName(existingRouteName);

        ServiceResponse<EventRoute> response = subject.save(tenant, application, route);
        assertThat(response, hasErrorMessage(EventRouteService.Validations.NAME_IN_USE.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/transformations.json", "/fixtures/event-routes.json", "/fixtures/devices.json"})
    public void shouldReturnCrossApplicationMessage() {

        route.setOutgoing(
                        RouteActor.builder().uri(
                                new URIDealer() {
                                    @Override
                                    public String getUriScheme() {
                                        return Device.URI_SCHEME;
                                    }

                                    @Override
                                    public String getContext() {
                                        return tenant.getDomainName();
                                    }

                                    @Override
                                    public String getGuid() {
                                        return "8363c556-84ea-11e6-92a2-4b01fea7e243";
                                    }
                                }.toURI()
                        ).data(new HashMap<String, String>() {{
                            put(DEVICE_MQTT_CHANNEL, "in");
                        }}).build()
                );

        ServiceResponse<EventRoute> response = subject.save(tenant, application, route);

        assertThat(response, hasErrorMessage(EventRouteService.Validations.CROSS_APPLICATION.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/transformations.json", "/fixtures/event-routes.json", "/fixtures/devices.json"})
    public void shouldReturnDeviceNotFoundMessage() {

        route.setIncoming(
                        RouteActor.builder().uri(
                                new URIDealer() {
                                    @Override
                                    public String getUriScheme() {
                                        return Device.URI_SCHEME;
                                    }

                                    @Override
                                    public String getContext() {
                                        return tenant.getDomainName();
                                    }

                                    @Override
                                    public String getGuid() {
                                        return "6834ebf8-3a69-445e-9780-ddc47b0638bd";
                                    }
                                }.toURI()
                        ).data(new HashMap<String, String>() {{
                            put(DEVICE_MQTT_CHANNEL, "in");
                        }}).build()
                );

        ServiceResponse<EventRoute> response = subject.save(tenant, application, route);

        assertThat(response, hasErrorMessage(DeviceRegisterService.Validations.DEVICE_GUID_DOES_NOT_EXIST.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/transformations.json", "/fixtures/event-routes.json", "/fixtures/devices.json"})
    public void shouldReturnRestNotFoundMessage() {

        route.setOutgoing(
                        RouteActor.builder().uri(
                                new URIDealer() {
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
                                        return "6834ebf8-3a69-445e-9780-ddc47b0638bd";
                                    }
                                }.toURI()
                        ).data(new HashMap<String, String>() {{
                            put(DEVICE_MQTT_CHANNEL, "in");
                        }}).build()
                );

        ServiceResponse<EventRoute> response = subject.save(tenant, application, route);

        assertThat(response, hasErrorMessage(RestDestinationService.Validations.DESTINATION_NOT_FOUND.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/transformations.json", "/fixtures/event-routes.json", "/fixtures/devices.json"})
    public void shouldReturnErrorSavingInvalidModelLocationGuid() {

        route.setOutgoing(
                        RouteActor.builder().uri(
                                new URIDealer() {
                                    @Override
                                    public String getUriScheme() {
                                        return DeviceModelLocation.URI_SCHEME;
                                    }

                                    @Override
                                    public String getContext() {
                                        return tenant.getDomainName();
                                    }

                                    @Override
                                    public String getGuid() {
                                        return "6834ebf8-3a69-445e-9780-ddc47b0638bd";
                                    }
                                }.toURI()
                        ).data(new HashMap<String, String>() {{
                            put(DEVICE_MQTT_CHANNEL, "in");
                        }}).build()
                );

        ServiceResponse<EventRoute> response = subject.save(tenant, application, route);

        assertThat(response, hasErrorMessage(Validations.GUID_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/transformations.json", "/fixtures/event-routes.json", "/fixtures/devices.json"})
    public void shouldReturnErrorSavingNonExistingDeviceModel() {

        route.setOutgoing(
                        RouteActor.builder().uri(
                                new URIDealer() {
                                    @Override
                                    public String getUriScheme() {
                                        return DeviceModelLocation.URI_SCHEME;
                                    }

                                    @Override
                                    public String getContext() {
                                        return tenant.getDomainName();
                                    }

                                    @Override
                                    public String getGuid() {
                                        return "6834ebf8-3a69-445e-9780-ddc47b0638bd/6834ebf8-3a69-445e-9780-ddc47b0638bd";
                                    }
                                }.toURI()
                        ).data(new HashMap<String, String>() {{
                            put(DEVICE_MQTT_CHANNEL, "in");
                        }}).build()
                );

        ServiceResponse<EventRoute> response = subject.save(tenant, application, route);

        assertThat(response, hasErrorMessage(DeviceModelService.Validations.DEVICE_MODEL_NOT_FOUND.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/transformations.json", "/fixtures/event-routes.json", "/fixtures/devices.json"})
    public void shouldReturnErrorSavingNonExistingLocation() {

        final String modelGuid = "a5d6b6bd-a258-4a3b-82a9-76dc1727c7a6";

        DeviceModel deviceModel = DeviceModel.builder()
                                             .tenant(tenant)
                                             .application(application)
                                             .name("blue")
                                             .guid(modelGuid)
                                             .build();
        deviceModel = deviceModelRepository.save(deviceModel);

        route.setIncoming(
                        RouteActor.builder().uri(
                                new URIDealer() {
                                    @Override
                                    public String getUriScheme() {
                                        return DeviceModelLocation.URI_SCHEME;
                                    }

                                    @Override
                                    public String getContext() {
                                        return tenant.getDomainName();
                                    }

                                    @Override
                                    public String getGuid() {
                                        return modelGuid + "/6834ebf8-3a69-445e-9780-ddc47b0638bd";
                                    }
                                }.toURI()
                        ).data(new HashMap<String, String>() {{
                            put(DEVICE_MQTT_CHANNEL, "in");
                        }}).build()
                );

        ServiceResponse<EventRoute> response = subject.save(tenant, application, route);

        assertThat(response, hasErrorMessage(LocationService.Validations.LOCATION_GUID_DOES_NOT_EXIST.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/transformations.json", "/fixtures/event-routes.json", "/fixtures/devices.json"})
    public void shouldSaveModelLocation() {

        final String modelGuid    = "a5d6b6bd-a258-4a3b-82a9-76dc1727c7a6";
        final String locationGuid = "5217e479-e677-4281-a27f-9bbd898e6b6e";

        DeviceModel deviceModel = DeviceModel.builder()
                                             .tenant(tenant)
                                             .application(application)
                                             .name("blue")
                                             .guid(modelGuid)
                                             .build();
        deviceModel = deviceModelRepository.save(deviceModel);

        Location location = Location.builder()
                                    .tenant(tenant)
                                    .application(application)
                                    .name("here")
                                    .guid(locationGuid)
                                    .build();
        location = locationRepository.save(location);

        route.setIncoming(
                        RouteActor.builder().uri(
                                new URIDealer() {
                                    @Override
                                    public String getUriScheme() {
                                        return DeviceModelLocation.URI_SCHEME;
                                    }

                                    @Override
                                    public String getContext() {
                                        return tenant.getDomainName();
                                    }

                                    @Override
                                    public String getGuid() {
                                        return modelGuid + '/' + locationGuid;
                                    }
                                }.toURI()
                        ).data(new HashMap<String, String>() {{
                            put(DEVICE_MQTT_CHANNEL, "in");
                        }}).build()
                );

        ServiceResponse<EventRoute> response = subject.save(tenant, application, route);

        assertThat(response, isResponseOk());
        assertThat(eventRouteRepository.findByIncomingUri(route.getIncoming().getUri()), notNullValue());
        assertThat(response.getResult().getIncoming().getUri(), equalTo(route.getIncoming().getUri()));
        assertThat(response.getResult().getIncoming().getDisplayName(), equalTo("blue @ here"));
        assertThat(response.getResult().getOutgoing().getUri(), equalTo(route.getOutgoing().getUri()));
        assertThat(response.getResult().getOutgoing().getDisplayName(), equalTo("SN4434567855"));
        assertThat(response.getResult().getTransformation(), equalTo(route.getTransformation()));
        assertThat(response.getResult().getGuid(), notNullValue());

    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/devices.json",
            "/fixtures/transformations.json", "/fixtures/event-routes.json"})
    public void shouldPersistIfRouteIsValid() {

        ServiceResponse<EventRoute> response = subject.save(tenant, application, route);

        assertThat(response, isResponseOk());
        assertThat(eventRouteRepository.findByIncomingUri(route.getIncoming().getUri()), notNullValue());
        assertThat(response.getResult().getIncoming().getUri(), equalTo(route.getIncoming().getUri()));
        assertThat(response.getResult().getIncoming().getDisplayName(), equalTo("SN4434567844"));
        assertThat(response.getResult().getOutgoing().getUri(), equalTo(route.getOutgoing().getUri()));
        assertThat(response.getResult().getOutgoing().getDisplayName(), equalTo("SN4434567855"));
        assertThat(response.getResult().getTransformation(), equalTo(route.getTransformation()));
        assertThat(response.getResult().getGuid(), notNullValue());
    }

    //TODO Verify this constraint for effectiveness
//    @Test
//    @UsingDataSet(locations = "/fixtures/tenants.json")
//    public void shouldReturnAValidationMessageIfIncomingAndOutgoingChannelsAreTheSame() throws Exception {
//        String channel = "channel";
//
//        route.getIncoming().getData().put("channel",channel);
//        route.getOutgoing().getData().put("channel",channel);
//
//        List<String> errorMessages = Arrays.asList(new String[] { "Incoming and outgoing device channels cannot be the same" });
//        ServiceResponse response = subject.save(tenant,route);
//
//        assertThat(response, notNullValue());
//        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.ERROR));
//        assertThat(response.getResponseMessages(), equalTo(errorMessages));
//    }


    /* ----------------------------- save amazon kinesis ------------------------------ */

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/transformations.json", "/fixtures/event-routes.json", "/fixtures/devices.json"})
    public void shouldSaveAmazonKinesis() {

        route.setOutgoing(
                RouteActor.builder().uri(
                        new URIDealer() {
                            @Override
                            public String getUriScheme() {
                                return AmazonKinesis.URI_SCHEME;
                            }

                            @Override
                            public String getContext() {
                                return tenant.getDomainName();
                            }

                            @Override
                            public String getGuid() {
                                return "stream/eu-west-1";
                            }
                        }.toURI()
                ).data(new HashMap<String, String>() {{
                    put(DEVICE_MQTT_CHANNEL, "in");
                }}).build()
        );

        AmazonKinesis kinesisProperties = AmazonKinesis.builder()
                .key("key")
                .secret("secret")
                .region("eu-west-1")
                .streamName("stream")
                .build();

        Map<String, String> kinesisValues = kinesisProperties.getValues();
        route.getOutgoing().setData(kinesisValues);

        ServiceResponse<EventRoute> response = subject.save(tenant, application, route);

        assertThat(response, isResponseOk());
        assertThat(eventRouteRepository.findByIncomingUri(route.getIncoming().getUri()), notNullValue());
        assertThat(response.getResult().getIncoming().getUri(), equalTo(route.getIncoming().getUri()));
        assertThat(response.getResult().getIncoming().getDisplayName(), equalTo("SN4434567844"));
        assertThat(response.getResult().getOutgoing().getUri(), equalTo(route.getOutgoing().getUri()));
        assertThat(response.getResult().getOutgoing().getDisplayName(), equalTo("stream @ eu-west-1"));
        assertThat(response.getResult().getOutgoing().getData().get("key"), equalTo("key"));
        assertThat(response.getResult().getOutgoing().getData().get("secret"), equalTo("secret"));
        assertThat(response.getResult().getOutgoing().getData().get("streamName"), equalTo("stream"));
        assertThat(response.getResult().getOutgoing().getData().get("region"), equalTo("eu-west-1"));
        assertThat(response.getResult().getTransformation(), equalTo(route.getTransformation()));
        assertThat(response.getResult().getGuid(), notNullValue());

    }


    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/transformations.json", "/fixtures/event-routes.json", "/fixtures/devices.json"})
    public void shouldReturnValidationMessageSaveAmazonKinesisWithoutKey() {

        route.setOutgoing(
                RouteActor.builder().uri(
                        new URIDealer() {
                            @Override
                            public String getUriScheme() {
                                return AmazonKinesis.URI_SCHEME;
                            }

                            @Override
                            public String getContext() {
                                return tenant.getDomainName();
                            }

                            @Override
                            public String getGuid() {
                                return "stream/eu-west-1";
                            }
                        }.toURI()
                ).data(new HashMap<String, String>() {{
                    put(DEVICE_MQTT_CHANNEL, "in");
                }}).build()
        );

        AmazonKinesis kinesisProperties = AmazonKinesis.builder()
                .key("")
                .secret("secret")
                .region("eu-west-1")
                .streamName("stream")
                .build();

        Map<String, String> kinesisValues = kinesisProperties.getValues();
        route.getOutgoing().setData(kinesisValues);

        ServiceResponse<EventRoute> response = subject.save(tenant, application, route);

        assertThat(response, hasErrorMessage(AmazonKinesis.Validations.AMAZON_KINESIS_INVALID_KEY.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/transformations.json", "/fixtures/event-routes.json", "/fixtures/devices.json"})
    public void shouldReturnValidationMessageSaveAmazonKinesisWithoutSecret() {

        route.setOutgoing(
                RouteActor.builder().uri(
                        new URIDealer() {
                            @Override
                            public String getUriScheme() {
                                return AmazonKinesis.URI_SCHEME;
                            }

                            @Override
                            public String getContext() {
                                return tenant.getDomainName();
                            }

                            @Override
                            public String getGuid() {
                                return "stream/eu-west-1";
                            }
                        }.toURI()
                ).data(new HashMap<String, String>() {{
                    put(DEVICE_MQTT_CHANNEL, "in");
                }}).build()
        );

        AmazonKinesis kinesisProperties = AmazonKinesis.builder()
                .key("key")
                .secret("")
                .region("eu-west-1")
                .streamName("stream")
                .build();

        Map<String, String> kinesisValues = kinesisProperties.getValues();
        route.getOutgoing().setData(kinesisValues);

        ServiceResponse<EventRoute> response = subject.save(tenant, application, route);

        assertThat(response, hasErrorMessage(AmazonKinesis.Validations.AMAZON_KINESIS_INVALID_SECRET.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/transformations.json", "/fixtures/event-routes.json", "/fixtures/devices.json"})
    public void shouldReturnValidationMessageSaveAmazonKinesisWithNullRegion() {

        route.setOutgoing(
                RouteActor.builder().uri(
                        new URIDealer() {
                            @Override
                            public String getUriScheme() {
                                return AmazonKinesis.URI_SCHEME;
                            }

                            @Override
                            public String getContext() {
                                return tenant.getDomainName();
                            }

                            @Override
                            public String getGuid() {
                                return "stream/eu-west-1";
                            }
                        }.toURI()
                ).data(new HashMap<String, String>() {{
                    put(DEVICE_MQTT_CHANNEL, "in");
                }}).build()
        );

        AmazonKinesis kinesisProperties = AmazonKinesis.builder()
                .key("key")
                .secret("secret")
                .region(null)
                .streamName("stream")
                .build();

        Map<String, String> kinesisValues = kinesisProperties.getValues();
        route.getOutgoing().setData(kinesisValues);

        ServiceResponse<EventRoute> response = subject.save(tenant, application, route);

        assertThat(response, hasErrorMessage(AmazonKinesis.Validations.AMAZON_KINESIS_INVALID_REGION.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/transformations.json", "/fixtures/event-routes.json", "/fixtures/devices.json"})
    public void shouldReturnValidationMessageSaveAmazonKinesisWithInvalidRegion() {

        route.setOutgoing(
                RouteActor.builder().uri(
                        new URIDealer() {
                            @Override
                            public String getUriScheme() {
                                return AmazonKinesis.URI_SCHEME;
                            }

                            @Override
                            public String getContext() {
                                return tenant.getDomainName();
                            }

                            @Override
                            public String getGuid() {
                                return "stream/eu-west-1";
                            }
                        }.toURI()
                ).data(new HashMap<String, String>() {{
                    put(DEVICE_MQTT_CHANNEL, "in");
                }}).build()
        );

        AmazonKinesis kinesisProperties = AmazonKinesis.builder()
                .key("key")
                .secret("secret")
                .region("eu-1")
                .streamName("stream")
                .build();

        Map<String, String> kinesisValues = kinesisProperties.getValues();
        route.getOutgoing().setData(kinesisValues);

        ServiceResponse<EventRoute> response = subject.save(tenant, application, route);

        assertThat(response, hasErrorMessage(AmazonKinesis.Validations.AMAZON_KINESIS_INVALID_REGION.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/transformations.json", "/fixtures/event-routes.json", "/fixtures/devices.json"})
    public void shouldReturnValidationMessageSaveAmazonKinesisWithoutStreamName() {

        route.setOutgoing(
                RouteActor.builder().uri(
                        new URIDealer() {
                            @Override
                            public String getUriScheme() {
                                return AmazonKinesis.URI_SCHEME;
                            }

                            @Override
                            public String getContext() {
                                return tenant.getDomainName();
                            }

                            @Override
                            public String getGuid() {
                                return "stream/eu-west-1";
                            }
                        }.toURI()
                ).data(new HashMap<String, String>() {{
                    put(DEVICE_MQTT_CHANNEL, "in");
                }}).build()
        );

        AmazonKinesis kinesisProperties = AmazonKinesis.builder()
                .key("key")
                .secret("secret")
                .region("eu-west-1")
                .streamName("")
                .build();

        Map<String, String> kinesisValues = kinesisProperties.getValues();
        route.getOutgoing().setData(kinesisValues);

        ServiceResponse<EventRoute> response = subject.save(tenant, application, route);

        assertThat(response, hasErrorMessage(AmazonKinesis.Validations.AMAZON_KINESIS_INVALID_STREAM_NAME.getCode()));
    }

    /* ---------------------- update ------------------------- */

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json"})
    public void shouldReturnValidationMessageTenantIsNullWhenUpdating() {
        ServiceResponse<EventRoute> response = subject.update(null, null, existingGuid, route);

        assertThat(response, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json"})
    public void shouldReturnValidationMessageTenantDoesNotExistWhenUpdating() {
        ServiceResponse<EventRoute> response = subject.update(
                Tenant.builder().id("unknown_id").name("name").domainName("unknown_domain").build(),
                application,
                existingGuid,
                route);

        assertThat(response, hasErrorMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnValidationMessageGuidIsNullWhenUpdating() {
        ServiceResponse<EventRoute> response = subject.update(tenant, application, null, route);

        assertThat(response, hasErrorMessage(EventRouteService.Validations.GUID_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnValidationMessageGuidIsEmptyWhenUpdating() {
        ServiceResponse<EventRoute> response = subject.update(tenant, application, "", route);

        assertThat(response, hasErrorMessage(EventRouteService.Validations.GUID_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/event-routes.json"})
    public void shouldReturnValidationMessageGuidDoesNotExistWhenUpdating() {
        ServiceResponse<EventRoute> response = subject.update(tenant, application, "unknown_guid", route);

        assertThat(response, hasErrorMessage(EventRouteService.Validations.EVENT_ROUTE_NOT_FOUND.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnValidationMessageIfRecordIsNullWhenUpdating() {
        ServiceResponse<EventRoute> response = subject.update(tenant, application, existingGuid, null);

        assertThat(response, hasErrorMessage(CommonValidations.RECORD_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/event-routes.json"})
    public void shouldReturnRecordValidationMessagesIfRecordIsInvalidWhenUpdating() {
        //Invalid state
        route.setName(null);

        ServiceResponse<EventRoute> response = subject.update(tenant, application, existingGuid, route);

        assertThat(response, hasErrorMessage(EventRoute.Validations.NAME_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/transformations.json", "/fixtures/event-routes.json"})
    public void shouldReturnAValidationMessageIfRouteNameAlreadyExistsWithinTenantWhenUpdating() {
        String existingRouteName = "Device event forwarding route 2";

        route.setName(existingRouteName);

        ServiceResponse<EventRoute> response = subject.update(tenant, application, existingGuid, route);
        assertThat(response, hasErrorMessage(EventRouteService.Validations.NAME_IN_USE.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/transformations.json", "/fixtures/event-routes.json", "/fixtures/devices.json"})
    public void shouldReturnCrossApplicationMessageWhenUpdating() {
        route.setOutgoing(
                RouteActor.builder().uri(
                        new URIDealer() {
                            @Override
                            public String getUriScheme() {
                                return Device.URI_SCHEME;
                            }

                            @Override
                            public String getContext() {
                                return tenant.getDomainName();
                            }

                            @Override
                            public String getGuid() {
                                return "8363c556-84ea-11e6-92a2-4b01fea7e243";
                            }
                        }.toURI()
                ).data(new HashMap<String, String>() {{
                    put(DEVICE_MQTT_CHANNEL, "in");
                }}).build()
        );

        ServiceResponse<EventRoute> response = subject.update(tenant, application, existingGuid, route);
        assertThat(response, hasErrorMessage(EventRouteService.Validations.CROSS_APPLICATION.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/devices.json",
            "/fixtures/event-routes.json"})
    public void shouldUpdateIfRouteIsValid() {
        ServiceResponse<EventRoute> response = subject.update(tenant, application, existingGuid, route);

        assertThat(response, isResponseOk());
        assertThat(response.getResult(), notNullValue());
        assertThat(response.getResult().getId(), equalTo(routeId));
    }

    /* ---------------------- getAll ------------------------- */
    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/transformations.json", "/fixtures/event-routes.json"})
    public void shouldReturnAllRegisteredRoutesWithinATenant() {
        List<EventRoute> allRoutes = subject.getAll(tenant, application).getResult();

        assertThat(allRoutes, notNullValue());
        assertThat(allRoutes, hasSize(10));
        assertThat(allRoutes.get(0).getId(), equalTo("71fb0d48-674b-4f64-a3e5-0256ff3a63aa"));
        assertThat(allRoutes.get(1).getId(), equalTo("01231829-4435-4eb0-abd6-7a7bae7812bd"));
        assertThat(allRoutes.get(2).getId(), equalTo("71fb0d48-674b-4f64-a3e5-0256ff3a63ab"));
        assertThat(allRoutes.get(3).getId(), equalTo("af85a86f-9c94-4dd5-8281-844ab522b26a"));
        assertThat(allRoutes.get(4).getId(), equalTo("88a3a30a-35af-4a40-a066-42512338a81f"));
        assertThat(allRoutes.get(5).getId(), equalTo("71fb0d48-674b-4f64-a3e5-0256ff3a63ad"));
        assertThat(allRoutes.get(6).getId(), equalTo("71fb0d48-674b-4f64-a3e5-0256ff3a63ae"));
        assertThat(allRoutes.get(7).getId(), equalTo("71fb0d48-674b-4f64-a3e5-0256ff3a63ba"));
        assertThat(allRoutes.get(8).getId(), equalTo("71fb0d48-674b-4f64-a3e5-0256ff3a63bb"));
        assertThat(allRoutes.get(9).getId(), equalTo("71fb0d48-674b-4f64-a3e5-0256ff3a63bc"));

        allRoutes = subject.getAll(emptyTenant, emptyApplication).getResult();
        assertThat(allRoutes, notNullValue());
        assertThat(allRoutes, empty());
    }

    /* ---------------------- getByGUID ------------------------- */

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/transformations.json", "/fixtures/event-routes.json"})
    public void shouldReturnARegisteredRouteByItsID() {
        EventRoute route = subject.getByGUID(tenant, application, existingGuid).getResult();

        assertThat(route, notNullValue());
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/transformations.json", "/fixtures/event-routes.json", "/fixtures/devices.json"})
    public void shouldSaveEditedRouteState() {
        EventRoute route = subject.getByGUID(tenant, application, existingGuid).getResult();

        String editedName = "Edited name";
        route.setName(editedName);
        route.setActive(false);

        ServiceResponse<EventRoute> response = subject.save(tenant, application, route);

        assertThat(response, isResponseOk());
        assertThat(EventRoute.class.cast(response.getResult()).getName(), equalTo(editedName));
        assertThat(EventRoute.class.cast(response.getResult()).isActive(), equalTo(false));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/transformations.json",
            "/fixtures/devices.json", "/fixtures/event-routes.json"})
    public void shouldSaveEditedRouteAndFillDisplayNameForIncoming() {
        String expectedDisplayName = "SN4434567844";
        EventRoute route = subject.getByGUID(tenant, application, existingGuid).getResult();

        route.getIncoming().setUri(URI.create(DEVICE_URI_FOR_DISPLAY_NAME));
        route.setName("Changing Name To Persist");

        ServiceResponse<EventRoute> response = subject.save(tenant, application, route);

        assertThat(response, isResponseOk());
        assertThat(EventRoute.class.cast(response.getResult()).getIncoming().getDisplayName(), equalTo(expectedDisplayName));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/transformations.json", "/fixtures/devices.json",
            "/fixtures/event-routes.json"})
    public void shouldUpdateRouteAndFillDisplayNameForIncoming() {
        String expectedDisplayName = "SN4434567844";
        String newRouteName = "Changing Name To Persist";
        EventRoute route = subject.getByGUID(tenant, application, existingGuid).getResult();

        route.getIncoming().setUri(URI.create(DEVICE_URI_FOR_DISPLAY_NAME));
        route.setName(newRouteName);

        ServiceResponse<EventRoute> response = subject.update(tenant, application, route.getGuid(), route);

        assertThat(response, isResponseOk());
        assertThat(EventRoute.class.cast(response.getResult()).getIncoming().getDisplayName(),
                equalTo(expectedDisplayName));
        assertThat(EventRoute.class.cast(response.getResult()).getName(), equalTo(newRouteName));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/transformations.json", "/fixtures/devices.json",
            "/fixtures/event-routes.json"})
    public void shouldSaveEditedRouteAndFillDisplayNameForOutgoingDevice() {
        String expectedDisplayName = "SN4434567844";
        EventRoute route = subject.getByGUID(tenant, application, existingGuid).getResult();

        route.getOutgoing().setUri(URI.create(DEVICE_URI_FOR_DISPLAY_NAME));
        route.setName("Changing Name To Persist");

        ServiceResponse<EventRoute> response = subject.save(tenant, application, route);

        assertThat(response, isResponseOk());
        assertThat(EventRoute.class.cast(response.getResult()).getOutgoing().getDisplayName(),
                equalTo(expectedDisplayName));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/transformations.json", "/fixtures/devices.json",
            "/fixtures/event-routes.json"})
    public void shouldUpdateRouteAndFillDisplayNameForOutgoingDevice() {
        String expectedDisplayName = "SN4434567844";
        String newRouteName = "Changing Name To Persist";
        EventRoute route = subject.getByGUID(tenant, application, existingGuid).getResult();

        route.getOutgoing().setUri(URI.create(DEVICE_URI_FOR_DISPLAY_NAME));
        route.setName(newRouteName);

        ServiceResponse<EventRoute> response = subject.update(tenant, application, route.getGuid(), route);

        assertThat(response, isResponseOk());
        assertThat(EventRoute.class.cast(response.getResult()).getOutgoing().getDisplayName(),
                equalTo(expectedDisplayName));
        assertThat(EventRoute.class.cast(response.getResult()).getName(), equalTo(newRouteName));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/devices.json",
            "/fixtures/transformations.json", "/fixtures/rest-destinations.json", "/fixtures/event-routes.json"})
    public void shouldSaveEditedRouteAndFillDisplayNameForOutgoingREST() {
        String expectedDisplayName = "a restful destination";
        EventRoute route = subject.getByGUID(tenant, application, existingGuid).getResult();

        route.getOutgoing().setUri(URI.create(REST_URI_FOR_DISPLAY_NAME));
        route.setName("Changing Name To Persist");

        ServiceResponse<EventRoute> response = subject.save(tenant, application, route);

        assertThat(response, isResponseOk());
        assertThat(EventRoute.class.cast(response.getResult()).getOutgoing().getDisplayName(),
                equalTo(expectedDisplayName));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/devices.json",
            "/fixtures/transformations.json", "/fixtures/rest-destinations.json", "/fixtures/event-routes.json"})
    public void shouldUpdateRouteAndFillDisplayNameForOutgoingREST() {
        String expectedDisplayName = "a restful destination";
        String newRouteName = "Changing Name To Persist";
        EventRoute route = subject.getByGUID(tenant, application, existingGuid).getResult();

        route.getOutgoing().setUri(URI.create(REST_URI_FOR_DISPLAY_NAME));
        route.setName(newRouteName);

        ServiceResponse<EventRoute> response = subject.update(tenant, application, route.getGuid(), route);

        assertThat(response, isResponseOk());
        assertThat(EventRoute.class.cast(response.getResult()).getOutgoing().getDisplayName(),
                equalTo(expectedDisplayName));
        assertThat(EventRoute.class.cast(response.getResult()).getName(), equalTo(newRouteName));
    }


    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/transformations.json", "/fixtures/event-routes.json"})
    public void shouldReturnErrorMessageIfRouteDoesNotBelongToTenantWhenFindByGUID() {
        ServiceResponse<EventRoute> response = subject.getByGUID(emptyTenant, emptyApplication, existingGuid);

        assertThat(response, hasErrorMessage(EventRouteService.Validations.EVENT_ROUTE_NOT_FOUND.getCode()));
    }

    /* ---------------------- findByIncomingUri ------------------------- */

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/transformations.json", "/fixtures/event-routes.json"})
    public void shouldReturnARegisteredRouteByItsIncomingUri() {
        ServiceResponse<List<EventRoute>> ServiceResponse = subject.findByIncomingUri(route.getIncoming().getUri());
        List<EventRoute> routes = ServiceResponse.getResult();

        assertThat(routes, notNullValue());
        assertThat(routes, hasSize(3));
        assertThat(routes.get(0).getId(), equalTo("01231829-4435-4eb0-abd6-7a7bae7812bd"));
        assertThat(routes.get(1).getId(), equalTo("88a3a30a-35af-4a40-a066-42512338a81f"));
        assertThat(routes.get(2).getId(), equalTo("71fb0d48-674b-4f64-a3e5-0256ff3a63ae"));
    }

    /* ---------------------- remove ------------------------- */

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json"})
    public void shouldReturnValidationMessageTenantIsNullWhenRemoving() {
        ServiceResponse<EventRoute> response = subject.remove(null, null, existingGuid);

        assertThat(response, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json"})
    public void shouldReturnValidationMessageTenantDoesNotExistWhenRemoving() {
        ServiceResponse<EventRoute> response = subject.remove(
                Tenant.builder().id("unknown_id").name("name").domainName("unknown_domain").build(),
                application,
                existingGuid);

        assertThat(response, hasErrorMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnValidationMessageGuidIsNullWhenRemoving() {
        ServiceResponse<EventRoute> response = subject.remove(tenant, application, null);

        assertThat(response, hasErrorMessage(EventRouteService.Validations.GUID_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnValidationMessageGuidIsEmptyWhenRemoving() {
        ServiceResponse<EventRoute> response = subject.remove(tenant, application, "");

        assertThat(response, hasErrorMessage(EventRouteService.Validations.GUID_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/event-routes.json"})
    public void shouldReturnValidationMessageGuidDoesNotExistWhenRemoving() {
        ServiceResponse<EventRoute> response = subject.remove(tenant, application, "unknown_guid");

        assertThat(response, hasErrorMessage(EventRouteService.Validations.EVENT_ROUTE_NOT_FOUND.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnValidationMessageIfRecordIsNullWhenRemoving() {
        ServiceResponse<EventRoute> response = subject.remove(tenant, application, existingGuid);

        assertThat(response, hasErrorMessage(EventRouteService.Validations.EVENT_ROUTE_NOT_FOUND.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/event-routes.json"})
    public void shouldRemoveSuccessfully() {
        ServiceResponse<EventRoute> response = subject.remove(tenant, application, existingGuid);

        EventRoute removedRoute = subject.getByGUID(tenant, application, existingGuid).getResult();

        assertThat(response, isResponseOk());
        assertThat(response.getResult(), notNullValue());
        assertThat(response.getResult().getId(), equalTo(routeId));

        assertThat(removedRoute, nullValue());
    }

}
