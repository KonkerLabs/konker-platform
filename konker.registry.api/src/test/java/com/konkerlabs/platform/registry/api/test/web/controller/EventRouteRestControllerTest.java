package com.konkerlabs.platform.registry.api.test.web.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.List;

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
import com.konkerlabs.platform.registry.business.model.EventRoute;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.Transformation;
import com.konkerlabs.platform.registry.business.services.api.EventRouteService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = EventRouteRestController.class)
@AutoConfigureMockMvc(secure = false)
@ContextConfiguration(classes = {
        WebTestConfiguration.class,
        MongoTestConfig.class,
        WebMvcConfig.class
})
public class EventRouteRestControllerTest extends WebLayerTestContext {

    @Autowired
    private EventRouteService eventRouteService;

    @Autowired
    private Tenant tenant;

    private Transformation transformation1;

    private EventRoute route1;

    private EventRoute route2;

    @Before
    public void setUp() {
        transformation1 = Transformation.builder().guid("t_guid1").build();
        route1 = EventRoute.builder().name("name1").guid("guid1").transformation(transformation1).active(true).build();
        route2 = EventRoute.builder().name("name2").guid("guid2").filteringExpression("val eq 2").active(false).build();
    }

    @After
    public void tearDown() {
        Mockito.reset(eventRouteService);
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
                    .andExpect(jsonPath("$.httpStatus", is(HttpStatus.OK.value())))
                    .andExpect(jsonPath("$.status", is("OK")))
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
                    .andExpect(jsonPath("$.httpStatus", is(HttpStatus.INTERNAL_SERVER_ERROR.value())))
                    .andExpect(jsonPath("$.status", is("ERROR")))
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
                    .andExpect(jsonPath("$.httpStatus", is(HttpStatus.OK.value())))
                    .andExpect(jsonPath("$.status", is("OK")))
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
                    .andExpect(jsonPath("$.httpStatus", is(HttpStatus.BAD_REQUEST.value())))
                    .andExpect(jsonPath("$.status", is("ERROR")))
                    .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                    .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldCreateEventRoute() throws Exception {

        when(eventRouteService.save(org.mockito.Matchers.any(Tenant.class), org.mockito.Matchers.any(EventRoute.class)))
            .thenReturn(ServiceResponseBuilder.<EventRoute> ok().withResult(route1).build());

        getMockMvc().perform(MockMvcRequestBuilders.post("/routes/")
                                                   .content(getJson(new EventRouteVO(route1)))
                                                   .contentType("application/json")
                                                   .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is2xxSuccessful())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.httpStatus", is(HttpStatus.CREATED.value())))
                    .andExpect(jsonPath("$.status", is("OK")))
                    .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                    .andExpect(jsonPath("$.result").isMap())
                    .andExpect(jsonPath("$.result.name", is("name1")))
                    .andExpect(jsonPath("$.result.guid", is("guid1")))
                    .andExpect(jsonPath("$.result.active", is(true)));

    }

    @Test
    public void shouldTryCreateEventRouteWithBadRequest() throws Exception {

        when(eventRouteService.save(org.mockito.Matchers.any(Tenant.class), org.mockito.Matchers.any(EventRoute.class)))
            .thenReturn(ServiceResponseBuilder.<EventRoute> error().withMessage(EventRouteService.Validations.NAME_IN_USE.getCode()).build());

        getMockMvc().perform(MockMvcRequestBuilders.post("/routes/")
                                               .content(getJson(new EventRouteVO(route1)))
                                               .contentType("application/json")
                                               .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is4xxClientError())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.httpStatus", is(HttpStatus.BAD_REQUEST.value())))
                    .andExpect(jsonPath("$.status", is("ERROR")))
                    .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                    .andExpect(jsonPath("$.result").doesNotExist());

    }


    @Test
    public void shouldUpdateEventRoute() throws Exception {

        when(eventRouteService.getByGUID(tenant, route1.getGuid()))
            .thenReturn(ServiceResponseBuilder.<EventRoute> ok().withResult(route1).build());

        when(eventRouteService.update(org.mockito.Matchers.any(Tenant.class), org.mockito.Matchers.anyString(), org.mockito.Matchers.any(EventRoute.class)))
            .thenReturn(ServiceResponseBuilder.<EventRoute> ok().withResult(route1).build());

        getMockMvc().perform(MockMvcRequestBuilders.put("/routes/" + route1.getGuid())
                                                   .content(getJson(new EventRouteVO(route1)))
                                                   .contentType("application/json")
                                                   .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is2xxSuccessful())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.httpStatus", is(HttpStatus.OK.value())))
                    .andExpect(jsonPath("$.status", is("OK")))
                    .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                    .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldTryUpdateEventRouteWithInternalError() throws Exception {

        when(eventRouteService.getByGUID(tenant, route1.getGuid()))
            .thenReturn(ServiceResponseBuilder.<EventRoute> ok().withResult(route1).build());

        when(eventRouteService.update(org.mockito.Matchers.any(Tenant.class), org.mockito.Matchers.anyString(), org.mockito.Matchers.any(EventRoute.class)))
            .thenReturn(ServiceResponseBuilder.<EventRoute> error().build());

        getMockMvc().perform(MockMvcRequestBuilders.put("/routes/" + route1.getGuid())
                                                   .content(getJson(new EventRouteVO(route1)))
                                                   .contentType("application/json")
                                                   .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is5xxServerError())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.httpStatus", is(HttpStatus.INTERNAL_SERVER_ERROR.value())))
                    .andExpect(jsonPath("$.status", is("ERROR")))
                    .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                    .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldDeleteEventRoute() throws Exception {

        when(eventRouteService.remove(tenant, route1.getGuid()))
            .thenReturn(ServiceResponseBuilder.<EventRoute> ok().build());

        getMockMvc().perform(MockMvcRequestBuilders.delete("/routes/" + route1.getGuid())
                                                   .content(getJson(new EventRouteVO(route1)))
                                                   .contentType("application/json")
                                                   .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is2xxSuccessful())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.httpStatus", is(HttpStatus.NO_CONTENT.value())))
                    .andExpect(jsonPath("$.status", is("OK")))
                    .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                    .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldTryDeleteEventRouteWithInternalError() throws Exception {

        when(eventRouteService.remove(tenant, route1.getGuid()))
            .thenReturn(ServiceResponseBuilder.<EventRoute> error().build());

        getMockMvc().perform(MockMvcRequestBuilders.delete("/routes/" + route1.getGuid())
                                                   .content(getJson(new EventRouteVO(route1)))
                                                   .contentType("application/json")
                                                   .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is5xxServerError())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.httpStatus", is(HttpStatus.INTERNAL_SERVER_ERROR.value())))
                    .andExpect(jsonPath("$.status", is("ERROR")))
                    .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                    .andExpect(jsonPath("$.result").doesNotExist());

    }

}
