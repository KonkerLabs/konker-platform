package com.konkerlabs.platform.registry.api.test.web.controller;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.text.MessageFormat;

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
import com.konkerlabs.platform.registry.api.web.controller.DeviceCustomDataRestController;
import com.konkerlabs.platform.registry.api.web.wrapper.CrudResponseAdvice;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.DeviceCustomData;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.DeviceCustomDataService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.mongodb.util.JSON;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = DeviceCustomDataRestController.class)
@AutoConfigureMockMvc(secure = false)
@ContextConfiguration(classes = {
        WebTestConfiguration.class,
        MongoTestConfig.class,
        WebMvcConfig.class,
        CrudResponseAdvice.class
})
public class DeviceCustomDataRestControllerTest extends WebLayerTestContext {

    private static final String NONEXIST_APPLICATION_NAME = "AppLost";

    @Autowired
    private DeviceRegisterService deviceRegisterService;

    @Autowired
    private DeviceCustomDataService deviceCustomDataService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private Tenant tenant;

    @Autowired
    private Application application;

    private Device device1;

    private DeviceCustomData deviceCustomData1;

    private final String json1 = "{ 'a' : 'b' }";

    private final String BASEPATH = "devices";

    private final String CUSTOMDATAPATH = "customData";

    private final String INVALID_GUID = "invalid_guid";

    @Before
    public void setUp() {

        device1 = Device.builder()
                        .id("LAjQnH2GMB")
                        .deviceId("id1")
                        .name("name1")
                        .guid("guid1")
                        .tenant(tenant)
                        .application(application)
                        .active(true)
                        .build();

        deviceCustomData1 = DeviceCustomData.builder()
                                            .tenant(tenant)
                                            .application(application)
                                            .device(device1)
                                            .json(json1)
                                            .build();

        when(deviceRegisterService.getByDeviceGuid(tenant, application, device1.getGuid()))
                .thenReturn(ServiceResponseBuilder.<Device>ok().withResult(device1).build());

        when(deviceRegisterService.getByDeviceGuid(tenant, application, INVALID_GUID))
                .thenReturn(ServiceResponseBuilder.<Device>error().withMessage(DeviceRegisterService.Validations.DEVICE_GUID_DOES_NOT_EXIST.getCode()).build());

        when(applicationService.getByApplicationName(tenant, application.getName()))
                .thenReturn(ServiceResponseBuilder.<Application>ok().withResult(application).build());

        when(deviceCustomDataService.getByTenantApplicationAndDevice(tenant, application, device1))
                .thenReturn(ServiceResponseBuilder.<DeviceCustomData>ok().withResult(deviceCustomData1).build());

    }

    @After
    public void tearDown() {
        Mockito.reset(deviceRegisterService);
    }

