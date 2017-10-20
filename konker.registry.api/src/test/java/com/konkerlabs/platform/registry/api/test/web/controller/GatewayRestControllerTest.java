package com.konkerlabs.platform.registry.api.test.web.controller;

import com.konkerlabs.platform.registry.api.config.WebMvcConfig;
import com.konkerlabs.platform.registry.api.model.GatewayVO;
import com.konkerlabs.platform.registry.api.test.config.MongoTestConfig;
import com.konkerlabs.platform.registry.api.test.config.WebTestConfiguration;
import com.konkerlabs.platform.registry.api.web.controller.GatewayRestController;
import com.konkerlabs.platform.registry.api.web.wrapper.CrudResponseAdvice;
import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.services.api.*;
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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = GatewayRestController.class)
@AutoConfigureMockMvc(secure = false)
@ContextConfiguration(classes = {
        WebTestConfiguration.class,
        MongoTestConfig.class,
        WebMvcConfig.class,
        CrudResponseAdvice.class
})
public class GatewayRestControllerTest extends WebLayerTestContext {

    private static final String NONEXIST_APPLICATION_NANE = "AppLost";

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private Tenant tenant;

    @Autowired
    private Application application;

    @Autowired
    private GatewayService gatewayService;

    @Autowired
    private LocationSearchService locationSearchService;

    private Location location;

    private Gateway gateway;

    private String BASEPATH = "gateways";

    private String INVALID_GUID = "000000-aaa";

    @Before
    public void setUp() {

        location = Location.builder()
                .tenant(tenant)
                .application(application)
                .guid(UUID.randomUUID().toString())
                .name("13th floor")
                .build();

        gateway = new Gateway();
        gateway.setName("hdxzbgh2ti");
        gateway.setDescription("w2f4ep5ksu");
        gateway.setGuid("2cdc391d-6a31-4103-9679-52cb6f2e5df5");
        gateway.setTenant(tenant);
        gateway.setApplication(application);
        gateway.setLocation(location);

        when(applicationService.getByApplicationName(tenant, application.getName()))
            .thenReturn(ServiceResponseBuilder.<Application> ok().withResult(application).build());

        when(locationSearchService.findByName(tenant, application, location.getName(), false))
            .thenReturn(ServiceResponseBuilder.<Location> ok().withResult(location).build());

        when(gatewayService.getByGUID(tenant, application, gateway.getGuid()))
                .thenReturn(ServiceResponseBuilder.<Gateway> ok().withResult(gateway).build());

        when(gatewayService.getByGUID(tenant, application, INVALID_GUID))
                .thenReturn(ServiceResponseBuilder.<Gateway> error().withMessage(GatewayService.Validations.GATEWAY_NOT_FOUND.getCode()).build());

    }

    @After
    public void tearDown() {
        Mockito.reset(applicationService);
    }


    @Test
    public void shouldListGateways() throws Exception {

        List<Gateway> gateways = new ArrayList<>();
        gateways.add(gateway);

        when(gatewayService.getAll(tenant, application))
                .thenReturn(ServiceResponseBuilder.<List<Gateway>>ok().withResult(gateways).build());

        getMockMvc().perform(MockMvcRequestBuilders.get(MessageFormat.format("/{0}/{1}/", application.getName(), BASEPATH))
                .contentType("application/json")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                .andExpect(jsonPath("$.result", hasSize(1)))
                .andExpect(jsonPath("$.result[0].name", is(gateway.getName())))
                .andExpect(jsonPath("$.result[0].description", is(gateway.getDescription())))
                .andExpect(jsonPath("$.result[0].locationName", is(gateway.getLocation().getName())))
                .andExpect(jsonPath("$.result[0].guid", is(gateway.getGuid())))
                .andExpect(jsonPath("$.result[0].active", is(false)))
                ;

    }

    @Test
    public void shouldReadGateway() throws Exception {

        getMockMvc().perform(MockMvcRequestBuilders
        		.get(MessageFormat.format("/{0}/{1}/{2}", application.getName(), BASEPATH, gateway.getGuid()))
        		.contentType("application/json")
        		.accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                .andExpect(jsonPath("$.result").isMap())
                .andExpect(jsonPath("$.result.name", is(gateway.getName())))
                .andExpect(jsonPath("$.result.guid", is(gateway.getGuid())))
                .andExpect(jsonPath("$.result.locationName", is(location.getName())))
                ;

    }

    @Test
    public void shouldReadWithWrongApplication() throws Exception {

        when(applicationService.getByApplicationName(tenant, NONEXIST_APPLICATION_NANE))
                .thenReturn(ServiceResponseBuilder.<Application>error().withMessage(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode()).build());

        getMockMvc().perform(MockMvcRequestBuilders
                    .get(MessageFormat.format("/{0}/{1}/{2}", NONEXIST_APPLICATION_NANE, BASEPATH, location.getName()))
                    .contentType("application/json")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is4xxClientError())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.code", is(HttpStatus.NOT_FOUND.value())))
                    .andExpect(jsonPath("$.status", is("error")))
                    .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                    .andExpect(jsonPath("$.messages[0]", is("Application does not exist")))
                    .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldReturnNotFoundWhenReadByGuid() throws Exception {

        getMockMvc().perform(MockMvcRequestBuilders
        		.get(MessageFormat.format("/{0}/{1}/{2}", application.getName(), BASEPATH, INVALID_GUID))
        		.accept(MediaType.APPLICATION_JSON)
        		.contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.NOT_FOUND.value())))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.messages").exists())
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    public void shouldCreateGateway() throws Exception {

        when(gatewayService.save(Mockito.any(Tenant.class), Mockito.any(Application.class), Mockito.any(Gateway.class)))
            .thenReturn(ServiceResponseBuilder.<Gateway>ok()
                    .withResult(gateway)
                    .build());

        getMockMvc().perform(MockMvcRequestBuilders
                .post(MessageFormat.format("/{0}/{1}", application.getName(), BASEPATH))
        		.content(getJson(new GatewayVO().apply(gateway)))
        		.contentType("application/json")
        		.accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.CREATED.value())))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                .andExpect(jsonPath("$.messages").doesNotExist())
                .andExpect(jsonPath("$.result").isMap())
                .andExpect(jsonPath("$.result.guid", is(gateway.getGuid())))
                .andExpect(jsonPath("$.result.locationName", is(location.getName())))
                ;

    }

