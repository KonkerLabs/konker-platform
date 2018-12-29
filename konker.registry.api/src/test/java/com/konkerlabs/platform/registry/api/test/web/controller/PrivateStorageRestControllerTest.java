package com.konkerlabs.platform.registry.api.test.web.controller;

import com.konkerlabs.platform.registry.api.config.WebMvcConfig;
import com.konkerlabs.platform.registry.api.test.config.MongoTestConfig;
import com.konkerlabs.platform.registry.api.test.config.WebTestConfiguration;
import com.konkerlabs.platform.registry.api.web.controller.PrivateStorageRestController;
import com.konkerlabs.platform.registry.api.web.wrapper.CrudResponseAdvice;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.PrivateStorageService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.storage.model.PrivateStorage;
import com.mongodb.util.JSON;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = PrivateStorageRestController.class)
@AutoConfigureMockMvc(secure = false)
@ContextConfiguration(classes = {
        WebTestConfiguration.class,
        MongoTestConfig.class,
        WebMvcConfig.class,
        CrudResponseAdvice.class
})
public class PrivateStorageRestControllerTest extends WebLayerTestContext {

    private static final String NONEXIST_APPLICATION_NAME = "AppLost";

    @Autowired
    private PrivateStorageService privateStorageService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private Tenant tenant;

    @Autowired
    private Application application;

    private PrivateStorage privateStorage1;
    private PrivateStorage privateStorage2;
    private List<PrivateStorage> allData;

    private final String json1 = "{\"_id\":\"adbc-123\",\"desc\":\"just a test\"}";
    private final String json2 = "{\"_id\":\"adbc-456\",\"desc\":\"just a test 456\"}";

    private final String BASEPATH = "privateStorage";

    @Before
    public void setUp() {
       privateStorage1 = PrivateStorage.builder()
               .collectionName("customers")
               .collectionContent(json1)
               .build();

       privateStorage2 = PrivateStorage.builder()
                .collectionName("customers")
                .collectionContent(json2)
                .build();

       allData = new ArrayList<>();
       allData.add(privateStorage1);
        allData.add(privateStorage2);

        when(applicationService.getByApplicationName(tenant, application.getName()))
                .thenReturn(ServiceResponseBuilder.<Application>ok().withResult(application).build());

    }

    @After
    public void tearDown() {
        Mockito.reset(privateStorageService);
    }

