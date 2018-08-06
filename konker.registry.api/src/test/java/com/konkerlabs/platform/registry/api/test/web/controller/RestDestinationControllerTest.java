package com.konkerlabs.platform.registry.api.test.web.controller;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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
import com.konkerlabs.platform.registry.api.model.RestDestinationVO;
import com.konkerlabs.platform.registry.api.test.config.MongoTestConfig;
import com.konkerlabs.platform.registry.api.test.config.WebTestConfiguration;
import com.konkerlabs.platform.registry.api.web.controller.RestDestinationController;
import com.konkerlabs.platform.registry.api.web.wrapper.CrudResponseAdvice;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.RestDestination;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.RestDestinationService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = RestDestinationController.class)
@AutoConfigureMockMvc(secure = false)
@ContextConfiguration(classes = {
        WebTestConfiguration.class,
        MongoTestConfig.class,
        WebMvcConfig.class,
        CrudResponseAdvice.class
})
public class RestDestinationControllerTest extends WebLayerTestContext {

    private static final String NONEXIST_APPLICATION_NAME = "AppLost";

    @Autowired
    private RestDestinationService restDestinationService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private Tenant tenant;

    @Autowired
    private Application application;

    private RestDestination restDestination1;

    private RestDestination restDestination2;

    private final String BASEPATH = "restDestinations";

    @Before
    public void setUp() {
        restDestination1 = RestDestination.builder()
        		.guid(UUID.randomUUID().toString())
        		.name("Rest Dest 1")
        		.method("GET")
        		.headers(Collections.emptyMap())
        		.serviceURI("http://www.konkerlabs.com/api")
        		.serviceUsername("user1")
        		.servicePassword("xpto123")
        		.active(true)
        		.build();

        restDestination2 = RestDestination.builder()
    			.guid(UUID.randomUUID().toString())
    			.name("Rest Dest 2")
    			.method("POST")
    			.headers(Collections.singletonMap("Content-Type", "application/json"))
    			.serviceURI("http://www.konkerlabs.com/api")
    			.serviceUsername("user2")
    			.servicePassword("xpto321")
    			.active(false)
    			.build();

        when(applicationService.getByApplicationName(tenant, application.getName()))
            .thenReturn(ServiceResponseBuilder.<Application> ok().withResult(application).build());

    }

    @After
    public void tearDown() {
        Mockito.reset(restDestinationService);
    }

