package com.konkerlabs.platform.registry.alerts.test.web.controller;

import com.konkerlabs.platform.registry.alerts.model.SilenceTriggerVO;
import com.konkerlabs.platform.registry.alerts.web.controller.SilenceTriggerRestController;
import com.konkerlabs.platform.registry.alerts.web.wrapper.CrudResponseAdvice;
import com.konkerlabs.platform.registry.alerts.config.WebMvcConfig;
import com.konkerlabs.platform.registry.alerts.test.config.MongoTestConfig;
import com.konkerlabs.platform.registry.alerts.test.config.WebTestConfiguration;
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
import java.util.UUID;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = SilenceTriggerRestController.class)
@AutoConfigureMockMvc(secure = false)
@ContextConfiguration(classes = {
        WebTestConfiguration.class,
        MongoTestConfig.class,
        WebMvcConfig.class,
        CrudResponseAdvice.class
})
public class SilenceTriggerRestControllerTest extends WebLayerTestContext {

    private static final String NONEXIST_APPLICATION_NANE = "AppLost";

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private Tenant tenant;

    @Autowired
    private Application application;

    @Autowired
    private SilenceTriggerService silenceTriggerService;

    @Autowired
    private DeviceModelService deviceModelService;

    @Autowired
    private LocationSearchService locationSearchService;

    private DeviceModel deviceModel;

    private Location location;

    private SilenceTrigger silenceTrigger;

    private String BASEPATH = "triggers/silence";

    @Before
    public void setUp() {

        deviceModel = DeviceModel.builder()
                .tenant(tenant)
                .application(application)
        		.guid(UUID.randomUUID().toString())
        		.name("air conditioner")
        		.build();

        location = Location.builder()
                .tenant(tenant)
                .application(application)
                .guid(UUID.randomUUID().toString())
                .name("13th floor")
                .build();

        silenceTrigger = new SilenceTrigger();
        silenceTrigger.setGuid("2cdc391d-6a31-4103-9679-52cb6f2e5df5");
        silenceTrigger.setTenant(tenant);
        silenceTrigger.setApplication(application);
        silenceTrigger.setDeviceModel(deviceModel);
        silenceTrigger.setLocation(location);
        silenceTrigger.setMinutes(200);

        when(applicationService.getByApplicationName(tenant, application.getName()))
            .thenReturn(ServiceResponseBuilder.<Application> ok().withResult(application).build());

        when(deviceModelService.getByTenantApplicationAndName(tenant, application, deviceModel.getName()))
            .thenReturn(ServiceResponseBuilder.<DeviceModel> ok().withResult(deviceModel).build());

        when(locationSearchService.findByName(tenant, application, location.getName(), false))
            .thenReturn(ServiceResponseBuilder.<Location> ok().withResult(location).build());

        when(silenceTriggerService.findByTenantAndApplicationAndModelAndLocation(tenant, application, deviceModel, location))
            .thenReturn(ServiceResponseBuilder.<SilenceTrigger> ok().withResult(silenceTrigger).build());

    }

    @After
    public void tearDown() {
        Mockito.reset(applicationService);
    }

