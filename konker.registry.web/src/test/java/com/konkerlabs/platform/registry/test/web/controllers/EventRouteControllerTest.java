package com.konkerlabs.platform.registry.test.web.controllers;

import static com.konkerlabs.platform.registry.business.model.Device.builder;
import static com.konkerlabs.platform.registry.business.model.behaviors.DeviceURIDealer.DEVICE_URI_SCHEME;
import static com.konkerlabs.platform.registry.business.model.behaviors.RESTDestinationURIDealer.REST_DESTINATION_URI_SCHEME;
import static com.konkerlabs.platform.registry.web.controllers.EventRouteController.Messages.ROUTE_REMOVED_SUCCESSFULLY;
import static java.text.MessageFormat.format;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

import com.konkerlabs.platform.registry.config.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.DeviceModel;
import com.konkerlabs.platform.registry.business.model.EventRoute;
import com.konkerlabs.platform.registry.business.model.EventRoute.RouteActor;
import com.konkerlabs.platform.registry.business.model.Location;
import com.konkerlabs.platform.registry.business.model.RestDestination;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.Transformation;
import com.konkerlabs.platform.registry.business.model.behaviors.URIDealer;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.DeviceModelService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.EventRouteService;
import com.konkerlabs.platform.registry.business.services.api.LocationSearchService;
import com.konkerlabs.platform.registry.business.services.api.RestDestinationService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.business.services.api.TransformationService;
import com.konkerlabs.platform.registry.test.base.SecurityTestConfiguration;
import com.konkerlabs.platform.registry.test.base.WebLayerTestContext;
import com.konkerlabs.platform.registry.test.base.WebTestConfiguration;
import com.konkerlabs.platform.registry.web.controllers.EventRouteController;
import com.konkerlabs.platform.registry.web.forms.EventRouteForm;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {
        WebMvcConfig.class,
        WebTestConfiguration.class,
        SecurityTestConfiguration.class,
        EventRouteControllerTest.EventRouteTestContextConfig.class,
        WebConfig.class,
        CdnConfig.class,
        AmazonConfig.class,
        EmailConfig.class,
        MessageSourceConfig.class
})
public class EventRouteControllerTest extends WebLayerTestContext {

    @Autowired
    private EventRouteService eventRouteService;
    @Autowired
    private TransformationService transformationService;
    @Autowired
    private DeviceRegisterService deviceRegisterService;
    @Autowired
    private RestDestinationService restDestinationService;
    @Autowired
    private DeviceModelService deviceModelService;
    @Autowired
    private LocationSearchService locationSearchService;
    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private Tenant tenant;
    @Autowired
    private Application application;

    private Device incomingDevice;
    private Device outgoingDevice;
    private EventRoute newRoute;
    private EventRoute savedRoute;
    private List<EventRoute> registeredRoutes;
    private ServiceResponse<EventRoute> response;
    private MultiValueMap<String, String> routeData;

    private EventRouteForm routeForm;
    private String routeGuid = "71fb0d48-674b-4f64-a3e5-0256ff3a63af";
    private String otherRouteGuid = "71fb0d48-674b-4f64-a3e5-0256ff3a63bg";