    @Test
    public void shouldListRestDestinations() throws Exception {
        List<RestDestination> restDestinations = new ArrayList<>();
        restDestinations.add(restDestination1);
        restDestinations.add(restDestination2);

        when(restDestinationService.findAll(tenant, application))
        	.thenReturn(
        			ServiceResponseBuilder
        				.<List<RestDestination>>ok()
                		.withResult(restDestinations)
                		.build());

        getMockMvc()
        .perform(MockMvcRequestBuilders
        		.get(MessageFormat.format("/{0}/{1}/", application.getName(), BASEPATH))
        		.contentType("application/json")
        		.accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
        .andExpect(jsonPath("$.status", is("success")))
        .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
        .andExpect(jsonPath("$.result", hasSize(2)))
        .andExpect(jsonPath("$.result[0].guid", is(restDestination1.getGuid())))
        .andExpect(jsonPath("$.result[0].name", is("Rest Dest 1")))
        .andExpect(jsonPath("$.result[0].method", is("GET")))
        .andExpect(jsonPath("$.result[0].headers", is(Collections.emptyMap())))
        .andExpect(jsonPath("$.result[0].serviceURI", is("http://www.konkerlabs.com/api")))
        .andExpect(jsonPath("$.result[0].serviceUsername", is("user1")))
        .andExpect(jsonPath("$.result[0].servicePassword", is("xpto123")))
        .andExpect(jsonPath("$.result[0].active", is(true)))
        .andExpect(jsonPath("$.result[1].guid", is(restDestination2.getGuid())))
        .andExpect(jsonPath("$.result[1].name", is("Rest Dest 2")))
        .andExpect(jsonPath("$.result[1].method", is("POST")))
        .andExpect(jsonPath("$.result[1].headers", is(Collections.singletonMap("Content-Type", "application/json"))))
        .andExpect(jsonPath("$.result[1].serviceURI", is("http://www.konkerlabs.com/api")))
        .andExpect(jsonPath("$.result[1].serviceUsername", is("user2")))
        .andExpect(jsonPath("$.result[1].servicePassword", is("xpto321")))
        .andExpect(jsonPath("$.result[1].active", is(false)));
    }

    @Test
    public void shouldReturnInternalErrorWhenListRestDestinations() throws Exception {
        when(restDestinationService.findAll(tenant, application))
                .thenReturn(ServiceResponseBuilder.<List<RestDestination>>error().build());

        getMockMvc()
        .perform(MockMvcRequestBuilders.get(MessageFormat.format("/{0}/{1}/", application.getName(), BASEPATH))
        		.accept(MediaType.APPLICATION_JSON)
        		.contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is5xxServerError())
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andExpect(jsonPath("$.code", is(HttpStatus.INTERNAL_SERVER_ERROR.value())))
        .andExpect(jsonPath("$.status", is("error")))
        .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
        .andExpect(jsonPath("$.messages").doesNotExist())
        .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    public void shouldReadRestDestinationByGuid() throws Exception {
        when(restDestinationService.getByGUID(tenant, application, restDestination1.getGuid()))
        	.thenReturn(
        			ServiceResponseBuilder
        				.<RestDestination>ok()
        				.withResult(restDestination1)
        				.build());

        getMockMvc()
        .perform(MockMvcRequestBuilders
        		.get(MessageFormat.format("/{0}/{1}/", application.getName(), BASEPATH) + restDestination1.getGuid())
        		.contentType("application/json")
        		.accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
        .andExpect(jsonPath("$.status", is("success")))
        .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
        .andExpect(jsonPath("$.result").isMap())
        .andExpect(jsonPath("$.result.guid", is(restDestination1.getGuid())))
        .andExpect(jsonPath("$.result.name", is("Rest Dest 1")))
        .andExpect(jsonPath("$.result.method", is("GET")))
        .andExpect(jsonPath("$.result.headers", is(Collections.emptyMap())))
        .andExpect(jsonPath("$.result.serviceURI", is("http://www.konkerlabs.com/api")))
        .andExpect(jsonPath("$.result.serviceUsername", is("user1")))
        .andExpect(jsonPath("$.result.servicePassword", is("xpto123")))
        .andExpect(jsonPath("$.result.active", is(true)));
    }

    @Test
    public void shouldReadWithWrongApplication() throws Exception {

        when(applicationService.getByApplicationName(tenant, NONEXIST_APPLICATION_NAME))
                .thenReturn(ServiceResponseBuilder.<Application>error().withMessage(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode()).build());

        getMockMvc().perform(MockMvcRequestBuilders.get(MessageFormat.format("/{0}/{1}/{2}", NONEXIST_APPLICATION_NAME, BASEPATH, restDestination1.getGuid()))
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
        when(restDestinationService.getByGUID(tenant, application, restDestination1.getGuid()))
        	.thenReturn(
        			ServiceResponseBuilder
        				.<RestDestination>error()
                		.withMessage(RestDestinationService.Validations.DESTINATION_NOT_FOUND.getCode())
                		.build());

        getMockMvc()
        .perform(MockMvcRequestBuilders
        		.get(MessageFormat.format("/{0}/{1}/", application.getName(), BASEPATH) + restDestination1.getGuid())
        		.accept(MediaType.APPLICATION_JSON)
        		.contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is4xxClientError())
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andExpect(jsonPath("$.code", is(HttpStatus.NOT_FOUND.value())))
        .andExpect(jsonPath("$.status", is("error")))
        .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
        .andExpect(jsonPath("$.messages[0]", is("REST Destination does not exist")))
        .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    public void shouldCreateRestDestination() throws Exception {
        when(
        	restDestinationService
        		.register(org.mockito.Matchers.any(Tenant.class), org.mockito.Matchers.any(Application.class), org.mockito.Matchers.any(RestDestination.class)))
        .thenReturn(ServiceResponseBuilder.<RestDestination>ok().withResult(restDestination1).build());

        getMockMvc()
        .perform(MockMvcRequestBuilders.post(MessageFormat.format("/{0}/{1}/", application.getName(), BASEPATH))
        		.content(getJson(new RestDestinationVO().apply(restDestination1)))
        		.contentType("application/json")
        		.accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is2xxSuccessful())
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andExpect(jsonPath("$.code", is(HttpStatus.CREATED.value())))
        .andExpect(jsonPath("$.status", is("success")))
        .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
        .andExpect(jsonPath("$.result").isMap())
        .andExpect(jsonPath("$.result.guid", is(restDestination1.getGuid())))
        .andExpect(jsonPath("$.result.name", is("Rest Dest 1")))
        .andExpect(jsonPath("$.result.method", is("GET")))
        .andExpect(jsonPath("$.result.headers", is(Collections.emptyMap())))
        .andExpect(jsonPath("$.result.serviceURI", is("http://www.konkerlabs.com/api")))
        .andExpect(jsonPath("$.result.serviceUsername", is("user1")))
        .andExpect(jsonPath("$.result.servicePassword", is("xpto123")))
        .andExpect(jsonPath("$.result.active", is(true)));
    }

    @Test
    public void shouldTryCreateRestDestinationWithBadRequest() throws Exception {
        when(
        	restDestinationService
        		.register(org.mockito.Matchers.any(Tenant.class), org.mockito.Matchers.any(Application.class), org.mockito.Matchers.any(RestDestination.class)))
        .thenReturn(ServiceResponseBuilder
        				.<RestDestination>error()
        				.withMessage(RestDestinationService.Validations.DESTINATION_NOT_FOUND.getCode())
        				.build());

        getMockMvc()
        .perform(MockMvcRequestBuilders.post(MessageFormat.format("/{0}/{1}/", application.getName(), BASEPATH))
        		.content(getJson(new RestDestinationVO().apply(restDestination1)))
        		.contentType(MediaType.APPLICATION_JSON)
        		.accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is4xxClientError())
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andExpect(jsonPath("$.code", is(HttpStatus.BAD_REQUEST.value())))
        .andExpect(jsonPath("$.status", is("error")))
        .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
        .andExpect(jsonPath("$.messages").exists())
        .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldUpdateRestDestination() throws Exception {
        when(
        	restDestinationService
        		.getByGUID(tenant, application, restDestination1.getGuid()))
        .thenReturn(ServiceResponseBuilder.<RestDestination>ok().withResult(restDestination1).build());

        when(
        	restDestinationService
        		.update(org.mockito.Matchers.any(Tenant.class), org.mockito.Matchers.any(Application.class), org.mockito.Matchers.anyString(), org.mockito.Matchers.any(RestDestination.class)))
        .thenReturn(ServiceResponseBuilder.<RestDestination>ok().withResult(restDestination1).build());

        getMockMvc()
        .perform(MockMvcRequestBuilders.put(MessageFormat.format("/{0}/{1}/", application.getName(), BASEPATH) + restDestination1.getGuid())
        		.content(getJson(new RestDestinationVO().apply(restDestination1)))
        		.contentType(MediaType.APPLICATION_JSON)
        		.accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is2xxSuccessful())
        .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
        .andExpect(jsonPath("$.status", is("success")))
        .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
        .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldReturnInternalErrorWhenUpdateRestDestination() throws Exception {
    	when(
    		restDestinationService
    			.getByGUID(tenant, application, restDestination1.getGuid()))
    	.thenReturn(ServiceResponseBuilder.<RestDestination>ok().withResult(restDestination1).build());

    	when(
    		restDestinationService
    			.update(org.mockito.Matchers.any(Tenant.class), org.mockito.Matchers.any(Application.class), org.mockito.Matchers.anyString(), org.mockito.Matchers.any(RestDestination.class)))
    	.thenReturn(ServiceResponseBuilder.<RestDestination>error().build());

    	getMockMvc()
    	.perform(MockMvcRequestBuilders.put(MessageFormat.format("/{0}/{1}/", application.getName(), BASEPATH) + restDestination1.getGuid())
    			.content(getJson(new RestDestinationVO().apply(restDestination1)))
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
    public void shouldDeleteRestDestination() throws Exception {
        when(restDestinationService.remove(tenant, application, restDestination1.getGuid()))
                .thenReturn(ServiceResponseBuilder.<RestDestination>ok().build());

        getMockMvc()
        .perform(MockMvcRequestBuilders.delete(MessageFormat.format("/{0}/{1}/", application.getName(), BASEPATH) + restDestination1.getGuid())
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

        when(applicationService.getByApplicationName(tenant, NONEXIST_APPLICATION_NAME))
                .thenReturn(ServiceResponseBuilder.<Application>error().withMessage(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode()).build());

        getMockMvc().perform(MockMvcRequestBuilders.delete(MessageFormat.format("/{0}/{1}/{2}", NONEXIST_APPLICATION_NAME, BASEPATH, restDestination1.getGuid()))
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
    public void shouldReturnInternalErrorWhenDeleteRestDestination() throws Exception {
    	when(restDestinationService.remove(tenant, application, restDestination1.getGuid()))
    	.thenReturn(ServiceResponseBuilder
    					.<RestDestination>error()
    					.withMessage(RestDestinationService.Messages.REST_DESTINATION_REMOVED_UNSUCCESSFULLY.getCode())
    					.build());

        getMockMvc()
        .perform(MockMvcRequestBuilders.delete(MessageFormat.format("/{0}/{1}/", application.getName(), BASEPATH) + restDestination1.getGuid())
        		.contentType("application/json")
        		.accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is5xxServerError())
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andExpect(jsonPath("$.code", is(HttpStatus.INTERNAL_SERVER_ERROR.value())))
        .andExpect(jsonPath("$.status", is("error")))
        .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
        .andExpect(jsonPath("$.messages").exists())
        .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    public void shouldTryDeleteNonexistentRestDestination() throws Exception {
    	when(restDestinationService.remove(tenant, application, restDestination1.getGuid()))
    	.thenReturn(
    			ServiceResponseBuilder
    				.<RestDestination>error()
    				.withMessage(RestDestinationService.Validations.DESTINATION_NOT_FOUND.getCode())
    				.build());

    	getMockMvc().perform(MockMvcRequestBuilders.delete(MessageFormat.format("/{0}/{1}/", application.getName(), BASEPATH) + restDestination1.getGuid())
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
