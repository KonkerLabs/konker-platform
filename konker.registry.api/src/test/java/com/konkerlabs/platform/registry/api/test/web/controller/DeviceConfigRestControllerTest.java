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
import com.konkerlabs.platform.registry.api.test.config.MongoTestConfig;
import com.konkerlabs.platform.registry.api.test.config.WebTestConfiguration;
import com.konkerlabs.platform.registry.api.web.controller.DeviceConfigRestController;
import com.konkerlabs.platform.registry.api.web.wrapper.CrudResponseAdvice;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.DeviceConfig;
import com.konkerlabs.platform.registry.business.model.DeviceConfigSetup;
import com.konkerlabs.platform.registry.business.model.DeviceModel;
import com.konkerlabs.platform.registry.business.model.Location;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.DeviceConfigSetupService;
import com.konkerlabs.platform.registry.business.services.api.DeviceModelService;
import com.konkerlabs.platform.registry.business.services.api.LocationSearchService;
import com.konkerlabs.platform.registry.business.services.api.LocationService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = DeviceConfigRestController.class)
@AutoConfigureMockMvc(secure = false)
@ContextConfiguration(classes = {
        WebTestConfiguration.class,
        MongoTestConfig.class,
        WebMvcConfig.class,
        CrudResponseAdvice.class
})
public class DeviceConfigRestControllerTest extends WebLayerTestContext {

    private static final String NONEXIST_APPLICATION_NAME = "AppLost";

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private Tenant tenant;

    @Autowired
    private Application application;

    @Autowired
    private DeviceConfigSetupService deviceConfigSetupService;

    @Autowired
    private DeviceModelService deviceModelService;

    @Autowired
    private LocationSearchService locationSearchService;

    private DeviceModel deviceModel;

    private Location location;

    private DeviceConfig deviceConfig;

    private String json;

    private final String BASEPATH = "configs";

    @Before
    public void setUp() {

        json = "{ 'tmz' : 'GMT' }";

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

        deviceConfig = DeviceConfig.builder()
                .deviceModel(deviceModel.getName())
                .deviceModelGuid(deviceModel.getGuid())
                .locationName(location.getName())
                .locationGuid(location.getGuid())
                .json(json)
                .build();

        when(applicationService.getByApplicationName(tenant, application.getName()))
            .thenReturn(ServiceResponseBuilder.<Application> ok().withResult(application).build());

        when(deviceModelService.getByTenantApplicationAndName(tenant, application, deviceModel.getName()))
            .thenReturn(ServiceResponseBuilder.<DeviceModel> ok().withResult(deviceModel).build());

        when(locationSearchService.findByName(tenant, application, location.getName(), true))
            .thenReturn(ServiceResponseBuilder.<Location> ok().withResult(location).build());

    }

    @After
    public void tearDown() {
        Mockito.reset(applicationService);
    }