    @Test
    public void shouldListCollections() throws Exception {
        List<String> collections = new ArrayList<>();
        collections.add("customers");

        when(privateStorageService.listCollections(any(Tenant.class), any(Application.class), any(User.class)))
                .thenReturn(ServiceResponseBuilder.<List<String>>ok().withResult(collections).build());

        getMockMvc().perform(MockMvcRequestBuilders.get(MessageFormat.format("/{0}/{1}/{2}", application.getName(), BASEPATH, "/collections"))
                .contentType("application/json")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result[0]", is("customers")));

    }

    @Test
    public void shouldListCollectionsWithWrongApplication() throws Exception {
        when(applicationService.getByApplicationName(tenant, NONEXIST_APPLICATION_NAME))
                .thenReturn(ServiceResponseBuilder.<Application>error().withMessage(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode()).build());

        getMockMvc().perform(MockMvcRequestBuilders.get(MessageFormat.format("/{0}/{1}/{2}", NONEXIST_APPLICATION_NAME, BASEPATH, "/collections"))
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
    public void shouldTryListCollectionsWithBadRequest() throws Exception {
        when(privateStorageService.listCollections(any(Tenant.class), any(Application.class), any(User.class)))
                .thenReturn(ServiceResponseBuilder.<List<String>>error()
                        .withMessage(PrivateStorageService.Validations.PRIVATE_STORAGE_COLLECTION_CONTENT_DOES_NOT_EXIST.getCode()).build());

        getMockMvc().perform(MockMvcRequestBuilders.get(MessageFormat.format("/{0}/{1}/{2}", application.getName(), BASEPATH, "/collections"))
                .contentType("application/json")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.NOT_FOUND.value())))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.messages[0]", is("Data does not exists")))
                .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldListData() throws Exception {
        when(privateStorageService.findAll(any(Tenant.class), any(Application.class), any(User.class), anyString()))
                .thenReturn(ServiceResponseBuilder.<List<PrivateStorage>>ok()
                        .withResult(allData).build());

        getMockMvc().perform(MockMvcRequestBuilders.get(MessageFormat.format("/{0}/{1}/{2}", application.getName(), BASEPATH, "customers"))
                    .contentType("application/json")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
                    .andExpect(jsonPath("$.status", is("success")))
                    .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                    .andExpect(jsonPath("$.result").isArray())
                    .andExpect(jsonPath("$.result[0]", is(JSON.parse(json1))))
                    .andExpect(jsonPath("$.result[1]", is(JSON.parse(json2))));

    }

    @Test
    public void shouldListDataWithWrongApplication() throws Exception {

        when(applicationService.getByApplicationName(tenant, NONEXIST_APPLICATION_NAME))
                .thenReturn(ServiceResponseBuilder.<Application>error().withMessage(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode()).build());

        getMockMvc().perform(MockMvcRequestBuilders.get(MessageFormat.format("/{0}/{1}/{2}", NONEXIST_APPLICATION_NAME, BASEPATH, "customers"))
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
    public void shouldTryListDataWithBadRequest() throws Exception {
        when(privateStorageService.findAll(any(Tenant.class), any(Application.class), any(User.class), anyString()))
                .thenReturn(ServiceResponseBuilder.<List<PrivateStorage>>error()
                        .withMessage(PrivateStorageService.Validations.PRIVATE_STORAGE_INVALID_COLLECTION_NAME.getCode()).build());

        getMockMvc().perform(MockMvcRequestBuilders.get(MessageFormat.format("/{0}/{1}/{2}", application.getName(), BASEPATH, "/customers"))
                .contentType("application/json")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.NOT_FOUND.value())))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.messages[0]", is("Collection name invalid")))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    public void shouldReadData() throws Exception {
        when(privateStorageService.findById(any(Tenant.class), any(Application.class), any(User.class), anyString(), anyString()))
                .thenReturn(ServiceResponseBuilder.<PrivateStorage>ok()
                        .withResult(privateStorage1).build());

        getMockMvc().perform(MockMvcRequestBuilders.get(MessageFormat.format("/{0}/{1}/{2}/{3}", application.getName(), BASEPATH, "customers", "adbc-123"))
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
    public void shouldReadDataWithWrongApplication() throws Exception {
        when(applicationService.getByApplicationName(tenant, NONEXIST_APPLICATION_NAME))
                .thenReturn(ServiceResponseBuilder.<Application>error().withMessage(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode()).build());

        getMockMvc().perform(MockMvcRequestBuilders.get(MessageFormat.format("/{0}/{1}/{2}/{3}", NONEXIST_APPLICATION_NAME, BASEPATH, "customers", "adbc-123"))
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
    public void shouldTryReadDataWithBadRequest() throws Exception {
        when(privateStorageService.findById(any(Tenant.class), any(Application.class), any(User.class), anyString(), anyString()))
                .thenReturn(ServiceResponseBuilder.<PrivateStorage>error()
                        .withMessage(PrivateStorageService.Validations.PRIVATE_STORAGE_INVALID_COLLECTION_NAME.getCode()).build());

        getMockMvc().perform(MockMvcRequestBuilders.get(MessageFormat.format("/{0}/{1}/{2}/{3}", application.getName(), BASEPATH, "customers", "adbc-123"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.NOT_FOUND.value())))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.messages[0]", is("Collection name invalid")))
                .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldCreateData() throws Exception {
        when(privateStorageService.save(any(Tenant.class), any(Application.class), any(User.class), anyString(), anyString()))
                .thenReturn(ServiceResponseBuilder.<PrivateStorage>ok().withResult(privateStorage1).build());

        getMockMvc().perform(MockMvcRequestBuilders.post(MessageFormat.format("/{0}/{1}/{2}", application.getName(), BASEPATH, "customers"))
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
    public void shouldTryCreateDataWithBadRequest() throws Exception {
        when(privateStorageService.save(any(Tenant.class), any(Application.class), any(User.class), anyString(), anyString()))
                .thenReturn(ServiceResponseBuilder.<PrivateStorage>error().withMessage(PrivateStorageService.Validations.PRIVATE_STORAGE_IS_FULL.getCode()).build());

        getMockMvc().perform(MockMvcRequestBuilders.post(MessageFormat.format("/{0}/{1}/{2}", application.getName(), BASEPATH, "customers"))
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
    public void shouldUpdateData() throws Exception {
        when(privateStorageService.update(any(Tenant.class), any(Application.class), any(User.class), anyString(), anyString()))
                .thenReturn(ServiceResponseBuilder.<PrivateStorage>ok().withResult(privateStorage1).build());

        getMockMvc().perform(MockMvcRequestBuilders.put(MessageFormat.format("/{0}/{1}/{2}", application.getName(), BASEPATH, "customers"))
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
    public void shouldTryUpdateDataWithInternalError() throws Exception {
        when(privateStorageService.update(any(Tenant.class), any(Application.class), any(User.class), anyString(), anyString()))
                .thenReturn(ServiceResponseBuilder.<PrivateStorage>error().build());

        getMockMvc().perform(MockMvcRequestBuilders.put(MessageFormat.format("/{0}/{1}/{2}", application.getName(), BASEPATH, "customers"))
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
    public void shouldDeleteData() throws Exception {
        when(privateStorageService.remove(any(Tenant.class), any(Application.class), any(User.class), anyString(), anyString()))
                .thenReturn(ServiceResponseBuilder.<PrivateStorage>ok().build());

        getMockMvc().perform(MockMvcRequestBuilders.delete(MessageFormat.format("/{0}/{1}/{2}/{3}", application.getName(), BASEPATH, "customers", "adbc-123"))
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
    public void shouldTryDeleteDataWithWrongApplication() throws Exception {
        when(applicationService.getByApplicationName(tenant, NONEXIST_APPLICATION_NAME))
                .thenReturn(ServiceResponseBuilder.<Application>error().withMessage(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode()).build());

        getMockMvc().perform(MockMvcRequestBuilders.delete(MessageFormat.format("/{0}/{1}/{2}/{3}", NONEXIST_APPLICATION_NAME, BASEPATH, "customers", "adbc-123"))
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
    public void shouldTryDeleteDataWithInternalError() throws Exception {

        when(privateStorageService.remove(any(Tenant.class), any(Application.class), any(User.class), anyString(), anyString()))
                .thenReturn(ServiceResponseBuilder.<PrivateStorage>error().build());

        getMockMvc().perform(MockMvcRequestBuilders.delete(MessageFormat.format("/{0}/{1}/{2}/{3}", application.getName(), BASEPATH, "customers", "adbc-123"))
                                                   .contentType("application/json")
                                                   .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is5xxServerError())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.code", is(HttpStatus.INTERNAL_SERVER_ERROR.value())))
                    .andExpect(jsonPath("$.status", is("error")))
                    .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                    .andExpect(jsonPath("$.result").doesNotExist());

    }

}
