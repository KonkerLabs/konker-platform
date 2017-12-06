package com.konkerlabs.platform.registry.api.test.web.controller;

import com.konkerlabs.platform.registry.api.config.WebMvcConfig;
import com.konkerlabs.platform.registry.api.model.ApplicationVO;
import com.konkerlabs.platform.registry.api.test.config.MongoTestConfig;
import com.konkerlabs.platform.registry.api.test.config.WebTestConfiguration;
import com.konkerlabs.platform.registry.api.web.controller.ApplicationRestController;
import com.konkerlabs.platform.registry.api.web.wrapper.CrudResponseAdvice;
import com.konkerlabs.platform.registry.business.model.AlertTrigger;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.HealthAlert;
import com.konkerlabs.platform.registry.business.model.HealthAlert.HealthAlertSeverity;
import com.konkerlabs.platform.registry.business.model.HealthAlert.Solution;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.ApplicationServiceImpl;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.HealthAlertService;
import com.konkerlabs.platform.registry.business.services.api.HealthAlertService.Validations;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
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
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ApplicationRestController.class)
@AutoConfigureMockMvc(secure = false)
@ContextConfiguration(classes = {
        WebTestConfiguration.class,
        MongoTestConfig.class,
        WebMvcConfig.class,
        CrudResponseAdvice.class
})
public class ApplicationRestControllerTest extends WebLayerTestContext {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private HealthAlertService healthAlertService;

    @Autowired
    private Tenant tenant;

    private Application defaultApplication;
    
    private Application application0;

    private Application application1;

    private Application application2;

    private HealthAlert health1;
    private HealthAlert health2;
    private List<HealthAlert> healths;

    @Before
    public void setUp() {
        defaultApplication = Application.builder()
        		.name(ApplicationServiceImpl.DEFAULT_APPLICATION_ALIAS)
        		.friendlyName("Smart Frig")
        		.description("Description of smartff")
        		.qualifier(tenant.getName())
        		.registrationDate(Instant.now())
        		.build();
    	
        application0 = Application.builder()
        		.name(tenant.getDomainName())
        		.friendlyName("Smart Frig")
        		.description("Description of smartff")
        		.qualifier(tenant.getName())
        		.registrationDate(Instant.now())
        		.build();
    	
        application1 = Application.builder()
        		.name("smartff")
        		.friendlyName("Smart Frig")
        		.description("Description of smartff")
        		.qualifier(tenant.getName())
        		.registrationDate(Instant.now())
        		.build();

        application2 = Application.builder()
        		.name("konkerff")
        		.friendlyName("Konker Frig")
        		.description("Description of konkerff")
        		.qualifier(tenant.getName())
        		.registrationDate(Instant.now())
        		.build();

        Instant registrationDate = Instant.ofEpochMilli(1495716970000l).minusSeconds(3600l);
        health1 = HealthAlert.builder()
				.guid("7d51c242-81db-11e6-a8c2-0746f976f223")
				.severity(HealthAlertSeverity.FAIL)
				.description("No message received from the device for a long time.")
				.registrationDate(registrationDate)
				.lastChange(Instant.ofEpochMilli(1495716970000l))
        		.type(AlertTrigger.AlertTriggerType.SILENCE)
        		.deviceGuid("guid1")
        		.triggerGuid("7d51c242-81db-11e6-a8c2-0746f976f666")
        		.build();

		health2 = HealthAlert.builder()
				.guid("7d51c242-81db-11e6-a8c2-0746f976f223")
				.severity(HealthAlertSeverity.OK)
				.description("No message received from the device for a long time.")
				.registrationDate(registrationDate)
				.lastChange(Instant.ofEpochMilli(1495716970000l))
        		.type(AlertTrigger.AlertTriggerType.SILENCE)
        		.deviceGuid("guid1")
        		.triggerGuid("7d51c242-81db-11e6-a8c2-0746f976f666")
        		.build();

		healths = Arrays.asList(health1, health2);
    }

    @After
    public void tearDown() {
        Mockito.reset(applicationService);
    }