    @Test
    public void shouldReadSilenceTrigger() throws Exception {

        when(silenceTriggerService.findByTenantAndApplicationAndModelAndLocation(tenant, application, deviceModel, location))
            .thenReturn(ServiceResponseBuilder.<SilenceTrigger>ok()
                    .withResult(silenceTrigger)
                    .build());

        getMockMvc().perform(MockMvcRequestBuilders
        		.get(MessageFormat.format("/{0}/{1}/{2}/{3}", application.getName(), BASEPATH, deviceModel.getName(), location.getName()))
        		.contentType("application/json")
        		.accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                .andExpect(jsonPath("$.result").isMap())
                .andExpect(jsonPath("$.result.guid", is(silenceTrigger.getGuid())))
                .andExpect(jsonPath("$.result.deviceModelName", is(deviceModel.getName())))
                .andExpect(jsonPath("$.result.locationName", is(location.getName())))
                .andExpect(jsonPath("$.result.type", is("silence")))
                .andExpect(jsonPath("$.result.minutes", is(200)))
                ;

    }

    @Test
    public void shouldReadWithWrongApplication() throws Exception {

        when(applicationService.getByApplicationName(tenant, NONEXIST_APPLICATION_NANE))
                .thenReturn(ServiceResponseBuilder.<Application>error().withMessage(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode()).build());

        getMockMvc().perform(MockMvcRequestBuilders
                    .get(MessageFormat.format("/{0}/{1}/{2}/{3}", NONEXIST_APPLICATION_NANE, BASEPATH, deviceModel.getName(), location.getName()))
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
    public void shouldReadWithWrongLocation() throws Exception {

        when(locationSearchService.findByName(tenant, application, NONEXIST_APPLICATION_NANE, false))
            .thenReturn(ServiceResponseBuilder.<Location> error()
                    .withMessage(LocationService.Messages.LOCATION_NOT_FOUND.getCode(), new Object[] {"mg"})
                    .withResult(location)
                    .build());

        getMockMvc().perform(MockMvcRequestBuilders
                    .get(MessageFormat.format("/{0}/{1}/{2}/{3}", application.getName(), BASEPATH, deviceModel.getName(), NONEXIST_APPLICATION_NANE))
                    .contentType("application/json")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is4xxClientError())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.code", is(HttpStatus.NOT_FOUND.value())))
                    .andExpect(jsonPath("$.status", is("error")))
                    .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                    .andExpect(jsonPath("$.messages[0]", is("Location not found: mg")))
                    .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldReadWithWrongDeviceModel() throws Exception {

        when(deviceModelService.getByTenantApplicationAndName(tenant, application, NONEXIST_APPLICATION_NANE))
            .thenReturn(ServiceResponseBuilder.<DeviceModel> error()
                    .withMessage(DeviceModelService.Validations.DEVICE_MODEL_NOT_FOUND.getCode())
                    .build());

        getMockMvc().perform(MockMvcRequestBuilders
                    .get(MessageFormat.format("/{0}/{1}/{2}/{3}", application.getName(), BASEPATH, NONEXIST_APPLICATION_NANE, location.getName()))
                    .contentType("application/json")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is4xxClientError())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.code", is(HttpStatus.NOT_FOUND.value())))
                    .andExpect(jsonPath("$.status", is("error")))
                    .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                    .andExpect(jsonPath("$.messages[0]", is("Device model not found")))
                    .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldReturnNotFoundWhenReadByGuid() throws Exception {

        when(silenceTriggerService.findByTenantAndApplicationAndModelAndLocation(tenant, application, deviceModel, location))
            .thenReturn(ServiceResponseBuilder.<SilenceTrigger>error()
                .build());

        getMockMvc().perform(MockMvcRequestBuilders
        		.get(MessageFormat.format("/{0}/{1}/{2}/{3}", application.getName(), BASEPATH, deviceModel.getName(), location.getName()))
        		.accept(MediaType.APPLICATION_JSON)
        		.contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.NOT_FOUND.value())))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.messages").doesNotExist())
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    public void shouldCreateSilenceTrigger() throws Exception {

        when(silenceTriggerService.save(tenant, application, silenceTrigger))
            .thenReturn(ServiceResponseBuilder.<SilenceTrigger>ok()
                    .withResult(silenceTrigger)
                    .build());

        getMockMvc().perform(MockMvcRequestBuilders
                .post(MessageFormat.format("/{0}/{1}/{2}/{3}/", application.getName(), BASEPATH, deviceModel.getName(), location.getName()))
        		.content(getJson(new SilenceTriggerVO().apply(silenceTrigger)))
        		.contentType("application/json")
        		.accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.CREATED.value())))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                .andExpect(jsonPath("$.messages").doesNotExist())
                .andExpect(jsonPath("$.result").isMap())
                .andExpect(jsonPath("$.result.guid", is(silenceTrigger.getGuid())))
                .andExpect(jsonPath("$.result.deviceModelName", is(deviceModel.getName())))
                .andExpect(jsonPath("$.result.locationName", is(location.getName())))
                .andExpect(jsonPath("$.result.type", is("silence")))
                .andExpect(jsonPath("$.result.minutes", is(200)))
                ;

    }

    @Test
    public void shouldTryCreateSilenceTriggerWithBadRequest() throws Exception {

        when(silenceTriggerService.save(tenant, application, silenceTrigger))
            .thenReturn(ServiceResponseBuilder.<SilenceTrigger>error().withMessage(SilenceTrigger.Validations.INVALID_MINUTES_VALUE.getCode())
                    .build());

        getMockMvc().perform(MockMvcRequestBuilders
                .post(MessageFormat.format("/{0}/{1}/{2}/{3}/", application.getName(), BASEPATH, deviceModel.getName(), location.getName()))
                .content(getJson(new SilenceTriggerVO().apply(silenceTrigger)))
        		.contentType(MediaType.APPLICATION_JSON)
        		.accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.messages[0]", is("Invalid minutes. Must be equals or above than {0} minutes.")))
                .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldUpdateSilenceTrigger() throws Exception {

        when(silenceTriggerService.update(tenant, application, silenceTrigger.getGuid(), silenceTrigger))
            .thenReturn(ServiceResponseBuilder.<SilenceTrigger>ok()
                    .withResult(silenceTrigger)
                    .build());

        getMockMvc().perform(MockMvcRequestBuilders
                .put(MessageFormat.format("/{0}/{1}/{2}/{3}/", application.getName(), BASEPATH, deviceModel.getName(), location.getName()))
                .content(getJson(new SilenceTriggerVO().apply(silenceTrigger)))
        		.contentType(MediaType.APPLICATION_JSON)
        		.accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldReturnInternalErrorWhenUpdateSilenceTrigger() throws Exception {

        when(silenceTriggerService.update(tenant, application, silenceTrigger.getGuid(), silenceTrigger))
            .thenReturn(ServiceResponseBuilder.<SilenceTrigger>error().build());

    	getMockMvc().perform(MockMvcRequestBuilders
    	        .put(MessageFormat.format("/{0}/{1}/{2}/{3}/", application.getName(), BASEPATH, deviceModel.getName(), location.getName()))
    	        .content(getJson(new SilenceTriggerVO().apply(silenceTrigger)))
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
    public void shouldDeleteSilenceTrigger() throws Exception {

        when(silenceTriggerService.remove(tenant, application, silenceTrigger.getGuid()))
            .thenReturn(ServiceResponseBuilder.<SilenceTrigger>ok()
                    .build());

        getMockMvc().perform(MockMvcRequestBuilders
                .delete(MessageFormat.format("/{0}/{1}/{2}/{3}/", application.getName(), BASEPATH, deviceModel.getName(), location.getName()))
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
                    .delete(MessageFormat.format("/{0}/{1}/{2}/{3}/", NONEXIST_APPLICATION_NANE, BASEPATH, deviceModel.getName(), location.getName()))
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
    public void shouldReturnInternalErrorWhenDeleteSilenceTrigger() throws Exception {

        when(silenceTriggerService.remove(tenant, application, silenceTrigger.getGuid()))
            .thenReturn(ServiceResponseBuilder.<SilenceTrigger> error().build());

        getMockMvc().perform(MockMvcRequestBuilders
                .delete(MessageFormat.format("/{0}/{1}/{2}/{3}/", application.getName(), BASEPATH, deviceModel.getName(), location.getName()))
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
    public void shouldTryDeleteNonexistentSilenceTrigger() throws Exception {

        when(silenceTriggerService.findByTenantAndApplicationAndModelAndLocation(tenant, application, deviceModel, location))
            .thenReturn(ServiceResponseBuilder.<SilenceTrigger> error()
                    .withMessage(SilenceTriggerService.Validations.SILENCE_TRIGGER_NOT_FOUND.getCode())
                    .build());

    	getMockMvc().perform(MockMvcRequestBuilders
    	        .delete(MessageFormat.format("/{0}/{1}/{2}/{3}/", application.getName(), BASEPATH, deviceModel.getName(), location.getName()))
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
