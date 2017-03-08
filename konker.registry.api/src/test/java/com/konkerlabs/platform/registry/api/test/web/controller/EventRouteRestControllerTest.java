package com.konkerlabs.platform.registry.api.test.web.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.konkerlabs.platform.registry.api.config.WebMvcConfig;
import com.konkerlabs.platform.registry.api.model.EventRouteVO;
import com.konkerlabs.platform.registry.api.test.config.MongoTestConfig;
import com.konkerlabs.platform.registry.api.test.config.WebTestConfiguration;
import com.konkerlabs.platform.registry.api.web.controller.EventRouteRestController;
import com.konkerlabs.platform.registry.api.web.wrapper.CrudResponseAdvice;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.EventRoute;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.Transformation;
import com.konkerlabs.platform.registry.business.model.EventRoute.RouteActor;
import com.konkerlabs.platform.registry.business.model.RestDestination;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.EventRouteService;
import com.konkerlabs.platform.registry.business.services.api.RestDestinationService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.business.services.api.TransformationService;;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = EventRouteRestController.class)
@AutoConfigureMockMvc(secure = false)
@ContextConfiguration(classes = {
        WebTestConfiguration.class,
        MongoTestConfig.class,
        WebMvcConfig.class,
        CrudResponseAdvice.class
})
public class EventRouteRestControllerTest extends WebLayerTestContext {

    @Autowired
    private EventRouteService eventRouteService;

    @Autowired
    private DeviceRegisterService deviceRegisterService;

    @Autowired
    private RestDestinationService restDestinationService;

    @Autowired
    private TransformationService transformationService;

    @Autowired
    private Tenant tenant;

    private Transformation transformation1;

    private EventRoute route1;

    private EventRoute route2;

    private EventRoute route3;

    private Device device1;

    private Device device2;

    private RestDestination rest1;

    @Before
    public void setUp() {
        transformation1 = Transformation.builder().guid("t_guid1").build();

        device1 = Device.builder().tenant(tenant).guid("d_guid1").build();
        device2 = Device.builder().tenant(tenant).guid("d_guid2").build();

        rest1 = RestDestination.builder().tenant(tenant).guid("r_guid1").build();

        Map<String, String> map = new HashMap<>();
        map.put("channel", "SBT");

        RouteActor routeActor1 = RouteActor.builder().uri(device1.toURI()).data(map).build();
        RouteActor routeActor2 = RouteActor.builder().uri(device2.toURI()).data(map).build();
        RouteActor routeActor3 = RouteActor.builder().uri(rest1.toURI()).data(map).build();

        route1 = EventRoute.builder()
                .name("name1")
                .guid("guid1")
                .incoming(routeActor1)
                .outgoing(routeActor2)
                .transformation(transformation1)
                .active(true)
                .build();

        route2 = EventRoute.builder()
                .name("name2")
                .guid("guid2")
                .incoming(routeActor2)
                .outgoing(routeActor1)
                .filteringExpression("val eq 2")
                .active(false)
                .build();

        route3 = EventRoute.builder()
                .name("name3")
                .guid("guid3")
                .incoming(routeActor1)
                .outgoing(routeActor3)
                .active(true)
                .build();

    }

    @After
    public void tearDown() {
        Mockito.reset(eventRouteService);
        Mockito.reset(deviceRegisterService);
        Mockito.reset(transformationService);
    }