    @Test
    public void shouldListDeviceConfigs() throws Exception {

        List<DeviceConfig> deviceConfigs = new ArrayList<>();
        deviceConfigs.add(deviceConfig);
        deviceConfigs.add(deviceConfig);

        when(deviceConfigSetupService.findAll(tenant, application))
            .thenReturn(ServiceResponseBuilder.<List<DeviceConfig>>ok()
                    .withResult(deviceConfigs)
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
                .andExpect(jsonPath("$.result[0].deviceModelGuid", is(deviceModel.getGuid())))
                .andExpect(jsonPath("$.result[0].deviceModel", is(deviceModel.getName())))
                .andExpect(jsonPath("$.result[0].locationGuid", is(location.getGuid())))
                .andExpect(jsonPath("$.result[0].locationName", is(location.getName())))
                .andExpect(jsonPath("$.result[0].json.tmz", is("GMT")))
                .andExpect(jsonPath("$.result[1].deviceModelGuid", is(deviceModel.getGuid())))
                .andExpect(jsonPath("$.result[1].deviceModel", is(deviceModel.getName())))
                .andExpect(jsonPath("$.result[1].locationGuid", is(location.getGuid())))
                .andExpect(jsonPath("$.result[1].locationName", is(location.getName())))
                .andExpect(jsonPath("$.result[1].json.tmz", is("GMT")))
                ;
    }

    @Test
    public void shouldReturnInternalErrorWhenListDeviceConfigs() throws Exception {

        when(deviceConfigSetupService.findAll(tenant, application))
            .thenReturn(ServiceResponseBuilder.<List<DeviceConfig>>error()
                    .build());

        getMockMvc().perform(MockMvcRequestBuilders
                .get(MessageFormat.format("/{0}/{1}/", application.getName(), BASEPATH))
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
    public void shouldReadDeviceConfig() throws Exception {

        when(deviceConfigSetupService.findByModelAndLocation(tenant, application, deviceModel, location))
            .thenReturn(ServiceResponseBuilder.<String>ok()
                    .withResult(json)
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
                .andExpect(jsonPath("$.result.tmz", is("GMT")))
                ;

    }

    @Test
    public void shouldReadWithWrongApplication() throws Exception {

        when(applicationService.getByApplicationName(tenant, NONEXIST_APPLICATION_NAME))
                .thenReturn(ServiceResponseBuilder.<Application>error().withMessage(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode()).build());

        getMockMvc().perform(MockMvcRequestBuilders
                    .get(MessageFormat.format("/{0}/{1}/{2}/{3}", NONEXIST_APPLICATION_NAME, BASEPATH, deviceModel.getName(), location.getName()))
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

        when(locationSearchService.findByName(tenant, application, NONEXIST_APPLICATION_NAME, true))
            .thenReturn(ServiceResponseBuilder.<Location> error()
                    .withMessage(LocationService.Messages.LOCATION_NOT_FOUND.getCode(), new Object[] {"mg"})
                    .withResult(location)
                    .build());

        getMockMvc().perform(MockMvcRequestBuilders
                    .get(MessageFormat.format("/{0}/{1}/{2}/{3}", application.getName(), BASEPATH, deviceModel.getName(), NONEXIST_APPLICATION_NAME))
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

        when(deviceModelService.getByTenantApplicationAndName(tenant, application, NONEXIST_APPLICATION_NAME))
            .thenReturn(ServiceResponseBuilder.<DeviceModel> error()
                    .withMessage(DeviceModelService.Validations.DEVICE_MODEL_NOT_FOUND.getCode())
                    .build());

        getMockMvc().perform(MockMvcRequestBuilders
                    .get(MessageFormat.format("/{0}/{1}/{2}/{3}", application.getName(), BASEPATH, NONEXIST_APPLICATION_NAME, location.getName()))
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

        when(deviceConfigSetupService.findByModelAndLocation(tenant, application, deviceModel, location))
            .thenReturn(ServiceResponseBuilder.<String>error()
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
    public void shouldCreateDeviceConfig() throws Exception {

        when(deviceConfigSetupService.save(tenant, application, deviceModel, location, json))
            .thenReturn(ServiceResponseBuilder.<DeviceConfig>ok()
                    .withResult(deviceConfig)
                    .withMessage(DeviceConfigSetupService.Messages.DEVICE_CONFIG_REGISTERED_SUCCESSFULLY.getCode()).build());

        getMockMvc().perform(MockMvcRequestBuilders
                .post(MessageFormat.format("/{0}/{1}/{2}/{3}/", application.getName(), BASEPATH, deviceModel.getName(), location.getName()))
        		.content(json)
        		.contentType("application/json")
        		.accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.CREATED.value())))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                .andExpect(jsonPath("$.messages").doesNotExist())
                .andExpect(jsonPath("$.result").isMap())
                .andExpect(jsonPath("$.result.deviceModelGuid", is(deviceModel.getGuid())))
                .andExpect(jsonPath("$.result.deviceModel", is(deviceModel.getName())))
                .andExpect(jsonPath("$.result.locationGuid", is(location.getGuid())))
                .andExpect(jsonPath("$.result.locationName", is(location.getName())))
                .andExpect(jsonPath("$.result.json.tmz", is("GMT")));

    }

    @Test
    public void shouldTryCreateDeviceConfigWithBadRequest() throws Exception {

        when(deviceConfigSetupService.save(tenant, application, deviceModel, location, json))
            .thenReturn(ServiceResponseBuilder.<DeviceConfig>error().withMessage(DeviceConfigSetupService.Validations.DEVICE_INVALID_JSON.getCode()).build());

        getMockMvc().perform(MockMvcRequestBuilders
                .post(MessageFormat.format("/{0}/{1}/{2}/{3}/", application.getName(), BASEPATH, deviceModel.getName(), location.getName()))
        		.content(json)
        		.contentType(MediaType.APPLICATION_JSON)
        		.accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.messages[0]", is("Invalid JSON")))
                .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldUpdateDeviceConfig() throws Exception {

        when(deviceConfigSetupService.update(tenant, application, deviceModel, location, json))
            .thenReturn(ServiceResponseBuilder.<DeviceConfig>ok()
                    .withResult(deviceConfig)
                    .withMessage(DeviceConfigSetupService.Messages.DEVICE_CONFIG_REGISTERED_SUCCESSFULLY.getCode()).build());

        getMockMvc().perform(MockMvcRequestBuilders
                .put(MessageFormat.format("/{0}/{1}/{2}/{3}/", application.getName(), BASEPATH, deviceModel.getName(), location.getName()))
        		.content(json)
        		.contentType(MediaType.APPLICATION_JSON)
        		.accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldReturnInternalErrorWhenUpdateDeviceConfig() throws Exception {

        when(deviceConfigSetupService.update(tenant, application, deviceModel, location, json))
            .thenReturn(ServiceResponseBuilder.<DeviceConfig>error().build());

    	getMockMvc().perform(MockMvcRequestBuilders
    	        .put(MessageFormat.format("/{0}/{1}/{2}/{3}/", application.getName(), BASEPATH, deviceModel.getName(), location.getName()))
    			.content(json)
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
    public void shouldDeleteDeviceConfig() throws Exception {

        when(deviceConfigSetupService.remove(tenant, application, deviceModel, location))
            .thenReturn(ServiceResponseBuilder.<DeviceConfigSetup>ok().withMessage(DeviceConfigSetupService.Messages.DEVICE_CONFIG_REMOVED_SUCCESSFULLY.getCode()).build());

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

        when(applicationService.getByApplicationName(tenant, NONEXIST_APPLICATION_NAME))
                .thenReturn(ServiceResponseBuilder.<Application>error().withMessage(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode()).build());

        getMockMvc().perform(MockMvcRequestBuilders
                    .delete(MessageFormat.format("/{0}/{1}/{2}/{3}/", NONEXIST_APPLICATION_NAME, BASEPATH, deviceModel.getName(), location.getName()))
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
    public void shouldReturnInternalErrorWhenDeleteDeviceConfig() throws Exception {

        when(deviceConfigSetupService.remove(tenant, application, deviceModel, location))
            .thenReturn(ServiceResponseBuilder.<DeviceConfigSetup> error().build());

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
    public void shouldTryDeleteNonexistentDeviceConfig() throws Exception {

        when(deviceConfigSetupService.remove(tenant, application, deviceModel, location))
            .thenReturn(ServiceResponseBuilder.<DeviceConfigSetup> error()
                    .withMessage(DeviceConfigSetupService.Validations.DEVICE_CONFIG_NOT_FOUND.getCode())
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