    @Test
    public void shouldListApplications() throws Exception {
        List<Application> applications = new ArrayList<>();
        applications.add(application0);
        applications.add(application1);
        applications.add(application2);

        when(applicationService.findAll(tenant))
        	.thenReturn(
        			ServiceResponseBuilder
        				.<List<Application>>ok()
                		.withResult(applications)
                		.build());
        
        when(applicationService.isDefaultApplication(application0, tenant))
    	.thenReturn(true);

        getMockMvc()
        .perform(MockMvcRequestBuilders
        		.get("/applications/")
        		.contentType("application/json")
        		.accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
        .andExpect(jsonPath("$.status", is("success")))
        .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
        .andExpect(jsonPath("$.result", hasSize(3)))
        .andExpect(jsonPath("$.result[0].name", is(ApplicationServiceImpl.DEFAULT_APPLICATION_ALIAS)))
        .andExpect(jsonPath("$.result[0].friendlyName", is("Smart Frig")))
        .andExpect(jsonPath("$.result[0].description", is("Description of smartff")))
        .andExpect(jsonPath("$.result[1].name", is("smartff")))
        .andExpect(jsonPath("$.result[1].friendlyName", is("Smart Frig")))
        .andExpect(jsonPath("$.result[1].description", is("Description of smartff")))
        .andExpect(jsonPath("$.result[2].name", is("konkerff")))
        .andExpect(jsonPath("$.result[2].friendlyName", is("Konker Frig")))
        .andExpect(jsonPath("$.result[2].description", is("Description of konkerff")));
        
    }

    @Test
    public void shouldReturnInternalErrorWhenListApplications() throws Exception {
        when(applicationService.findAll(tenant))
                .thenReturn(ServiceResponseBuilder.<List<Application>>error().build());

        getMockMvc()
        .perform(MockMvcRequestBuilders.get("/applications/")
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
    public void shouldReadApplicationByName() throws Exception {
        when(applicationService.getByApplicationName(tenant, application1.getName()))
        	.thenReturn(
        			ServiceResponseBuilder
        				.<Application>ok()
        				.withResult(application1)
        				.build());

        getMockMvc()
        .perform(MockMvcRequestBuilders
        		.get("/applications/" + application1.getName())
        		.contentType("application/json")
        		.accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
        .andExpect(jsonPath("$.status", is("success")))
        .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
        .andExpect(jsonPath("$.result").isMap())
        .andExpect(jsonPath("$.result.name", is("smartff")))
        .andExpect(jsonPath("$.result.friendlyName", is("Smart Frig")))
        .andExpect(jsonPath("$.result.description", is("Description of smartff")));
    }

    @Test
    public void shouldReadDefaultApplicationByName() throws Exception {
        when(applicationService.getByApplicationName(tenant, defaultApplication.getName()))
        	.thenReturn(
        			ServiceResponseBuilder
        				.<Application>ok()
        				.withResult(defaultApplication)
        				.build());

        getMockMvc()
        .perform(MockMvcRequestBuilders
        		.get("/applications/" + defaultApplication.getName())
        		.contentType("application/json")
        		.accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
        .andExpect(jsonPath("$.status", is("success")))
        .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
        .andExpect(jsonPath("$.result").isMap())
        .andExpect(jsonPath("$.result.name", is(ApplicationServiceImpl.DEFAULT_APPLICATION_ALIAS)))
        .andExpect(jsonPath("$.result.friendlyName", is("Smart Frig")))
        .andExpect(jsonPath("$.result.description", is("Description of smartff")));
    }

    @Test
    public void shouldReturnNotFoundWhenReadByName() throws Exception {
        when(applicationService.getByApplicationName(tenant, application1.getName()))
        	.thenReturn(
        			ServiceResponseBuilder
        				.<Application>error()
                		.withMessage(ApplicationService.Validations.APPLICATION_NOT_FOUND.getCode())
                		.build());

        getMockMvc()
        .perform(MockMvcRequestBuilders
        		.get("/applications/" + application1.getName())
        		.accept(MediaType.APPLICATION_JSON)
        		.contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is4xxClientError())
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andExpect(jsonPath("$.code", is(HttpStatus.NOT_FOUND.value())))
        .andExpect(jsonPath("$.status", is("error")))
        .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
        .andExpect(jsonPath("$.messages[0]", is("Application not found")))
        .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    public void shouldCreateApplication() throws Exception {
        when(
        	applicationService
        		.register(org.mockito.Matchers.any(Tenant.class), org.mockito.Matchers.any(Application.class)))
        .thenReturn(ServiceResponseBuilder.<Application>ok().withResult(application1).build());

        getMockMvc()
        .perform(MockMvcRequestBuilders.post("/applications/")
        		.content(getJson(new ApplicationVO().apply(application1)))
        		.contentType("application/json")
        		.accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is2xxSuccessful())
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andExpect(jsonPath("$.code", is(HttpStatus.CREATED.value())))
        .andExpect(jsonPath("$.status", is("success")))
        .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
        .andExpect(jsonPath("$.result").isMap())
        .andExpect(jsonPath("$.result.name", is("smartff")))
        .andExpect(jsonPath("$.result.friendlyName", is("Smart Frig")))
        .andExpect(jsonPath("$.result.description", is("Description of smartff")));
    }

    @Test
    public void shouldTryCreateApplicationWithBadRequest() throws Exception {
        when(
        	applicationService
        		.register(org.mockito.Matchers.any(Tenant.class), org.mockito.Matchers.any(Application.class)))
        .thenReturn(ServiceResponseBuilder
        				.<Application>error()
        				.withMessage(ApplicationService.Validations.APPLICATION_NOT_FOUND.getCode())
        				.build());

        getMockMvc()
        .perform(MockMvcRequestBuilders.post("/applications/")
        		.content(getJson(new ApplicationVO().apply(application1)))
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
    public void shouldUpdateApplication() throws Exception {
        when(
        	applicationService
        		.getByApplicationName(tenant, application1.getName()))
        .thenReturn(ServiceResponseBuilder.<Application>ok().withResult(application1).build());

        when(
        	applicationService
        		.update(org.mockito.Matchers.any(Tenant.class), org.mockito.Matchers.anyString(), org.mockito.Matchers.any(Application.class)))
        .thenReturn(ServiceResponseBuilder.<Application>ok().withResult(application1).build());

        getMockMvc()
        .perform(MockMvcRequestBuilders.put("/applications/" + application1.getName())
        		.content(getJson(new ApplicationVO().apply(application1)))
        		.contentType(MediaType.APPLICATION_JSON)
        		.accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is2xxSuccessful())
        .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
        .andExpect(jsonPath("$.status", is("success")))
        .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
        .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldReturnInternalErrorWhenUpdateApplication() throws Exception {
    	when(
    		applicationService
    			.getByApplicationName(tenant, application1.getName()))
    	.thenReturn(ServiceResponseBuilder.<Application>ok().withResult(application1).build());

    	when(
    		applicationService
    			.update(org.mockito.Matchers.any(Tenant.class), org.mockito.Matchers.anyString(), org.mockito.Matchers.any(Application.class)))
    	.thenReturn(ServiceResponseBuilder.<Application>error().build());

    	getMockMvc()
    	.perform(MockMvcRequestBuilders.put("/applications/" + application1.getName())
    			.content(getJson(new ApplicationVO().apply(application1)))
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
    public void shouldDeleteApplication() throws Exception {
        when(applicationService.remove(tenant, application1.getName()))
                .thenReturn(ServiceResponseBuilder.<Application>ok().build());

        getMockMvc()
        .perform(MockMvcRequestBuilders.delete("/applications/" + application1.getName())
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
    public void shouldReturnInternalErrorWhenDeleteApplication() throws Exception {
    	when(applicationService.remove(tenant, application1.getName()))
    	.thenReturn(ServiceResponseBuilder
    					.<Application>error()
    					.withMessage(ApplicationService.Messages.APPLICATION_REMOVED_SUCCESSFULLY.getCode())
    					.build());

        getMockMvc()
        .perform(MockMvcRequestBuilders.delete("/applications/" + application1.getName())
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
    public void shouldTryDeleteNonexistentApplication() throws Exception {
    	when(applicationService.remove(tenant, application1.getName()))
    	.thenReturn(
    			ServiceResponseBuilder
    				.<Application>error()
    				.withMessage(ApplicationService.Validations.APPLICATION_NOT_FOUND.getCode())
    				.build());

    	getMockMvc().perform(MockMvcRequestBuilders.delete("/applications/" + application1.getName())
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

    @Test
    public void shouldShowHealthAlerts() throws Exception {
        when(healthAlertService.findAllByTenantAndApplication(tenant, application1))
				.thenReturn(ServiceResponseBuilder.<List<HealthAlert>>ok().withResult(healths).build());

        when(applicationService.getByApplicationName(tenant, application1.getName()))
				.thenReturn(ServiceResponseBuilder.<Application>ok().withResult(application1).build());

        getMockMvc().perform(MockMvcRequestBuilders.get(MessageFormat.format("/{0}/{1}/health/alerts", "applications", application1.getName()))
                    .contentType("application/json")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
                    .andExpect(jsonPath("$.status", is("success")))
                    .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                    .andExpect(jsonPath("$.result", hasSize(2)))
                    .andExpect(jsonPath("$.result[0].guid", is(health1.getGuid())))
                    .andExpect(jsonPath("$.result[0].severity", is(health1.getSeverity().toString())))
                    .andExpect(jsonPath("$.result[0].description", is("No message received from the device for a long time.")))
                    .andExpect(jsonPath("$.result[0].occurenceDate", is(health1.getLastChange().toString())))
                    .andExpect(jsonPath("$.result[0].type", is(health1.getType().toString())))
                    .andExpect(jsonPath("$.result[0].triggerGuid", is(health1.getTriggerGuid())))
                    .andExpect(jsonPath("$.result[1].guid", is(health2.getGuid())))
                    .andExpect(jsonPath("$.result[1].severity", is(health2.getSeverity().toString())))
                    .andExpect(jsonPath("$.result[1].description", is("No message received from the device for a long time.")))
                    .andExpect(jsonPath("$.result[1].occurenceDate", is(health2.getLastChange().toString())))
                    .andExpect(jsonPath("$.result[1].type", is(health2.getType().toString())))
                    .andExpect(jsonPath("$.result[1].triggerGuid", is(health2.getTriggerGuid())));
    }

    @Test
    public void shouldShowHealthAlertsWithDeviceHealthEmpty() throws Exception {
    	when(healthAlertService.findAllByTenantAndApplication(tenant, application1))
				.thenReturn(ServiceResponseBuilder.<List<HealthAlert>> error().withMessage(Validations.HEALTH_ALERT_DOES_NOT_EXIST.getCode()).build());

		when(applicationService.getByApplicationName(tenant, application1.getName()))
				.thenReturn(ServiceResponseBuilder.<Application>ok().withResult(application1).build());

		getMockMvc().perform(MockMvcRequestBuilders.get(MessageFormat.format("/{0}/{1}/health/alerts", "applications", application1.getName()))
                .contentType("application/json")
                .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is4xxClientError())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.code", is(HttpStatus.NOT_FOUND.value())))
                    .andExpect(jsonPath("$.status", is("error")))
                    .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                    .andExpect(jsonPath("$.messages[0]", is("Health alert does not exist")))
                    .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldDeleteHealthAlertFromApplication() throws Exception {
    	when(applicationService.getByApplicationName(tenant, application1.getName()))
    			.thenReturn(ServiceResponseBuilder.<Application>ok().withResult(application1).build());

        when(healthAlertService.remove(tenant, application1, health1.getGuid(), Solution.ALERT_DELETED))
                .thenReturn(ServiceResponseBuilder.<HealthAlert>ok().withResult(health1).build());

        getMockMvc()
        .perform(MockMvcRequestBuilders.delete("/applications/{0}/health/alerts/{1}", application1.getName(), health1.getGuid())
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
    public void shouldTryDeleteNonexistentHealthAlert() throws Exception {
    	when(applicationService.getByApplicationName(tenant, application1.getName()))
				.thenReturn(ServiceResponseBuilder.<Application>ok().withResult(application1).build());

		when(healthAlertService.remove(tenant, application1, health1.getGuid(), Solution.ALERT_DELETED))
		        .thenReturn(ServiceResponseBuilder.<HealthAlert>error()
		        		.withMessage(HealthAlertService.Validations.HEALTH_ALERT_NOT_FOUND.getCode()).build());

    	getMockMvc().perform(MockMvcRequestBuilders.delete("/applications/{0}/health/alerts/{1}", application1.getName(), health1.getGuid())
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
