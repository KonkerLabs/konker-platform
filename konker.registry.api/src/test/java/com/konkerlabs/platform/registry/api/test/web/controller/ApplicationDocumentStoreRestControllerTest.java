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
import com.konkerlabs.platform.registry.api.web.controller.ApplicationDocumentStoreRestController;
import com.konkerlabs.platform.registry.api.web.wrapper.CrudResponseAdvice;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.ApplicationDocumentStore;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.ApplicationDocumentStoreService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.mongodb.util.JSON;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ApplicationDocumentStoreRestController.class)
@AutoConfigureMockMvc(secure = false)
@ContextConfiguration(classes = {
        WebTestConfiguration.class,
        MongoTestConfig.class,
        WebMvcConfig.class,
        CrudResponseAdvice.class
})
public class ApplicationDocumentStoreRestControllerTest extends WebLayerTestContext {

    private static final String NONEXIST_APPLICATION_NAME = "AppLost";

    @Autowired
    private ApplicationDocumentStoreService applicationDocumentStoreService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private Tenant tenant;

    @Autowired
    private Application application;

    private ApplicationDocumentStore deviceCustomData1;

    private final String json1 = "{ 'a' : 'b' }";

    private final String BASEPATH = "documentStore";

    private final String CUSTOMDATAPATH = "customData";

    @Before
    public void setUp() {

        deviceCustomData1 = ApplicationDocumentStore.builder()
                                            .tenant(tenant)
                                            .application(application)
                                            .collection("collection1")
                                            .key("keyA")
                                            .json(json1)
                                            .build();

        when(applicationService.getByApplicationName(tenant, application.getName()))
                .thenReturn(ServiceResponseBuilder.<Application>ok().withResult(application).build());

        when(applicationDocumentStoreService.findUniqueByTenantApplication(tenant, application, "collection1", "keyA"))
                .thenReturn(ServiceResponseBuilder.<ApplicationDocumentStore>ok().withResult(deviceCustomData1).build());

    }

    @After
    public void tearDown() {
        Mockito.reset(applicationDocumentStoreService);
    }

    @Test
    public void shouldReadDevice() throws Exception {

        getMockMvc().perform(MockMvcRequestBuilders.get(MessageFormat.format("/{0}/{1}/{2}/{3}", application.getName(), BASEPATH, "collection1", "keyA", CUSTOMDATAPATH))
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

        getMockMvc().perform(MockMvcRequestBuilders.get(MessageFormat.format("/{0}/{1}/{2}/{3}", NONEXIST_APPLICATION_NAME, BASEPATH, "collection1", "keyA", CUSTOMDATAPATH))
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
    public void shouldTryReadDeviceWithBadRequest() throws Exception {

        when(applicationDocumentStoreService.findUniqueByTenantApplication(tenant, application, "collection1", "keyA"))
                .thenReturn(ServiceResponseBuilder.<ApplicationDocumentStore>error().withMessage(ApplicationDocumentStoreService.Validations.APP_DOCUMENT_DOES_NOT_EXIST.getCode()).withResult(deviceCustomData1).build());

        getMockMvc().perform(MockMvcRequestBuilders.get(MessageFormat.format("/{0}/{1}/{2}/{3}", application.getName(), BASEPATH, "collection1", "keyA", CUSTOMDATAPATH))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.NOT_FOUND.value())))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.messages[0]", is("Document does not exists")))
                .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldCreateDevice() throws Exception {

        when(applicationDocumentStoreService.save(tenant, application, "collection1", "keyA", json1))
                .thenReturn(ServiceResponseBuilder.<ApplicationDocumentStore>ok().withResult(deviceCustomData1).build());

        getMockMvc().perform(MockMvcRequestBuilders.post(MessageFormat.format("/{0}/{1}/{2}/{3}", application.getName(), BASEPATH, "collection1", "keyA", CUSTOMDATAPATH))
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

        when(applicationDocumentStoreService.save(tenant, application, "collection1", "keyA", json1))
                .thenReturn(ServiceResponseBuilder.<ApplicationDocumentStore>error().withMessage(DeviceRegisterService.Validations.DEVICE_GUID_DOES_NOT_EXIST.getCode()).build());

        getMockMvc().perform(MockMvcRequestBuilders.post(MessageFormat.format("/{0}/{1}/{2}/{3}", application.getName(), BASEPATH, "collection1", "keyA", CUSTOMDATAPATH))
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

        when(applicationDocumentStoreService.update(tenant, application, "collection1", "keyA", json1))
                .thenReturn(ServiceResponseBuilder.<ApplicationDocumentStore>ok().withResult(deviceCustomData1).build());

        getMockMvc().perform(MockMvcRequestBuilders.put(MessageFormat.format("/{0}/{1}/{2}/{3}", application.getName(), BASEPATH, "collection1", "keyA", CUSTOMDATAPATH))
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

        when(applicationDocumentStoreService.update(tenant, application, "collection1", "keyA", json1))
                .thenReturn(ServiceResponseBuilder.<ApplicationDocumentStore>error().build());

        getMockMvc().perform(MockMvcRequestBuilders.put(MessageFormat.format("/{0}/{1}/{2}/{3}", application.getName(), BASEPATH, "collection1", "keyA", CUSTOMDATAPATH))
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

        when(applicationDocumentStoreService.remove(tenant, application, "collection1", "keyA"))
                .thenReturn(ServiceResponseBuilder.<ApplicationDocumentStore>ok().build());

        getMockMvc().perform(MockMvcRequestBuilders.delete(MessageFormat.format("/{0}/{1}/{2}/{3}", application.getName(), BASEPATH, "collection1", "keyA", CUSTOMDATAPATH))
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

        getMockMvc().perform(MockMvcRequestBuilders.delete(MessageFormat.format("/{0}/{1}/{2}/{3}", NONEXIST_APPLICATION_NAME, BASEPATH, "collection1", "keyA", CUSTOMDATAPATH))
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

        when(applicationDocumentStoreService.remove(tenant, application, "collection1", "keyA"))
                .thenReturn(ServiceResponseBuilder.<ApplicationDocumentStore>error().build());

        getMockMvc().perform(MockMvcRequestBuilders.delete(MessageFormat.format("/{0}/{1}/{2}/{3}", application.getName(), BASEPATH, "collection1", "keyA", CUSTOMDATAPATH))
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
    public void shouldTryDeleteNonexistentEventRoute() throws Exception {

        when(applicationDocumentStoreService.remove(tenant, application, "collection1", "keyA"))
                .thenReturn(ServiceResponseBuilder.<ApplicationDocumentStore>error().withMessage(ApplicationDocumentStoreService.Validations.APP_DOCUMENT_DOES_NOT_EXIST.getCode()).build());

        getMockMvc().perform(MockMvcRequestBuilders.delete(MessageFormat.format("/{0}/{1}/{2}/{3}", application.getName(), BASEPATH, "collection1", "keyA", CUSTOMDATAPATH))
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
