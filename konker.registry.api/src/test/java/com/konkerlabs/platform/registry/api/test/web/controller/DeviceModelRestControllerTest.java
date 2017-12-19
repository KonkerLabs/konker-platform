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
import com.konkerlabs.platform.registry.api.model.DeviceModelVO;
import com.konkerlabs.platform.registry.api.test.config.MongoTestConfig;
import com.konkerlabs.platform.registry.api.test.config.WebTestConfiguration;
import com.konkerlabs.platform.registry.api.web.controller.DeviceModelRestController;
import com.konkerlabs.platform.registry.api.web.wrapper.CrudResponseAdvice;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.DeviceModel;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.DeviceModelService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = DeviceModelRestController.class)
@AutoConfigureMockMvc(secure = false)
@ContextConfiguration(classes = {
        WebTestConfiguration.class,
        MongoTestConfig.class,
        WebMvcConfig.class,
        CrudResponseAdvice.class
})
public class DeviceModelRestControllerTest extends WebLayerTestContext {

    private static final String NONEXIST_APPLICATION_NANE = "AppLost";

    @Autowired
    private DeviceModelService deviceModelService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private Tenant tenant;

    @Autowired
    private Application application;

    private DeviceModel deviceModel1;

    private DeviceModel deviceModel2;

    private String BASEPATH = "deviceModels";

    @Before
    public void setUp() {
        deviceModel1 = DeviceModel.builder()
        		.guid(UUID.randomUUID().toString())
        		.name("PresenceSensor")
        		.description("Presence sensor")
				.contentType(DeviceModel.ContentType.APPLICATION_JSON)
        		.defaultModel(true)
        		.build();

        deviceModel2 = DeviceModel.builder()
        		.guid(UUID.randomUUID().toString())
        		.name("SmartAC")
        		.description("Smart AC")
				.contentType(DeviceModel.ContentType.APPLICATION_MSGPACK)
        		.defaultModel(false)
        		.build();

        when(applicationService.getByApplicationName(tenant, application.getName()))
            .thenReturn(ServiceResponseBuilder.<Application> ok().withResult(application).build());

    }

    @After
    public void tearDown() {
        Mockito.reset(deviceModelService);
    }