    @Before
    public void setUp() throws Exception {
        List<Transformation> transformations = new ArrayList<>();
        when(transformationService.getAll(tenant, application)).thenReturn(
                ServiceResponseBuilder.<List<Transformation>>ok()
                        .withResult(transformations).build()
        );
        List<Device> devices = new ArrayList<>();
        when(deviceRegisterService.findAll(tenant, application)).thenReturn(
                ServiceResponseBuilder.<List<Device>>ok()
                        .withResult(devices).build()
        );
        List<RestDestination> restDestinations = new ArrayList<>();
        when(restDestinationService.findAll(tenant, application)).thenReturn(
                ServiceResponseBuilder.<List<RestDestination>>ok()
                        .withResult(restDestinations).build()
        );
        List<DeviceModel> deviceModels = new ArrayList<>();
        when(deviceModelService.findAll(tenant, application)).thenReturn(
                ServiceResponseBuilder.<List<DeviceModel>>ok()
                        .withResult(deviceModels).build()
        );
        List<Location> locations = new ArrayList<>();
        when(locationSearchService.findAll(tenant, application)).thenReturn(
                ServiceResponseBuilder.<List<Location>>ok()
                        .withResult(locations).build()
        );

        incomingDevice = builder().deviceId("0000000000000004").build();
        outgoingDevice = builder().deviceId("0000000000000005").build();

        routeForm = new EventRouteForm();
        routeForm.setName("Route name");
        routeForm.setDescription("Route description");
        routeForm.setApplicationName("konker");
        routeForm.getIncoming().setAuthorityId(incomingDevice.getDeviceId());
        routeForm.getIncoming().getAuthorityData().put("channel","command");
        routeForm.setOutgoingScheme("device");
        routeForm.getOutgoing().setAuthorityId(outgoingDevice.getDeviceId());
        routeForm.getOutgoing().getAuthorityData().put("channel","in");
        routeForm.setFilteringExpression("#command.type == 'ButtonPressed'");
        routeForm.setTransformation("trans_id");
        routeForm.setActive(true);

        routeForm.setAdditionalSupplier(() -> tenant.getDomainName());

        routeData = new LinkedMultiValueMap<>();
        routeData.add("name", routeForm.getName());
        routeData.add("description", routeForm.getDescription());
        routeData.add("incoming.authorityId", routeForm.getIncoming().getAuthorityId());
        routeData.add("incoming.authorityData['channel']", routeForm.getIncoming().getAuthorityData().get("channel"));
        routeData.add("outgoingScheme", routeForm.getOutgoingScheme());
        routeData.add("outgoing.authorityId", routeForm.getOutgoing().getAuthorityId());
        routeData.add("outgoing.authorityData['channel']", routeForm.getOutgoing().getAuthorityData().get("channel"));
        routeData.add("filteringExpression", routeForm.getFilteringExpression());
        routeData.add("transformation", routeForm.getTransformation());
        routeData.add("active", "true");

        URIDealer deviceURI = new URIDealer() {
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
                return routeForm.getOutgoing().getAuthorityId();
            }
        };

