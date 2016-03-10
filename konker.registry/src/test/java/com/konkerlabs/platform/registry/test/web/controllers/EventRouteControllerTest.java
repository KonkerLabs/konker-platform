package com.konkerlabs.platform.registry.test.web.controllers;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.EventRoute;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.Transformation;
import com.konkerlabs.platform.registry.business.model.behaviors.DeviceURIDealer;
import com.konkerlabs.platform.registry.business.model.behaviors.SmsURIDealer;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.TransformationService;
import com.konkerlabs.platform.registry.business.services.routes.api.EventRouteService;
import com.konkerlabs.platform.registry.config.WebMvcConfig;
import com.konkerlabs.platform.registry.test.base.SecurityTestConfiguration;
import com.konkerlabs.platform.registry.test.base.WebLayerTestContext;
import com.konkerlabs.platform.registry.test.base.WebTestConfiguration;
import com.konkerlabs.platform.registry.web.forms.EventRouteForm;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.List;
import java.util.function.Supplier;

import static com.konkerlabs.platform.registry.business.model.Device.builder;
import static com.konkerlabs.platform.registry.business.model.EventRoute.RuleActor;
import static com.konkerlabs.platform.registry.business.model.behaviors.DeviceURIDealer.DEVICE_URI_SCHEME;
import static com.konkerlabs.platform.registry.business.model.behaviors.SmsURIDealer.SMS_URI_SCHEME;
import static com.konkerlabs.platform.registry.business.services.api.ServiceResponse.Status.ERROR;
import static com.konkerlabs.platform.registry.business.services.api.ServiceResponse.Status.OK;
import static java.text.MessageFormat.format;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    private Tenant tenant;

    private Device incomingDevice;
    private Device outgoingDevice;
    private EventRoute route;
    private List<EventRoute> registeredRoutes;
    private ServiceResponse<EventRoute> response;
    private MultiValueMap<String, String> routeData;
    private EventRouteForm routeForm;

    private String routeId = "71fb0d48-674b-4f64-a3e5-0256ff3a63af";

    @Before
    public void setUp() throws Exception {
        List<Transformation> transformations = new ArrayList<>();

        when(transformationService.getAll(tenant)).thenReturn(
            ServiceResponse.<List<Transformation>>builder()
                .status(OK)
                .result(transformations).<List<Transformation>>build()
        );

        incomingDevice = builder().deviceId("0000000000000004").build();
        outgoingDevice = builder().deviceId("0000000000000005").build();

        routeForm = new EventRouteForm();
        routeForm.setName("Route name");
        routeForm.setDescription("Route description");
        routeForm.setIncomingAuthority(incomingDevice.getDeviceId());
        routeForm.setIncomingChannel("command");
        routeForm.setOutgoingScheme("device");
        routeForm.setOutgoingDeviceAuthority(outgoingDevice.getDeviceId());
        routeForm.setOutgoingDeviceChannel("in");
        routeForm.setFilteringExpression("#command.type == 'ButtonPressed'");
        routeForm.setTransformation("trans_id");
        routeForm.setActive(true);

        routeForm.setAdditionalSupplier(() -> tenant.getDomainName());

        routeData = new LinkedMultiValueMap<>();
        routeData.add("name", routeForm.getName());
        routeData.add("description", routeForm.getDescription());
        routeData.add("incomingAuthority", routeForm.getIncomingAuthority());
        routeData.add("incomingChannel", routeForm.getIncomingChannel());
        routeData.add("outgoingScheme", routeForm.getOutgoingScheme());
        routeData.add("outgoingDeviceAuthority", routeForm.getOutgoingDeviceAuthority());
        routeData.add("outgoingDeviceChannel", routeForm.getOutgoingDeviceChannel());
        routeData.add("filteringExpression", routeForm.getFilteringExpression());
        routeData.add("transformation", routeForm.getTransformation());
        routeData.add("active", "true");

        DeviceURIDealer deviceUriDealer = new DeviceURIDealer() {
        };
        SmsURIDealer smsURIDealer = new SmsURIDealer() {
        };

        Supplier<URI> outgoingUriSupplier = () -> {
            switch (routeForm.getOutgoingScheme()) {
                case DEVICE_URI_SCHEME:
                    return deviceUriDealer.toDeviceRouteURI(tenant.getDomainName(), routeForm.getOutgoingDeviceAuthority());
                case SMS_URI_SCHEME:
                    return smsURIDealer.toSmsURI(routeForm.getOutgoingSmsPhoneNumber());
                default:
                    return null;
            }
        };

        route = EventRoute.builder()
                .name(routeForm.getName())
                .description(routeForm.getDescription())
                .incoming(new RuleActor(
                        deviceUriDealer.toDeviceRouteURI(tenant.getDomainName(), routeForm.getIncomingAuthority())
                ))
                .outgoing(new RuleActor(
                        outgoingUriSupplier.get()
                ))
                .filteringExpression(routeForm.getFilteringExpression())
                .transformation(Transformation.builder().id(routeForm.getTransformation()).build())
                .active(routeForm.isActive())
                .build();

        route.getIncoming().getData().put("channel", routeForm.getIncomingChannel());
        route.getOutgoing().getData().put("channel", routeForm.getOutgoingDeviceChannel());

        registeredRoutes = new ArrayList<EventRoute>(asList(new EventRoute[]{route}));
    }

    @After
    public void tearDown() {
        reset(eventRouteService);
    }

    @Test
    public void shouldListAllRegisteredRoutes() throws Exception {
        when(eventRouteService.getAll(eq(tenant))).thenReturn(registeredRoutes);

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
    public void shouldRenderEmptyBodyWhenSchemeIsUnknown() throws Exception {
        getMockMvc().perform(get("/routes/outgoing/{0}", "unknown_scheme"))
                .andExpect(view().name("common/empty"));
    }

    @Test
    public void shouldBindErrorMessagesWhenRegistrationFailsAndGoBackToCreationForm() throws Exception {
        response = ServiceResponse.<EventRoute>builder().responseMessages(asList(new String[]{"Some error"}))
                .status(ERROR).<EventRoute>build();

        when(eventRouteService.save(eq(tenant), eq(route))).thenReturn(response);

        getMockMvc().perform(post("/routes/save").params(routeData))
                .andExpect(model().attribute("errors", equalTo(response.getResponseMessages())))
                .andExpect(model().attribute("route", equalTo(routeForm))).andExpect(view().name("routes/form"));

        verify(eventRouteService).save(eq(tenant), eq(route));
    }

    @Test
    public void shouldBindBusinessExceptionMessageWhenRegistrationFailsAndGoBackToCreationForm() throws Exception {
        String exceptionMessage = "Some business exception message";

        when(eventRouteService.save(eq(tenant), eq(route))).thenThrow(new BusinessException(exceptionMessage));

        getMockMvc().perform(post("/routes/save").params(routeData))
                .andExpect(model().attribute("errors", equalTo(asList(new String[]{exceptionMessage}))))
                .andExpect(model().attribute("route", equalTo(routeForm)))
                .andExpect(view().name("routes/form"));

        verify(eventRouteService).save(eq(tenant), eq(route));
    }

    @Test
    public void shouldRedirectToShowAfterSuccessfulRouteCreation() throws Exception {
        response = spy(ServiceResponse.<EventRoute>builder()
                .status(OK)
                .result(route)
                .<EventRoute>build());

        when(eventRouteService.save(eq(tenant), eq(route))).thenReturn(response);

        getMockMvc().perform(post("/routes/save").params(routeData))
                .andExpect(flash().attribute("message", "Route registered successfully"))
                .andExpect(redirectedUrl(MessageFormat.format("/routes/{0}", route.getId())));

        verify(eventRouteService).save(eq(tenant), eq(route));
    }

    @Test
    public void shouldShowEditForm() throws Exception {
        routeForm.setAdditionalSupplier(null);

        when(eventRouteService.getById(tenant, routeId)).thenReturn(
                ServiceResponse.<EventRoute>builder().result(route).status(OK).<EventRoute>build());

        getMockMvc().perform(get(format("/routes/{0}/edit", routeId)))
                .andExpect(model().attribute("route", equalTo(routeForm)))
                .andExpect(model().attribute("action", format("/routes/{0}", routeId)))
                .andExpect(view().name("routes/form"));
    }

    @Test
    public void shouldRedirectToShowAfterSuccessfulRouteEdit() throws Exception {
        route.setId(routeId);
        response = spy(ServiceResponse.<EventRoute>builder()
                .status(OK)
                .result(route)
                .<EventRoute>build());

        when(eventRouteService.save(eq(tenant), eq(route))).thenReturn(response);

        getMockMvc().perform(post("/routes/{0}", route.getId()).params(routeData))
                .andExpect(flash().attribute("message", "Route registered successfully"))
                .andExpect(redirectedUrl(MessageFormat.format("/routes/{0}", route.getId())));

        verify(eventRouteService).save(eq(tenant), eq(route));
    }

    @Test
    public void shouldShowRouteDetails() throws Exception {
        routeForm.setAdditionalSupplier(null);

        routeForm.setId(routeId);
        route.setId(routeId);
        when(eventRouteService.getById(tenant, route.getId())).thenReturn(
                ServiceResponse.<EventRoute>builder().result(route).status(OK).<EventRoute>build());

        getMockMvc().perform(
                get("/routes/{0}", route.getId())
        ).andExpect(model().attribute("route", equalTo(routeForm)))
                .andExpect(view().name("routes/show"));

        verify(eventRouteService).getById(tenant, route.getId());
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
        public TransformationService transformationService() {
            return mock(TransformationService.class);
        }
    }
}