    @Test
    public void shouldTryCreateGatewayWithBadRequest() throws Exception {

        when(gatewayService.save(Mockito.any(Tenant.class), Mockito.any(Application.class), Mockito.any(Gateway.class)))
                .thenReturn(ServiceResponseBuilder.<Gateway>error()
                        .withMessage(GatewayService.Validations.NAME_IN_USE.getCode())
                        .build());

        getMockMvc().perform(MockMvcRequestBuilders
                .post(MessageFormat.format("/{0}/{1}", application.getName(), BASEPATH))
                .content(getJson(new GatewayVO().apply(gateway)))
        		.contentType(MediaType.APPLICATION_JSON)
        		.accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.messages[0]", is("Gateway name is already in use")))
                .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldUpdateGateway() throws Exception {

        when(gatewayService.update(tenant, application, gateway.getGuid(), gateway))
            .thenReturn(ServiceResponseBuilder.<Gateway>ok()
                    .withResult(gateway)
                    .build());

        getMockMvc().perform(MockMvcRequestBuilders
                .put(MessageFormat.format("/{0}/{1}/{2}", application.getName(), BASEPATH, gateway.getGuid()))
                .content(getJson(new GatewayVO().apply(gateway)))
        		.contentType(MediaType.APPLICATION_JSON)
        		.accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldTryUpdateInvalidGuid() throws Exception {

        getMockMvc().perform(MockMvcRequestBuilders
                .put(MessageFormat.format("/{0}/{1}/{2}", application.getName(), BASEPATH, INVALID_GUID))
                .content(getJson(new GatewayVO().apply(gateway)))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.messages[0]", is("Gateway not found")))
                .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldReturnInternalErrorWhenUpdateGateway() throws Exception {

        when(gatewayService.update(tenant, application, gateway.getGuid(), gateway))
            .thenReturn(ServiceResponseBuilder.<Gateway>error().build());

    	getMockMvc().perform(MockMvcRequestBuilders
    	        .put(MessageFormat.format("/{0}/{1}/{2}", application.getName(), BASEPATH, gateway.getGuid()))
    	        .content(getJson(new GatewayVO().apply(gateway)))
    			.contentType(MediaType.APPLICATION_JSON)
    			.accept(MediaType.APPLICATION_JSON))
            	.andExpect(status().is5xxServerError())
            	.andExpect(content().contentType("application/json;charset=UTF-8"))
            	.andExpect(jsonPath("$.code", is(HttpStatus.INTERNAL_SERVER_ERROR.value())))
            	.andExpect(jsonPath("$.status", is("error")))
            	.andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
            	.andExpect(jsonPath("$.messages").doesNotExist())
            	.andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    public void shouldDeleteGateway() throws Exception {

        when(gatewayService.remove(tenant, application, gateway.getGuid()))
            .thenReturn(ServiceResponseBuilder.<Gateway>ok()
                    .build());

        getMockMvc().perform(MockMvcRequestBuilders
                .delete(MessageFormat.format("/{0}/{1}/{2}", application.getName(), BASEPATH, gateway.getGuid()))
        		.contentType("application/json")
        		.accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.NO_CONTENT.value())))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    public void shouldTryDeleteWithWrongApplication() throws Exception {

        when(applicationService.getByApplicationName(tenant, NONEXIST_APPLICATION_NANE))
                .thenReturn(ServiceResponseBuilder.<Application>error().withMessage(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode()).build());

        getMockMvc().perform(MockMvcRequestBuilders
                    .delete(MessageFormat.format("/{0}/{1}/{2}", NONEXIST_APPLICATION_NANE, BASEPATH, location.getName()))
                    .contentType("application/json")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is4xxClientError())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.code", is(HttpStatus.NOT_FOUND.value())))
                    .andExpect(jsonPath("$.status", is("error")))
                    .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                    .andExpect(jsonPath("$.messages[0]", is("Application does not exist")))
                    .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldReturnInternalErrorWhenDeleteGateway() throws Exception {

        when(gatewayService.remove(tenant, application, gateway.getGuid()))
            .thenReturn(ServiceResponseBuilder.<Gateway> error().build());

        getMockMvc().perform(MockMvcRequestBuilders
                .delete(MessageFormat.format("/{0}/{1}/{2}", application.getName(), BASEPATH, gateway.getGuid()))
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
    public void shouldTryDeleteNonExistentGateway() throws Exception {

        when(gatewayService.remove(tenant, application, INVALID_GUID))
                .thenReturn(ServiceResponseBuilder.<Gateway> error().withMessage(GatewayService.Validations.GATEWAY_NOT_FOUND.getCode()).build());

        getMockMvc().perform(MockMvcRequestBuilders
    	        .delete(MessageFormat.format("/{0}/{1}/{2}", application.getName(), BASEPATH, INVALID_GUID))
    			.contentType("application/json")
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