    @Test
    public void shouldReadDevice() throws Exception {

        getMockMvc().perform(MockMvcRequestBuilders.get(MessageFormat.format("/{0}/{1}/{2}/{3}", application.getName(), BASEPATH, device1.getGuid(), CUSTOMDATAPATH))
                    .contentType("application/json")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
                    .andExpect(jsonPath("$.status", is("success")))
                    .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                    .andExpect(jsonPath("$.result").isMap())
                    .andExpect(jsonPath("$.result", is(JSON.parse(json1))));

    }

    @Test
    public void shouldReadWithWrongApplication() throws Exception {

        when(applicationService.getByApplicationName(tenant, NONEXIST_APPLICATION_NAME))
                .thenReturn(ServiceResponseBuilder.<Application>error().withMessage(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode()).build());

        getMockMvc().perform(MockMvcRequestBuilders.get(MessageFormat.format("/{0}/{1}/{2}/{3}", NONEXIST_APPLICATION_NAME, BASEPATH, device1.getGuid(), CUSTOMDATAPATH))
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
    public void shouldTryReadDeviceWithInvalidGuid() throws Exception {

        getMockMvc().perform(MockMvcRequestBuilders.get(MessageFormat.format("/{0}/{1}/{2}/{3}", application.getName(), BASEPATH, INVALID_GUID, CUSTOMDATAPATH))
                    .contentType("application/json")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is4xxClientError())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.code", is(HttpStatus.NOT_FOUND.value())))
                    .andExpect(jsonPath("$.status", is("error")))
                    .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                    .andExpect(jsonPath("$.messages[0]", is("Device GUID does not exist")))
                    .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldTryReadDeviceWithBadRequest() throws Exception {

        when(deviceCustomDataService.getByTenantApplicationAndDevice(tenant, application, device1))
                .thenReturn(ServiceResponseBuilder.<DeviceCustomData>error().withMessage(DeviceCustomDataService.Validations.DEVICE_CUSTOM_DATA_DOES_NOT_EXIST.getCode()).withResult(deviceCustomData1).build());

        getMockMvc().perform(MockMvcRequestBuilders.get(MessageFormat.format("/{0}/{1}/{2}/{3}", application.getName(), BASEPATH, device1.getGuid(), CUSTOMDATAPATH))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.NOT_FOUND.value())))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.messages[0]", is("Custom data does not exists")))
                .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldCreateDevice() throws Exception {

        when(deviceCustomDataService.save(tenant, application, device1, json1))
                .thenReturn(ServiceResponseBuilder.<DeviceCustomData>ok().withResult(deviceCustomData1).build());

        getMockMvc().perform(MockMvcRequestBuilders.post(MessageFormat.format("/{0}/{1}/{2}/{3}", application.getName(), BASEPATH, device1.getGuid(), CUSTOMDATAPATH))
                                                   .content(json1)
                                                   .contentType("application/json")
                                                   .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is2xxSuccessful())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.code", is(HttpStatus.CREATED.value())))
                    .andExpect(jsonPath("$.status", is("success")))
                    .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                    .andExpect(jsonPath("$.result").isMap())
                    .andExpect(jsonPath("$.result", is(JSON.parse(json1))));
    }

    @Test
    public void shouldTryCreateDeviceWithBadRequest() throws Exception {

        when(deviceCustomDataService.save(tenant, application, device1, json1))
                .thenReturn(ServiceResponseBuilder.<DeviceCustomData>error().withMessage(DeviceRegisterService.Validations.DEVICE_GUID_DOES_NOT_EXIST.getCode()).build());

        getMockMvc().perform(MockMvcRequestBuilders.post(MessageFormat.format("/{0}/{1}/{2}/{3}", application.getName(), BASEPATH, device1.getGuid(), CUSTOMDATAPATH))
                .content(json1)
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
    public void shouldUpdateDevice() throws Exception {

        when(deviceCustomDataService.update(tenant, application, device1, json1))
                .thenReturn(ServiceResponseBuilder.<DeviceCustomData>ok().withResult(deviceCustomData1).build());

        getMockMvc().perform(MockMvcRequestBuilders.put(MessageFormat.format("/{0}/{1}/{2}/{3}", application.getName(), BASEPATH, device1.getGuid(), CUSTOMDATAPATH))
                .content(json1)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldTryUpdateDeviceWithInternalError() throws Exception {

        when(deviceCustomDataService.update(tenant, application, device1, json1))
                .thenReturn(ServiceResponseBuilder.<DeviceCustomData>error().build());

        getMockMvc().perform(MockMvcRequestBuilders.put(MessageFormat.format("/{0}/{1}/{2}/{3}", application.getName(), BASEPATH, device1.getGuid(), CUSTOMDATAPATH))
                .content(json1)
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
    public void shouldDeleteDevice() throws Exception {

        when(deviceCustomDataService.remove(tenant, application, device1))
                .thenReturn(ServiceResponseBuilder.<DeviceCustomData>ok().build());

        getMockMvc().perform(MockMvcRequestBuilders.delete(MessageFormat.format("/{0}/{1}/{2}/{3}", application.getName(), BASEPATH, device1.getGuid(), CUSTOMDATAPATH))
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
    public void shouldTryDeleteDeviceWithInvalidGuid() throws Exception {

        when(deviceCustomDataService.remove(tenant, application, device1))
                .thenReturn(ServiceResponseBuilder.<DeviceCustomData>ok().build());

        getMockMvc().perform(MockMvcRequestBuilders.delete(MessageFormat.format("/{0}/{1}/{2}/{3}", application.getName(), BASEPATH, INVALID_GUID, CUSTOMDATAPATH))
                                                   .contentType("application/json")
                                                   .accept(MediaType.APPLICATION_JSON))
                                                   .andExpect(status().is4xxClientError())
                                                   .andExpect(content().contentType("application/json;charset=UTF-8"))
                                                   .andExpect(jsonPath("$.code", is(HttpStatus.NOT_FOUND.value())))
                                                   .andExpect(jsonPath("$.status", is("error")))
                                                   .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                                                   .andExpect(jsonPath("$.messages[0]", is("Device GUID does not exist")))
                                                   .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldTryDeleteWithWrongApplication() throws Exception {

        when(applicationService.getByApplicationName(tenant, NONEXIST_APPLICATION_NAME))
                .thenReturn(ServiceResponseBuilder.<Application>error().withMessage(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode()).build());

        getMockMvc().perform(MockMvcRequestBuilders.delete(MessageFormat.format("/{0}/{1}/{2}/{3}", NONEXIST_APPLICATION_NAME, BASEPATH, device1.getGuid(), CUSTOMDATAPATH))
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
    public void shouldTryDeleteDeviceWithInternalError() throws Exception {

        when(deviceCustomDataService.remove(tenant, application, device1))
                .thenReturn(ServiceResponseBuilder.<DeviceCustomData>error().withMessage(DeviceCustomDataService.Messages.DEVICE_CUSTOM_DATA_REMOVED_SUCCESSFULLY.getCode()).build());

        getMockMvc().perform(MockMvcRequestBuilders.delete(MessageFormat.format("/{0}/{1}/{2}/{3}", application.getName(), BASEPATH, device1.getGuid(), CUSTOMDATAPATH))
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
    public void shouldTryDeleteNonexistentEventRoute() throws Exception {

        when(deviceCustomDataService.remove(tenant, application, device1))
                .thenReturn(ServiceResponseBuilder.<DeviceCustomData>error().withMessage(DeviceCustomDataService.Validations.DEVICE_CUSTOM_DATA_DOES_NOT_EXIST.getCode()).build());

        getMockMvc().perform(MockMvcRequestBuilders.delete(MessageFormat.format("/{0}/{1}/{2}/{3}", application.getName(), BASEPATH, device1.getGuid(), CUSTOMDATAPATH))
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