        URIDealer restDestinationURI = new URIDealer() {
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
                return routeForm.getOutgoing().getAuthorityId();
            }
        };



        Supplier<URI> outgoingUriSupplier = () -> {
            switch (routeForm.getOutgoingScheme()) {
                case DEVICE_URI_SCHEME:
                    return deviceURI.toURI();
                case REST_DESTINATION_URI_SCHEME:
                    return restDestinationURI.toURI();
                default:
                    return null;
            }
        };


        EventRoute.EventRouteBuilder routeBuilder = EventRoute.builder()
                .name(routeForm.getName())
                .description(routeForm.getDescription())
                .incoming(RouteActor.builder()
                        .uri(new URIDealer() {
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
                                return routeForm.getIncoming().getAuthorityId();
                            }
                        }.toURI())
                        .data(routeForm.getIncoming().getAuthorityData())
                        .build())
                .outgoing(RouteActor.builder()
                        .uri(outgoingUriSupplier.get())
                        .data(routeForm.getOutgoing().getAuthorityData())
                        .build())
                .filteringExpression(routeForm.getFilteringExpression())
                .transformation(Transformation.builder().id(routeForm.getTransformation()).build())
                .active(routeForm.isActive());

        newRoute = routeBuilder.build();

        savedRoute = routeBuilder.guid(routeGuid).application(application).build();

        registeredRoutes = new ArrayList<EventRoute>(asList(new EventRoute[]{savedRoute}));
    }

    @After
    public void tearDown() {
        reset(eventRouteService);
    }

    @Test
    @WithMockUser(authorities={"LIST_ROUTES"})
    public void shouldListAllRegisteredRoutes() throws Exception {
        when(eventRouteService.getAll(eq(tenant), eq(application))).thenReturn(
            ServiceResponseBuilder.<List<EventRoute>>ok()
                .withResult(registeredRoutes).build()
        );

        when(applicationService.findAll(tenant))
			.thenReturn(ServiceResponseBuilder.<List<Application>> ok().withResult(Collections.singletonList(application)).build());

        getMockMvc().perform(get("/routes"))
        	.andDo(print())
        	.andExpect(model().attribute("routes", equalTo(registeredRoutes)))
            .andExpect(view().name("routes/index"));
    }

    @Test
    @WithMockUser(authorities={"CREATE_DEVICE_ROUTE"})
    public void shouldShowCreationForm() throws Exception {
        EventRouteForm routeForm = new EventRouteForm();
        routeForm.setApplicationName("konker");

        getMockMvc().perform(get("/routes/new"))
                .andExpect(view().name("routes/form"))
                .andExpect(model().attribute("route", routeForm))
                .andExpect(model().attribute("action", format("/routes/{0}/save", application.getName())));
    }

    @Test
    public void shouldRenderDeviceIncomingViewFragment() throws Exception {
        getMockMvc().perform(get("/routes/{0}/incoming/{1}", application.getName(), "device"))
                .andExpect(view().name("routes/device-incoming"))
                .andExpect(model().attribute("route", new EventRouteForm()));
    }
    
    @Test
    public void shouldRenderApplicationIncomingViewFragment() throws Exception {
        getMockMvc().perform(get("/routes/{0}/incoming/{1}", application.getName(), "application"))
                .andExpect(view().name("routes/application-incoming"))
                .andExpect(model().attribute("route", new EventRouteForm()));
    }

    @Test
    public void shouldRenderModelLocationIncomingViewFragment() throws Exception {
        getMockMvc().perform(get("/routes/{0}/incoming/{1}", application.getName(), "modelLocation"))
                .andExpect(view().name("routes/model-location-incoming"))
                .andExpect(model().attribute("route", new EventRouteForm()));
    }

    @Test
    public void shouldRenderModelLocationOutgoingViewFragment() throws Exception {
        getMockMvc().perform(get("/routes/{0}/outgoing/{1}", application.getName(), "modelLocation"))
                .andExpect(view().name("routes/model-location-outgoing"))
                .andExpect(model().attribute("route", new EventRouteForm()));
    }

    @Test
    public void shouldRenderDeviceOutgoingViewFragment() throws Exception {
        getMockMvc().perform(get("/routes/{0}/outgoing/{1}", application.getName(), "device"))
                .andExpect(view().name("routes/device-outgoing"))
                .andExpect(model().attribute("route", new EventRouteForm()));
    }

    @Test
    public void shouldRenderRestDestinationsViewFragment() throws Exception {
        getMockMvc().perform(get("/routes/{0}/outgoing/{1}", application.getName(), "rest"))
                .andExpect(view().name("routes/rest-outgoing"))
                .andExpect(model().attribute("route", new EventRouteForm()));
    }

    @Test
    public void shouldRenderAmazonKinesisViewFragment() throws Exception {
        getMockMvc().perform(get("/routes/{0}/outgoing/{1}", application.getName(), "amazonKinesis"))
                .andExpect(view().name("routes/amazon-kinesis-outgoing"))
                .andExpect(model().attribute("route", new EventRouteForm()));
    }

    @Test
    public void shouldRenderEmptyBodyWhenSchemeIsUnknown() throws Exception {
        getMockMvc().perform(get("/routes/{0}/outgoing/{1}", application.getName(), "unknown_scheme"))
                .andExpect(view().name("common/empty"));
    }

    @Test
    @WithMockUser(authorities={"CREATE_DEVICE_ROUTE"})
    public void shouldBindErrorMessagesWhenRegistrationFailsAndGoBackToCreationForm() throws Exception {
        response = ServiceResponseBuilder.<EventRoute>error().
                withMessage(CommonValidations.TENANT_NULL.getCode()).build();

        when(eventRouteService.save(eq(tenant), eq(application), eq(newRoute))).thenReturn(response);
        when(applicationService.getByApplicationName(tenant, application.getName()))
			.thenReturn(ServiceResponseBuilder.<Application> ok().withResult(application).build());

        getMockMvc().perform(post("/routes/{0}/save", application.getName()).params(routeData))
                .andExpect(model().attribute("errors", equalTo(
                    Arrays.asList(new String[] {
                            applicationContext.getMessage(CommonValidations.TENANT_NULL.getCode(),null,Locale.ENGLISH)
                    })
                )))
                .andExpect(model().attribute("method",""))
                .andExpect(model().attribute("route", equalTo(routeForm))).andExpect(view().name("routes/form"));

        verify(eventRouteService).save(eq(tenant), eq(application), eq(newRoute));
    }

    @Test
    @WithMockUser(authorities={"CREATE_DEVICE_ROUTE"})
    public void shouldRedirectToShowAfterSuccessfulRouteCreation() throws Exception {
        response = spy(ServiceResponseBuilder.<EventRoute>ok()
                .withResult(savedRoute)
                .build());

        when(eventRouteService.save(eq(tenant), eq(application), eq(newRoute))).thenReturn(response);
        when(applicationService.getByApplicationName(tenant, application.getName()))
    		.thenReturn(ServiceResponseBuilder.<Application> ok().withResult(application).build());

        getMockMvc().perform(post("/routes/{0}/save", application.getName()).params(routeData))
                .andExpect(flash().attribute("message",
                    applicationContext.getMessage(EventRouteController.Messages.ROUTE_REGISTERED_SUCCESSFULLY.getCode(),null, Locale.ENGLISH)
                ))
                .andExpect(redirectedUrl(MessageFormat.format("/routes/{0}/{1}", application.getName(), savedRoute.getGuid())));

        verify(eventRouteService).save(eq(tenant), eq(application), eq(newRoute));
    }

    @Test
    @WithMockUser(authorities={"EDIT_DEVICE_ROUTE"})
    public void shouldShowEditForm() throws Exception {
        routeForm.setAdditionalSupplier(null);
        routeForm.setApplicationName(null);

        when(eventRouteService.getByGUID(tenant, application, routeGuid)).thenReturn(
                ServiceResponseBuilder.<EventRoute>ok().withResult(newRoute).build());
        when(applicationService.getByApplicationName(tenant, application.getName()))
        	.thenReturn(ServiceResponseBuilder.<Application> ok().withResult(application).build());
        when(applicationService.findAll(tenant))
        	.thenReturn(ServiceResponseBuilder.<List<Application>> ok().withResult(Collections.singletonList(application)).build());

        getMockMvc().perform(get("/routes/{0}/{1}/edit", application.getName(), routeGuid))
                .andExpect(model().attribute("route", equalTo(routeForm)))
                .andExpect(model().attribute("action", format("/routes/{0}/{1}", application.getName(), routeGuid)))
                .andExpect(model().attribute("method", "put"))
                .andExpect(view().name("routes/form"));
    }

    @Test
    @WithMockUser(authorities={"EDIT_DEVICE_ROUTE"})
    public void shouldBindErrorMessagesWhenUpdateFailsAndGoBackToEditForm() throws Exception {
        response = ServiceResponseBuilder.<EventRoute>error()
                .withMessage(CommonValidations.TENANT_NULL.getCode()).build();

        when(eventRouteService.update(eq(tenant), eq(application), eq(routeGuid), eq(newRoute))).thenReturn(response);
        when(applicationService.getByApplicationName(tenant, application.getName()))
			.thenReturn(ServiceResponseBuilder.<Application> ok().withResult(application).build());

        getMockMvc().perform(put("/routes/{0}/{1}", application.getName(), routeGuid).params(routeData))
                .andExpect(model().attribute("errors", equalTo(
                    Arrays.asList(new String[] {
                        applicationContext.getMessage(CommonValidations.TENANT_NULL.getCode(),null,Locale.ENGLISH)
                    })
                )))
                .andExpect(model().attribute("method","put"))
                .andExpect(model().attribute("route", equalTo(routeForm))).andExpect(view().name("routes/form"));

        verify(eventRouteService).update(eq(tenant), eq(application), eq(routeGuid), eq(newRoute));
    }

    @Test
    @WithMockUser(authorities={"EDIT_DEVICE_ROUTE"})
    public void shouldRedirectToShowAfterSuccessfulRouteEdit() throws Exception {
        response = spy(ServiceResponseBuilder.<EventRoute>ok()
                .withResult(savedRoute).build());

        when(eventRouteService.update(eq(tenant), eq(application), eq(routeGuid), eq(newRoute))).thenReturn(response);
        when(applicationService.getByApplicationName(tenant, application.getName()))
			.thenReturn(ServiceResponseBuilder.<Application> ok().withResult(application).build());

        getMockMvc().perform(put("/routes/{0}/{1}", application.getName(), routeGuid).params(routeData))
                .andExpect(flash().attribute("message",
                        applicationContext.getMessage(EventRouteController.Messages.ROUTE_REGISTERED_SUCCESSFULLY.getCode(),null, Locale.ENGLISH)
                ))
                .andExpect(redirectedUrl(MessageFormat.format("/routes/{0}/{1}", application.getName(), savedRoute.getGuid())));

        verify(eventRouteService).update(eq(tenant), eq(application), eq(routeGuid), eq(newRoute));
    }

    @Test
    @WithMockUser(authorities={"SHOW_DEVICE_ROUTE"})
    public void shouldShowRouteDetails() throws Exception {
        routeForm.setAdditionalSupplier(null);
        routeForm.setApplicationName(null);

        routeForm.setId(routeGuid);
        newRoute.setId(routeGuid);
        when(eventRouteService.getByGUID(tenant, application, newRoute.getId())).thenReturn(
                ServiceResponseBuilder.<EventRoute>ok().withResult(newRoute).build());
        when(applicationService.getByApplicationName(tenant, application.getName()))
			.thenReturn(ServiceResponseBuilder.<Application> ok().withResult(application).build());

        getMockMvc().perform(
                get("/routes/{0}/{1}", application.getName(), newRoute.getId())
        ).andExpect(model().attribute("route", equalTo(routeForm)))
                .andExpect(view().name("routes/show"));

        verify(eventRouteService).getByGUID(tenant, application, newRoute.getId());
    }

    @Test
    @WithMockUser(authorities={"REMOVE_DEVICE_ROUTE"})
    public void shoudlRedirectToRouteIndexAfterRouteRemoval() throws Exception {
        newRoute.setGuid(routeGuid);

        ServiceResponse<EventRoute> responseDelete = ServiceResponseBuilder.<EventRoute>ok()
                .withResult(newRoute).build();
        ServiceResponse<List<EventRoute>> responseGetAll = ServiceResponseBuilder.<List<EventRoute>>ok()
                .withResult(registeredRoutes).<List<EventRoute>>build();
        spy(responseDelete);
        spy(responseGetAll);

        when(eventRouteService.remove(tenant, application, newRoute.getGuid())).thenReturn(responseDelete);
        when(eventRouteService.getAll(eq(tenant), eq(application))).thenReturn(responseGetAll);
        when(applicationService.getByApplicationName(tenant, application.getName()))
			.thenReturn(ServiceResponseBuilder.<Application> ok().withResult(application).build());

        getMockMvc().perform(delete("/routes/{0}/{1}", application.getName(), newRoute.getGuid()))
                .andExpect(flash().attribute("message",
                    applicationContext.getMessage(ROUTE_REMOVED_SUCCESSFULLY.getCode(),null,Locale.ENGLISH)
                ))
                .andExpect(redirectedUrl("/routes"));

        verify(eventRouteService).remove(tenant, application, newRoute.getGuid());
    }

    @Test
    @WithMockUser(authorities={"REMOVE_DEVICE_ROUTE"})
    public void shoudlReturnMessageAfterTryRemoveInexistentRoute() throws Exception {
        newRoute.setGuid(otherRouteGuid);

        ServiceResponse<EventRoute> responseDelete = ServiceResponseBuilder.<EventRoute>error()
                .withMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()).build();
        spy(responseDelete);

        when(eventRouteService.remove(tenant, application, newRoute.getGuid())).thenReturn(responseDelete);
        when(applicationService.getByApplicationName(tenant, application.getName()))
			.thenReturn(ServiceResponseBuilder.<Application> ok().withResult(application).build());

        List<String> messages = Collections.singletonList(
        		applicationContext.getMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode(),null,Locale.ENGLISH));

        getMockMvc().perform(delete("/routes/{0}/{1}", application.getName(), newRoute.getGuid()))
                .andExpect(flash().attribute("errors", messages))
                .andExpect(redirectedUrl("/routes"));

        verify(eventRouteService).remove(tenant, application, newRoute.getGuid());
    }

    @Configuration
    static class EventRouteTestContextConfig {
        @Bean
        public EventRouteService eventRouteService() {
            return mock(EventRouteService.class);
        }

        @Bean
        public DeviceRegisterService deviceRegisterService() {
            return mock(DeviceRegisterService.class);
        }

        @Bean
        public DeviceModelService deviceModelService() {
            return mock(DeviceModelService.class);
        }

        @Bean
        public LocationSearchService locationSearchService() {
            return mock(LocationSearchService.class);
        }

        @Bean
        public RestDestinationService restDestinationService() {
            return mock(RestDestinationService.class);
        }

        @Bean
        public TransformationService transformationService() {
            return mock(TransformationService.class);
        }

        @Bean
        public ApplicationService applicationService() {
            return mock(ApplicationService.class);
        }
    }

}