    @Test
    public void shouldListEventRoutes() throws Exception {

        List<EventRoute> routes = new ArrayList<>();
        routes.add(route1);
        routes.add(route2);

        when(eventRouteService.getAll(tenant))
            .thenReturn(ServiceResponseBuilder.<List<EventRoute>> ok().withResult(routes).build());

        getMockMvc().perform(MockMvcRequestBuilders.get("/routes/")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
                    .andExpect(jsonPath("$.status", is("success")))
                    .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                    .andExpect(jsonPath("$.result", hasSize(2)))
                    .andExpect(jsonPath("$.result[0].name", is("name1")))
                    .andExpect(jsonPath("$.result[0].guid", is("guid1")))
                    .andExpect(jsonPath("$.result[0].filteringExpression").doesNotExist())
                    .andExpect(jsonPath("$.result[0].transformationGuid", is("t_guid1")))
                    .andExpect(jsonPath("$.result[0].active", is(true)))
                    .andExpect(jsonPath("$.result[1].name", is("name2")))
                    .andExpect(jsonPath("$.result[1].guid", is("guid2")))
                    .andExpect(jsonPath("$.result[1].filteringExpression", is("val eq 2")))
                    .andExpect(jsonPath("$.result[1].transformationGuid").doesNotExist())
                    .andExpect(jsonPath("$.result[1].active", is(false)))
                    ;

    }

    @Test
    public void shouldTryListEventRoutesWithInternalError() throws Exception {

        when(eventRouteService.getAll(tenant))
            .thenReturn(ServiceResponseBuilder.<List<EventRoute>> error().build());

        getMockMvc().perform(MockMvcRequestBuilders.get("/routes/")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is5xxServerError())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.code", is(HttpStatus.INTERNAL_SERVER_ERROR.value())))
                    .andExpect(jsonPath("$.status", is("error")))
                    .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                    .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldReadEventRoute() throws Exception {

        when(eventRouteService.getByGUID(tenant, route1.getGuid()))
            .thenReturn(ServiceResponseBuilder.<EventRoute> ok().withResult(route1).build());

        getMockMvc().perform(MockMvcRequestBuilders.get("/routes/" + route1.getGuid())
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
                    .andExpect(jsonPath("$.status", is("success")))
                    .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                    .andExpect(jsonPath("$.result").isMap())
                    .andExpect(jsonPath("$.result.name", is("name1")))
                    .andExpect(jsonPath("$.result.guid", is("guid1")))
                    .andExpect(jsonPath("$.result.filteringExpression").doesNotExist())
                    .andExpect(jsonPath("$.result.transformationGuid", is("t_guid1")))
                    .andExpect(jsonPath("$.result.active", is(true)));

    }

    @Test
    public void shouldTryReadEventRouteWithBadRequest() throws Exception {

        when(eventRouteService.getByGUID(tenant, route1.getGuid()))
            .thenReturn(ServiceResponseBuilder.<EventRoute> error().withMessage(EventRouteService.Validations.NAME_IN_USE.getCode()).build());

        getMockMvc().perform(MockMvcRequestBuilders.get("/routes/" + route1.getGuid())
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is4xxClientError())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.code", is(HttpStatus.NOT_FOUND.value())))
                    .andExpect(jsonPath("$.status", is("error")))
                    .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                    .andExpect(jsonPath("$.messages").exists())
                    .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldCreateEventRoute() throws Exception {

        when(eventRouteService.save(org.mockito.Matchers.any(Tenant.class), org.mockito.Matchers.any(EventRoute.class)))
            .thenReturn(ServiceResponseBuilder.<EventRoute> ok().withResult(route1).build());

        when(deviceRegisterService.getByDeviceGuid(tenant, device1.getGuid()))
            .thenReturn(ServiceResponseBuilder.<Device> ok().withResult(device1).build());

        when(deviceRegisterService.getByDeviceGuid(tenant, device2.getGuid()))
            .thenReturn(ServiceResponseBuilder.<Device> ok().withResult(device2).build());

        when(transformationService.get(tenant, transformation1.getGuid()))
            .thenReturn(ServiceResponseBuilder.<Transformation> ok().withResult(transformation1).build());

        getMockMvc().perform(MockMvcRequestBuilders.post("/routes/")
                                                   .content(getJson(new EventRouteVO().apply(route1)))
                                                   .contentType("application/json")
                                                   .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is2xxSuccessful())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.code", is(HttpStatus.CREATED.value())))
                    .andExpect(jsonPath("$.status", is("success")))
                    .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                    .andExpect(jsonPath("$.result").isMap())
                    .andExpect(jsonPath("$.result.name", is("name1")))
                    .andExpect(jsonPath("$.result.guid", is("guid1")))
                    .andExpect(jsonPath("$.result.incoming.type", is("DEVICE")))
                    .andExpect(jsonPath("$.result.incoming.guid", is("d_guid1")))
                    .andExpect(jsonPath("$.result.outgoing.type", is("DEVICE")))
                    .andExpect(jsonPath("$.result.outgoing.guid", is("d_guid2")))
                    .andExpect(jsonPath("$.result.active", is(true)));

    }

    @Test
    public void shouldCreateEventRouteToRestDestination() throws Exception {

        when(eventRouteService.save(org.mockito.Matchers.any(Tenant.class), org.mockito.Matchers.any(EventRoute.class)))
            .thenReturn(ServiceResponseBuilder.<EventRoute> ok().withResult(route3).build());

        when(deviceRegisterService.getByDeviceGuid(tenant, device1.getGuid()))
            .thenReturn(ServiceResponseBuilder.<Device> ok().withResult(device1).build());

        when(restDestinationService.getByGUID(tenant, rest1.getGuid()))
            .thenReturn(ServiceResponseBuilder.<RestDestination> ok().withResult(rest1).build());

        getMockMvc().perform(MockMvcRequestBuilders.post("/routes/")
                                                   .content(getJson(new EventRouteVO().apply(route3)))
                                                   .contentType("application/json")
                                                   .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is2xxSuccessful())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.code", is(HttpStatus.CREATED.value())))
                    .andExpect(jsonPath("$.status", is("success")))
                    .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                    .andExpect(jsonPath("$.result").isMap())
                    .andExpect(jsonPath("$.result.name", is("name3")))
                    .andExpect(jsonPath("$.result.guid", is("guid3")))
                    .andExpect(jsonPath("$.result.incoming.type", is("DEVICE")))
                    .andExpect(jsonPath("$.result.incoming.guid", is("d_guid1")))
                    .andExpect(jsonPath("$.result.outgoing.type", is("REST")))
                    .andExpect(jsonPath("$.result.outgoing.guid", is("r_guid1")))
                    .andExpect(jsonPath("$.result.active", is(true)));

    }

    @Test
    public void shouldTryCreateEventRouteWithBadRequest() throws Exception {

        when(eventRouteService.save(org.mockito.Matchers.any(Tenant.class), org.mockito.Matchers.any(EventRoute.class)))
            .thenReturn(ServiceResponseBuilder.<EventRoute> error().withMessage(EventRouteService.Validations.NAME_IN_USE.getCode()).build());

        when(deviceRegisterService.getByDeviceGuid(tenant, device1.getGuid()))
            .thenReturn(ServiceResponseBuilder.<Device> ok().withResult(device1).build());

        when(deviceRegisterService.getByDeviceGuid(tenant, device2.getGuid()))
            .thenReturn(ServiceResponseBuilder.<Device> ok().withResult(device2).build());

        when(transformationService.get(tenant, transformation1.getGuid()))
            .thenReturn(ServiceResponseBuilder.<Transformation> ok().withResult(transformation1).build());

        getMockMvc().perform(MockMvcRequestBuilders.post("/routes/")
                                               .content(getJson(new EventRouteVO().apply(route1)))
                                               .contentType("application/json")
                                               .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is4xxClientError())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.code", is(HttpStatus.BAD_REQUEST.value())))
                    .andExpect(jsonPath("$.status", is("error")))
                    .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                    .andExpect(jsonPath("$.messages").exists())
                    .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldTryCreateEventRouteWithInvalidOutgoing() throws Exception {

        when(eventRouteService.save(org.mockito.Matchers.any(Tenant.class), org.mockito.Matchers.any(EventRoute.class)))
            .thenReturn(ServiceResponseBuilder.<EventRoute> ok().withResult(route1).build());

        when(deviceRegisterService.getByDeviceGuid(tenant, device1.getGuid()))
            .thenReturn(ServiceResponseBuilder.<Device> error().withMessage(DeviceRegisterService.Validations.DEVICE_GUID_DOES_NOT_EXIST.getCode()).withResult(device1).build());

        when(deviceRegisterService.getByDeviceGuid(tenant, device2.getGuid()))
            .thenReturn(ServiceResponseBuilder.<Device> ok().withResult(device2).build());

        when(transformationService.get(tenant, transformation1.getGuid()))
            .thenReturn(ServiceResponseBuilder.<Transformation> ok().withResult(transformation1).build());

        getMockMvc().perform(MockMvcRequestBuilders.post("/routes/")
                                                   .content(getJson(new EventRouteVO().apply(route3)))
                                                   .contentType("application/json")
                                                   .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is5xxServerError())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.code", is(HttpStatus.INTERNAL_SERVER_ERROR.value())))
                    .andExpect(jsonPath("$.status", is("error")))
                    .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                    .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldTryCreateEventRouteWithInvalidTransformation() throws Exception {

        when(deviceRegisterService.getByDeviceGuid(tenant, device1.getGuid()))
            .thenReturn(ServiceResponseBuilder.<Device> ok().withResult(device1).build());

        when(deviceRegisterService.getByDeviceGuid(tenant, device2.getGuid()))
            .thenReturn(ServiceResponseBuilder.<Device> ok().withResult(device2).build());

        when(transformationService.get(tenant, transformation1.getGuid()))
            .thenReturn(ServiceResponseBuilder.<Transformation> error().withMessage(TransformationService.Validations.TRANSFORMATION_NOT_FOUND.getCode()).build());

        getMockMvc().perform(MockMvcRequestBuilders.post("/routes/")
                                               .content(getJson(new EventRouteVO().apply(route1)))
                                               .contentType("application/json")
                                               .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is4xxClientError())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.code", is(HttpStatus.BAD_REQUEST.value())))
                    .andExpect(jsonPath("$.status", is("error")))
                    .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                    .andExpect(jsonPath("$.messages").exists())
                    .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldUpdateEventRoute() throws Exception {

        when(eventRouteService.getByGUID(tenant, route1.getGuid()))
            .thenReturn(ServiceResponseBuilder.<EventRoute> ok().withResult(route1).build());

        when(eventRouteService.update(org.mockito.Matchers.any(Tenant.class), org.mockito.Matchers.anyString(), org.mockito.Matchers.any(EventRoute.class)))
            .thenReturn(ServiceResponseBuilder.<EventRoute> ok().withResult(route1).build());

        when(deviceRegisterService.getByDeviceGuid(tenant, device1.getGuid()))
            .thenReturn(ServiceResponseBuilder.<Device> ok().withResult(device1).build());

        when(deviceRegisterService.getByDeviceGuid(tenant, device2.getGuid()))
            .thenReturn(ServiceResponseBuilder.<Device> ok().withResult(device2).build());

        when(transformationService.get(tenant, transformation1.getGuid()))
            .thenReturn(ServiceResponseBuilder.<Transformation> ok().withResult(transformation1).build());

        getMockMvc().perform(MockMvcRequestBuilders.put("/routes/" + route1.getGuid())
                                                   .content(getJson(new EventRouteVO().apply(route1)))
                                                   .contentType("application/json")
                                                   .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is2xxSuccessful())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
                    .andExpect(jsonPath("$.status", is("success")))
                    .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                    .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldUpdateEventRouteWithoutTransformation() throws Exception {

        when(eventRouteService.getByGUID(tenant, route2.getGuid()))
            .thenReturn(ServiceResponseBuilder.<EventRoute> ok().withResult(route2).build());

        when(eventRouteService.update(org.mockito.Matchers.any(Tenant.class), org.mockito.Matchers.anyString(), org.mockito.Matchers.any(EventRoute.class)))
            .thenReturn(ServiceResponseBuilder.<EventRoute> ok().withResult(route2).build());

        when(deviceRegisterService.getByDeviceGuid(tenant, device1.getGuid()))
            .thenReturn(ServiceResponseBuilder.<Device> ok().withResult(device1).build());

        when(deviceRegisterService.getByDeviceGuid(tenant, device2.getGuid()))
            .thenReturn(ServiceResponseBuilder.<Device> ok().withResult(device2).build());

        getMockMvc().perform(MockMvcRequestBuilders.put("/routes/" + route2.getGuid())
                                                   .content(getJson(new EventRouteVO().apply(route2)))
                                                   .contentType("application/json")
                                                   .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is2xxSuccessful())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
                    .andExpect(jsonPath("$.status", is("success")))
                    .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                    .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldTryUpdateEventRouteWithoutOutgoing() throws Exception {

        when(eventRouteService.getByGUID(tenant, route1.getGuid()))
            .thenReturn(ServiceResponseBuilder.<EventRoute> ok().withResult(route1).build());

        when(eventRouteService.update(org.mockito.Matchers.any(Tenant.class), org.mockito.Matchers.anyString(), org.mockito.Matchers.any(EventRoute.class)))
            .thenReturn(ServiceResponseBuilder.<EventRoute> error().withMessage(EventRoute.Validations.OUTGOING_ACTOR_CHANNEL_NULL.getCode()).build());

        when(deviceRegisterService.getByDeviceGuid(tenant, device1.getGuid()))
            .thenReturn(ServiceResponseBuilder.<Device> ok().withResult(device1).build());

        when(deviceRegisterService.getByDeviceGuid(tenant, device2.getGuid()))
            .thenReturn(ServiceResponseBuilder.<Device> ok().withResult(device2).build());

        when(transformationService.get(tenant, transformation1.getGuid()))
            .thenReturn(ServiceResponseBuilder.<Transformation> ok().withResult(transformation1).build());

        EventRouteVO eventRouteWithoutOutgoingOutgoing = new EventRouteVO().apply(route1);
        eventRouteWithoutOutgoingOutgoing.getOutgoing().setType(null);

        getMockMvc().perform(MockMvcRequestBuilders.put("/routes/" + route1.getGuid())
                                                   .content(getJson(eventRouteWithoutOutgoingOutgoing))
                                                   .contentType("application/json")
                                                   .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is4xxClientError())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.code", is(HttpStatus.BAD_REQUEST.value())))
                    .andExpect(jsonPath("$.status", is("error")))
                    .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                    .andExpect(jsonPath("$.messages").exists())
                    .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldTryUpdateEventRouteWithInternalError() throws Exception {

        when(eventRouteService.getByGUID(tenant, route1.getGuid()))
            .thenReturn(ServiceResponseBuilder.<EventRoute> ok().withResult(route1).build());

        when(eventRouteService.update(org.mockito.Matchers.any(Tenant.class), org.mockito.Matchers.anyString(), org.mockito.Matchers.any(EventRoute.class)))
            .thenReturn(ServiceResponseBuilder.<EventRoute> error().build());

        when(deviceRegisterService.getByDeviceGuid(tenant, device1.getGuid()))
            .thenReturn(ServiceResponseBuilder.<Device> ok().withResult(device1).build());

        when(deviceRegisterService.getByDeviceGuid(tenant, device2.getGuid()))
            .thenReturn(ServiceResponseBuilder.<Device> ok().withResult(device2).build());

        when(transformationService.get(tenant, transformation1.getGuid()))
            .thenReturn(ServiceResponseBuilder.<Transformation> ok().withResult(transformation1).build());

        getMockMvc().perform(MockMvcRequestBuilders.put("/routes/" + route1.getGuid())
                                                   .content(getJson(new EventRouteVO().apply(route1)))
                                                   .contentType("application/json")
                                                   .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is5xxServerError())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.code", is(HttpStatus.INTERNAL_SERVER_ERROR.value())))
                    .andExpect(jsonPath("$.status", is("error")))
                    .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                    .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldDeleteEventRoute() throws Exception {

        when(eventRouteService.remove(tenant, route1.getGuid()))
            .thenReturn(ServiceResponseBuilder.<EventRoute> ok().build());

        getMockMvc().perform(MockMvcRequestBuilders.delete("/routes/" + route1.getGuid())
                                                   .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is2xxSuccessful())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.code", is(HttpStatus.NO_CONTENT.value())))
                    .andExpect(jsonPath("$.status", is("success")))
                    .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                    .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldTryDeleteEventRouteWithInternalError() throws Exception {

        when(eventRouteService.remove(tenant, route1.getGuid()))
            .thenReturn(ServiceResponseBuilder.<EventRoute> error().build());

        getMockMvc().perform(MockMvcRequestBuilders.delete("/routes/" + route1.getGuid())
                                                   .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is5xxServerError())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.code", is(HttpStatus.INTERNAL_SERVER_ERROR.value())))
                    .andExpect(jsonPath("$.status", is("error")))
                    .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                    .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldTryDeleteNonexistentEventRoute() throws Exception {

        when(eventRouteService.remove(tenant, route1.getGuid()))
            .thenReturn(ServiceResponseBuilder.<EventRoute> error().withMessage(EventRouteService.Validations.EVENT_ROUTE_NOT_FOUND.getCode()).build());

        getMockMvc().perform(MockMvcRequestBuilders.delete("/routes/" + route1.getGuid())
                                                   .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is4xxClientError())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.code", is(HttpStatus.NOT_FOUND.value())))
                    .andExpect(jsonPath("$.status", is("error")))
                    .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                    .andExpect(jsonPath("$.messages").exists())
                    .andExpect(jsonPath("$.result").doesNotExist());

    }

}