    @Test
    public void shouldListDeviceModels() throws Exception {
        List<DeviceModel> deviceModels = new ArrayList<>();
        deviceModels.add(deviceModel1);
        deviceModels.add(deviceModel2);

        when(deviceModelService.findAll(tenant, application))
        	.thenReturn(
        			ServiceResponseBuilder
        				.<List<DeviceModel>>ok()
                		.withResult(deviceModels)
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
        .andExpect(jsonPath("$.result[0].guid", is(deviceModel1.getGuid())))
        .andExpect(jsonPath("$.result[0].name", is("PresenceSensor")))
        .andExpect(jsonPath("$.result[0].description", is("Presence sensor")))
		.andExpect(jsonPath("$.result[0].contentType", is("application/json")))
		.andExpect(jsonPath("$.result[0].defaultModel", is(true)))
        .andExpect(jsonPath("$.result[1].guid", is(deviceModel2.getGuid())))
        .andExpect(jsonPath("$.result[1].name", is("SmartAC")))
        .andExpect(jsonPath("$.result[1].description", is("Smart AC")))
        .andExpect(jsonPath("$.result[1].contentType", is("application/msgpack")))
        .andExpect(jsonPath("$.result[1].defaultModel", is(false)));
    }

    @Test
    public void shouldReturnInternalErrorWhenListDeviceModels() throws Exception {
        when(deviceModelService.findAll(tenant, application))
                .thenReturn(ServiceResponseBuilder.<List<DeviceModel>>error().build());

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
    public void shouldReadDeviceModelByName() throws Exception {
        when(deviceModelService.getByTenantApplicationAndName(tenant, application, deviceModel1.getName()))
        	.thenReturn(
        			ServiceResponseBuilder
        				.<DeviceModel>ok()
        				.withResult(deviceModel1)
        				.build());

        getMockMvc()
        .perform(MockMvcRequestBuilders
        		.get(MessageFormat.format("/{0}/{1}/{2}", application.getName(), BASEPATH, deviceModel1.getName()))
        		.contentType("application/json")
        		.accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
        .andExpect(jsonPath("$.status", is("success")))
        .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
        .andExpect(jsonPath("$.result").isMap())
        .andExpect(jsonPath("$.result.guid", is(deviceModel1.getGuid())))
        .andExpect(jsonPath("$.result.name", is("PresenceSensor")))
        .andExpect(jsonPath("$.result.description", is("Presence sensor")))
        .andExpect(jsonPath("$.result.defaultModel", is(true)));
    }

    @Test
    public void shouldReadWithWrongDeviceModel() throws Exception {

        when(applicationService.getByApplicationName(tenant, NONEXIST_APPLICATION_NANE))
                .thenReturn(ServiceResponseBuilder.<Application>error()
                				.withMessage(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode()).build());

        getMockMvc().perform(MockMvcRequestBuilders.get(MessageFormat.format("/{0}/{1}/{2}", NONEXIST_APPLICATION_NANE, BASEPATH, deviceModel1.getName()))
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
    public void shouldReturnNotFoundWhenReadByName() throws Exception {
        when(deviceModelService.getByTenantApplicationAndName(tenant, application, deviceModel1.getName()))
        	.thenReturn(
        			ServiceResponseBuilder
        				.<DeviceModel>error()
                		.withMessage(DeviceModelService.Validations.DEVICE_MODEL_NOT_FOUND.getCode())
                		.build());

        getMockMvc()
        .perform(MockMvcRequestBuilders
        		.get(MessageFormat.format("/{0}/{1}/{2}", application.getName(), BASEPATH, deviceModel1.getName()))
        		.accept(MediaType.APPLICATION_JSON)
        		.contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is4xxClientError())
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andExpect(jsonPath("$.code", is(HttpStatus.NOT_FOUND.value())))
        .andExpect(jsonPath("$.status", is("error")))
        .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
        .andExpect(jsonPath("$.messages[0]", is("Device model not found")))
        .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    public void shouldCreateDeviceModel() throws Exception {
        when(
        	deviceModelService
        		.register(org.mockito.Matchers.any(Tenant.class), 
        				  org.mockito.Matchers.any(Application.class),
        				  org.mockito.Matchers.any(DeviceModel.class)))
        .thenReturn(ServiceResponseBuilder.<DeviceModel>ok().withResult(deviceModel1).build());

        getMockMvc()
        .perform(MockMvcRequestBuilders.post(MessageFormat.format("/{0}/{1}/", application.getName(), BASEPATH))
        		.content(getJson(new DeviceModelVO().apply(deviceModel1)))
        		.contentType("application/json")
        		.accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is2xxSuccessful())
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andExpect(jsonPath("$.code", is(HttpStatus.CREATED.value())))
        .andExpect(jsonPath("$.status", is("success")))
        .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
        .andExpect(jsonPath("$.result").isMap())
        .andExpect(jsonPath("$.result.guid", is(deviceModel1.getGuid())))
        .andExpect(jsonPath("$.result.name", is("PresenceSensor")))
        .andExpect(jsonPath("$.result.description", is("Presence sensor")))
        .andExpect(jsonPath("$.result.contentType", is("application/json")))
        .andExpect(jsonPath("$.result.defaultModel", is(true)));
    }

    @Test
    public void shouldTryCreateDeviceModelWithBadRequest() throws Exception {
        when(
        	deviceModelService
        		.register(org.mockito.Matchers.any(Tenant.class), 
        				org.mockito.Matchers.any(Application.class), 
        				org.mockito.Matchers.any(DeviceModel.class)))
        .thenReturn(ServiceResponseBuilder
        				.<DeviceModel>error()
        				.withMessage(DeviceModelService.Validations.DEVICE_MODEL_NOT_FOUND.getCode())
        				.build());

        getMockMvc()
        .perform(MockMvcRequestBuilders.post(MessageFormat.format("/{0}/{1}/", application.getName(), BASEPATH))
        		.content(getJson(new DeviceModelVO().apply(deviceModel1)))
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
    public void shouldUpdateDeviceModel() throws Exception {
        when(
        	deviceModelService
        		.getByTenantApplicationAndName(tenant, application, deviceModel1.getName()))
        .thenReturn(ServiceResponseBuilder.<DeviceModel>ok().withResult(deviceModel1).build());

        when(
        	deviceModelService
        		.update(org.mockito.Matchers.any(Tenant.class), 
        				org.mockito.Matchers.any(Application.class), 
        				org.mockito.Matchers.anyString(), 
        				org.mockito.Matchers.any(DeviceModel.class)))
        .thenReturn(ServiceResponseBuilder.<DeviceModel>ok().withResult(deviceModel1).build());

        getMockMvc()
        .perform(MockMvcRequestBuilders.put(MessageFormat.format("/{0}/{1}/{2}", application.getName(), BASEPATH, deviceModel1.getName()))
        		.content(getJson(new DeviceModelVO().apply(deviceModel1)))
        		.contentType(MediaType.APPLICATION_JSON)
        		.accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is2xxSuccessful())
        .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
        .andExpect(jsonPath("$.status", is("success")))
        .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
        .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldReturnInternalErrorWhenUpdateDeviceModel() throws Exception {
    	when(
    		deviceModelService
    			.getByTenantApplicationAndName(tenant, application, deviceModel1.getName()))
    	.thenReturn(ServiceResponseBuilder.<DeviceModel>ok().withResult(deviceModel1).build());

    	when(
    		deviceModelService
    			.update(org.mockito.Matchers.any(Tenant.class), 
    					org.mockito.Matchers.any(Application.class), 
    					org.mockito.Matchers.anyString(), org.mockito.Matchers.any(DeviceModel.class)))
    	.thenReturn(ServiceResponseBuilder.<DeviceModel>error().build());

    	getMockMvc()
    	.perform(MockMvcRequestBuilders.put(MessageFormat.format("/{0}/{1}/{2}", application.getName(), BASEPATH, deviceModel1.getName()))
    			.content(getJson(new DeviceModelVO().apply(deviceModel1)))
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
    public void shouldDeleteDeviceModel() throws Exception {
        when(deviceModelService.remove(tenant, application, deviceModel1.getName()))
                .thenReturn(ServiceResponseBuilder.<DeviceModel>ok().build());

        getMockMvc()
        .perform(MockMvcRequestBuilders.delete(MessageFormat.format("/{0}/{1}/{2}", application.getName(), BASEPATH, deviceModel1.getName()))
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

        getMockMvc().perform(MockMvcRequestBuilders.delete(MessageFormat.format("/{0}/{1}/{2}", NONEXIST_APPLICATION_NANE, BASEPATH, deviceModel1.getName()))
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
    public void shouldReturnInternalErrorWhenDeleteDeviceModel() throws Exception {
    	when(deviceModelService.remove(tenant, application, deviceModel1.getName()))
    	.thenReturn(ServiceResponseBuilder
    					.<DeviceModel>error()
    					.withMessage(DeviceModelService.Messages.DEVICE_MODEL_REMOVED_UNSUCCESSFULLY.getCode())
    					.build());

        getMockMvc()
        .perform(MockMvcRequestBuilders.delete(MessageFormat.format("/{0}/{1}/{2}", application.getName(), BASEPATH, deviceModel1.getName()))
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
    public void shouldTryDeleteNonexistentDeviceModel() throws Exception {
    	when(deviceModelService.remove(tenant, application, deviceModel1.getName()))
    	.thenReturn(
    			ServiceResponseBuilder
    				.<DeviceModel>error()
    				.withMessage(DeviceModelService.Validations.DEVICE_MODEL_NOT_FOUND.getCode())
    				.build());

    	getMockMvc().perform(MockMvcRequestBuilders.delete(MessageFormat.format("/{0}/{1}/{2}", application.getName(), BASEPATH, deviceModel1.getName()))
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
    public void shouldListDeviceModelDevices() throws Exception {
        List<Device> devices = new ArrayList<>();
        devices.add(Device.builder().name("device1").build());
        devices.add(Device.builder().name("device2").build());

        when(deviceModelService.listDevicesByDeviceModelName(tenant, application, deviceModel1.getName()))
            .thenReturn(ServiceResponseBuilder.<List<Device>>ok().withResult(devices).build());

        getMockMvc().perform(MockMvcRequestBuilders.get(MessageFormat.format("/{0}/{1}/{2}/devices", application.getName(), BASEPATH, deviceModel1.getName()))
                    .contentType("application/json")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
                    .andExpect(jsonPath("$.status", is("success")))
                    .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                    .andExpect(jsonPath("$.result").isArray())
                    .andExpect(jsonPath("$.result", hasSize(2)))
                    .andExpect(jsonPath("$.result[0].name", is("device1")))
                    .andExpect(jsonPath("$.result[1].name", is("device2")));

    }
}
