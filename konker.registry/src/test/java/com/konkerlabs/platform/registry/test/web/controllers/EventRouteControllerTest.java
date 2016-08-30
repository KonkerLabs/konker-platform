package com.konkerlabs.platform.registry.test.web.controllers;

import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.model.EventRoute.RouteActor;
import com.konkerlabs.platform.registry.business.model.behaviors.DeviceURIDealer;
import com.konkerlabs.platform.registry.business.model.behaviors.RESTDestinationURIDealer;
import com.konkerlabs.platform.registry.business.model.behaviors.SmsDestinationURIDealer;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.services.api.*;
import com.konkerlabs.platform.registry.business.services.routes.api.EventRouteService;
import com.konkerlabs.platform.registry.config.WebMvcConfig;
import com.konkerlabs.platform.registry.test.base.SecurityTestConfiguration;
import com.konkerlabs.platform.registry.test.base.WebLayerTestContext;
import com.konkerlabs.platform.registry.test.base.WebTestConfiguration;
import com.konkerlabs.platform.registry.web.controllers.EventRouteController;
import com.konkerlabs.platform.registry.web.forms.EventRouteForm;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

import static com.konkerlabs.platform.registry.business.model.Device.builder;
import static com.konkerlabs.platform.registry.business.model.behaviors.DeviceURIDealer.DEVICE_URI_SCHEME;
import static com.konkerlabs.platform.registry.business.model.behaviors.RESTDestinationURIDealer.REST_DESTINATION_URI_SCHEME;
import static com.konkerlabs.platform.registry.business.model.behaviors.SmsDestinationURIDealer.SMS_URI_SCHEME;
import static com.konkerlabs.platform.registry.business.services.api.ServiceResponse.Status.ERROR;
import static com.konkerlabs.platform.registry.business.services.api.ServiceResponse.Status.OK;
import static com.konkerlabs.platform.registry.web.controllers.EventRouteController.Messages.ROUTE_REMOVED_SUCCESSFULLY;
import static java.text.MessageFormat.format;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {
        WebMvcConfig.class,
        WebTestConfiguration.class,
        SecurityTestConfiguration.class,
        EventRouteControllerTest.EventRouteTestContextConfig.class
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
    private SmsDestinationService smsDestinationService;
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private Tenant tenant;

    private Device incomingDevice;
    private Device outgoingDevice;
    private EventRoute newRoute;
    private EventRoute savedRoute;
    private List<EventRoute> registeredRoutes;
    private NewServiceResponse<EventRoute> response;
    private MultiValueMap<String, String> routeData;

    private EventRouteForm routeForm;
    private String routeGuid = "71fb0d48-674b-4f64-a3e5-0256ff3a63af";

    @Before
    public void setUp() throws Exception {
        List<Transformation> transformations = new ArrayList<>();
        when(transformationService.getAll(tenant)).thenReturn(
                ServiceResponseBuilder.<List<Transformation>>ok()
                        .withResult(transformations).build()
        );

        List<Device> devices = new ArrayList<>();
        when(deviceRegisterService.findAll(tenant)).thenReturn(
                ServiceResponseBuilder.<List<Device>>ok()
                        .withResult(devices).build()
        );
        List<RestDestination> restDestinations = new ArrayList<>();
        when(restDestinationService.findAll(tenant)).thenReturn(
                ServiceResponseBuilder.<List<RestDestination>>ok()
                        .withResult(restDestinations).build()
        );
        List<SmsDestination> smsDestinations = new ArrayList<>();
        when(smsDestinationService.findAll(tenant)).thenReturn(
                ServiceResponse.<List<SmsDestination>>builder()
                        .status(OK)
                        .result(smsDestinations).<List<SmsDestination>>build()
        );

        incomingDevice = builder().deviceId("0000000000000004").build();
        outgoingDevice = builder().deviceId("0000000000000005").build();

        routeForm = new EventRouteForm();
        routeForm.setName("Route name");
        routeForm.setDescription("Route description");
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

        DeviceURIDealer deviceUriDealer = new DeviceURIDealer() {
        };
        SmsDestinationURIDealer smsURIDealer = new SmsDestinationURIDealer() {
        };
        RESTDestinationURIDealer restDestinationURIDealer = new RESTDestinationURIDealer() {
        };

        Supplier<URI> outgoingUriSupplier = () -> {
            switch (routeForm.getOutgoingScheme()) {
                case DEVICE_URI_SCHEME:
                    return deviceUriDealer.toDeviceRouteURI(tenant.getDomainName(), routeForm.getOutgoing().getAuthorityId());
                case SMS_URI_SCHEME:
                    return smsURIDealer.toSmsURI(tenant.getDomainName(), routeForm.getOutgoing().getAuthorityId());
                case REST_DESTINATION_URI_SCHEME:
                    return restDestinationURIDealer.toRestDestinationURI(tenant.getDomainName(), routeForm.getOutgoing().getAuthorityId());
                default:
                    return null;
            }
        };

        EventRoute.EventRouteBuilder routeBuilder = EventRoute.builder()
                .name(routeForm.getName())
                .description(routeForm.getDescription())
                .incoming(RouteActor.builder()
                        .uri(deviceUriDealer.toDeviceRouteURI(tenant.getDomainName(), routeForm.getIncoming().getAuthorityId()))
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

        savedRoute = routeBuilder.guid(routeGuid).build();

        registeredRoutes = new ArrayList<EventRoute>(asList(new EventRoute[]{newRoute}));
    }

    @After
    public void tearDown() {
        reset(eventRouteService);
    }

    @Test
    public void shouldListAllRegisteredRoutes() throws Exception {
        when(eventRouteService.getAll(eq(tenant))).thenReturn(
            ServiceResponseBuilder.<List<EventRoute>>ok()
                .withResult(registeredRoutes).build()
        );

        getMockMvc().perform(get("/routes")).andExpect(model().attribute("routes", equalTo(registeredRoutes)))
                .andExpect(view().name("routes/index"));
    }

    @Test
    public void shouldShowCreationForm() throws Exception {
        getMockMvc().perform(get("/routes/new"))
                .andExpect(view().name("routes/form"))
                .andExpect(model().attribute("route", new EventRouteForm()))
                .andExpect(model().attribute("action", "/routes/save"));
    }

    @Test
    public void shouldRenderDeviceOutgoingViewFragment() throws Exception {
        getMockMvc().perform(get("/routes/outgoing/{0}", "device"))
                .andExpect(view().name("routes/device-outgoing"))
                .andExpect(model().attribute("route", new EventRouteForm()));
    }

    @Test
    public void shouldRenderSmsViewFragment() throws Exception {
        getMockMvc().perform(get("/routes/outgoing/{0}", "sms"))
                .andExpect(view().name("routes/sms-outgoing"))
                .andExpect(model().attribute("route", new EventRouteForm()));
    }

    @Test
    public void shouldRenderRestDestinationsViewFragment() throws Exception {
        getMockMvc().perform(get("/routes/outgoing/{0}", "rest"))
                .andExpect(view().name("routes/rest-outgoing"))
                .andExpect(model().attribute("route", new EventRouteForm()));
    }

    @Test
    public void shouldRenderEmptyBodyWhenSchemeIsUnknown() throws Exception {
        getMockMvc().perform(get("/routes/outgoing/{0}", "unknown_scheme"))
                .andExpect(view().name("common/empty"));
    }

    @Test
    public void shouldBindErrorMessagesWhenRegistrationFailsAndGoBackToCreationForm() throws Exception {
        response = ServiceResponseBuilder.<EventRoute>error().
                withMessage(CommonValidations.TENANT_NULL.getCode()).build();

        when(eventRouteService.save(eq(tenant), eq(newRoute))).thenReturn(response);

        getMockMvc().perform(post("/routes/save").params(routeData))
                .andExpect(model().attribute("errors", equalTo(
                    Arrays.asList(new String[] {
                            applicationContext.getMessage(CommonValidations.TENANT_NULL.getCode(),null,Locale.ENGLISH)
                    })
                )))
                .andExpect(model().attribute("method",""))
                .andExpect(model().attribute("route", equalTo(routeForm))).andExpect(view().name("routes/form"));

        verify(eventRouteService).save(eq(tenant), eq(newRoute));
    }

    @Test
    public void shouldRedirectToShowAfterSuccessfulRouteCreation() throws Exception {
        response = spy(ServiceResponseBuilder.<EventRoute>ok()
                .withResult(savedRoute)
                .build());

        when(eventRouteService.save(eq(tenant), eq(newRoute))).thenReturn(response);

        getMockMvc().perform(post("/routes/save").params(routeData))
                .andExpect(flash().attribute("message",
                    applicationContext.getMessage(EventRouteController.Messages.ROUTE_REGISTERED_SUCCESSFULLY.getCode(),null, Locale.ENGLISH)
                ))
                .andExpect(redirectedUrl(MessageFormat.format("/routes/{0}", savedRoute.getGuid())));

        verify(eventRouteService).save(eq(tenant), eq(newRoute));
    }

    @Test
    public void shouldShowEditForm() throws Exception {
        routeForm.setAdditionalSupplier(null);

        when(eventRouteService.getByGUID(tenant, routeGuid)).thenReturn(
                ServiceResponseBuilder.<EventRoute>ok().withResult(newRoute).build());

        getMockMvc().perform(get(format("/routes/{0}/edit", routeGuid)))
                .andExpect(model().attribute("route", equalTo(routeForm)))
                .andExpect(model().attribute("action", format("/routes/{0}", routeGuid)))
                .andExpect(model().attribute("method", "put"))
                .andExpect(view().name("routes/form"));
    }

    @Test
    public void shouldBindErrorMessagesWhenUpdateFailsAndGoBackToEditForm() throws Exception {
        response = ServiceResponseBuilder.<EventRoute>error()
                .withMessage(CommonValidations.TENANT_NULL.getCode()).build();

        when(eventRouteService.update(eq(tenant), eq(routeGuid), eq(newRoute))).thenReturn(response);

        getMockMvc().perform(put("/routes/{0}", routeGuid).params(routeData))
                .andExpect(model().attribute("errors", equalTo(
                    Arrays.asList(new String[] {
                        applicationContext.getMessage(CommonValidations.TENANT_NULL.getCode(),null,Locale.ENGLISH)
                    })
                )))
                .andExpect(model().attribute("method","put"))
                .andExpect(model().attribute("route", equalTo(routeForm))).andExpect(view().name("routes/form"));

        verify(eventRouteService).update(eq(tenant), eq(routeGuid), eq(newRoute));
    }

    @Test
    public void shouldRedirectToShowAfterSuccessfulRouteEdit() throws Exception {
        response = spy(ServiceResponseBuilder.<EventRoute>ok()
                .withResult(newRoute).build());

        when(eventRouteService.update(eq(tenant), eq(routeGuid), eq(newRoute))).thenReturn(response);

        getMockMvc().perform(put("/routes/{0}", routeGuid).params(routeData))
                .andExpect(flash().attribute("message",
                        applicationContext.getMessage(EventRouteController.Messages.ROUTE_REGISTERED_SUCCESSFULLY.getCode(),null, Locale.ENGLISH)
                ))
                .andExpect(redirectedUrl(MessageFormat.format("/routes/{0}", newRoute.getGuid())));

        verify(eventRouteService).update(eq(tenant), eq(routeGuid), eq(newRoute));
    }

    @Test
    public void shouldShowRouteDetails() throws Exception {
        routeForm.setAdditionalSupplier(null);

        routeForm.setId(routeGuid);
        newRoute.setId(routeGuid);
        when(eventRouteService.getByGUID(tenant, newRoute.getId())).thenReturn(
                ServiceResponseBuilder.<EventRoute>ok().withResult(newRoute).build());

        getMockMvc().perform(
                get("/routes/{0}", newRoute.getId())
        ).andExpect(model().attribute("route", equalTo(routeForm)))
                .andExpect(view().name("routes/show"));

        verify(eventRouteService).getByGUID(tenant, newRoute.getId());
    }

    @Test
    public void shoudlRedirectToRouteIndexAfterRouteRemoval() throws Exception {
        newRoute.setGuid(routeGuid);

        NewServiceResponse<EventRoute> responseDelete = ServiceResponseBuilder.<EventRoute>ok()
                .withResult(newRoute).build();
        NewServiceResponse<List<EventRoute>> responseGetAll = ServiceResponseBuilder.<List<EventRoute>>ok()
                .withResult(registeredRoutes).<List<EventRoute>>build();
        spy(responseDelete);
        spy(responseGetAll);

        when(eventRouteService.remove(tenant, newRoute.getGuid())).thenReturn(responseDelete);
        when(eventRouteService.getAll(eq(tenant))).thenReturn(responseGetAll);

        getMockMvc().perform(delete("/routes/{0}", newRoute.getGuid()))
                .andExpect(flash().attribute("message",
                    applicationContext.getMessage(ROUTE_REMOVED_SUCCESSFULLY.getCode(),null,Locale.ENGLISH)
                ))
                .andExpect(redirectedUrl("/routes"));

        verify(eventRouteService).remove(tenant, newRoute.getGuid());
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
        public RestDestinationService restDestinationService() {
            return mock(RestDestinationService.class);
        }

        @Bean
        public SmsDestinationService smsDestinationService() {
            return mock(SmsDestinationService.class);
        }

        @Bean
        public TransformationService transformationService() {
            return mock(TransformationService.class);
        }
    }
}
