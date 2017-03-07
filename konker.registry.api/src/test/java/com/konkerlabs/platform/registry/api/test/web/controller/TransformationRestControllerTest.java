package com.konkerlabs.platform.registry.api.test.web.controller;


import com.konkerlabs.platform.registry.api.config.WebMvcConfig;
import com.konkerlabs.platform.registry.api.model.TransformationVO;
import com.konkerlabs.platform.registry.api.test.config.MongoTestConfig;
import com.konkerlabs.platform.registry.api.test.config.WebTestConfiguration;
import com.konkerlabs.platform.registry.api.web.controller.TransformationsRestController;
import com.konkerlabs.platform.registry.api.web.wrapper.CrudResponseAdvice;
import com.konkerlabs.platform.registry.business.model.RestTransformationStep;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.Transformation;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.business.services.api.TransformationService;
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
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TransformationsRestController.class)
@AutoConfigureMockMvc(secure = false)
@ContextConfiguration(classes = {
        WebTestConfiguration.class,
        MongoTestConfig.class,
        WebMvcConfig.class,
        CrudResponseAdvice.class
})
public class TransformationRestControllerTest extends WebLayerTestContext {

    @Autowired
    private TransformationService transformationService;

    @Autowired
    private Tenant tenant;

    private String method = "POST";
    private String url1 = "http://google.com/test";
    private String url2 = "http://google.com/test2";
    private String username = "root";
    private String password = "secret";
    private String BASEPATH = "/restTransformations/";

    private Transformation validTransformation1;
    private Transformation validTransformation2;
    private String ID1 = "1";
    private String ID2 = "2";
    private String GUID1 = "cd3bf9e5-53d9-459c-9a58-95a5ecc5c695";
    private String GUID2 = "5735af3a-7a69-4192-a261-13f278f30c8d";
    private String NAME1 = "test1";
    private String NAME2 = "test2";
    private String DESCRIPTION1 = "test1Description";
    private String DESCRIPTION2 = "test2Description";


    @Before
    public void setUp() {
        validTransformation1 =
                Transformation.builder()
                        .id(ID1)
                        .guid(GUID1)
                        .name(NAME1)
                        .description(DESCRIPTION1)
                        .step(
                                RestTransformationStep.builder()
                                        .attributes(
                                                new HashMap<String, Object>() {{
                                                    put("method", method);
                                                    put("url", url1);
                                                    put("username", username);
                                                    put("password", password);
                                                }})
                                        .build())
                        .build();

        validTransformation2 =
                Transformation.builder()
                        .id(ID2)
                        .guid(GUID2)
                        .name(NAME2)
                        .description(DESCRIPTION2)
                        .step(
                                RestTransformationStep.builder()
                                        .attributes(
                                                new HashMap<String, Object>() {{
                                                    put("method", method);
                                                    put("url", url2);
                                                    put("username", username);
                                                    put("password", password);
                                                }})
                                        .build())
                        .build();

    }

    @After
    public void tearDown() {
        Mockito.reset(transformationService);
    }

    @Test
    public void shouldListTransformations() throws Exception {

        when(transformationService.getAll(tenant))
                .thenReturn(ServiceResponseBuilder.<List<Transformation>>ok()
                        .withResult(Arrays.asList(
                                validTransformation1,
                                validTransformation2
                        )).build());

        getMockMvc().perform(MockMvcRequestBuilders
                .get(BASEPATH)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.result", hasSize(2)))
                .andExpect(jsonPath("$.result[0].id", is(ID1)))
                .andExpect(jsonPath("$.result[0].name", is(NAME1)))
                .andExpect(jsonPath("$.result[0].guid", is(GUID1)))
                .andExpect(jsonPath("$.result[0].description", is(DESCRIPTION1)))
                .andExpect(jsonPath("$.result[0].steps", hasSize(1)))
                .andExpect(jsonPath("$.result[0].steps[0].type", is("REST")))
                .andExpect(jsonPath("$.result[0].steps[0].attributes.username", is(username)))
                .andExpect(jsonPath("$.result[0].steps[0].attributes.url", is(url1)))
                .andExpect(jsonPath("$.result[0].steps[0].attributes.password", is(password)))
                .andExpect(jsonPath("$.result[1].id", is(ID2)))
                .andExpect(jsonPath("$.result[1].name", is(NAME2)))
                .andExpect(jsonPath("$.result[1].guid", is(GUID2)))
                .andExpect(jsonPath("$.result[1].description", is(DESCRIPTION2)))
                .andExpect(jsonPath("$.result[1].steps", hasSize(1)))
                .andExpect(jsonPath("$.result[1].steps[0].type", is("REST")))
                .andExpect(jsonPath("$.result[1].steps[0].attributes.username", is(username)))
                .andExpect(jsonPath("$.result[1].steps[0].attributes.url", is(url2)))
                .andExpect(jsonPath("$.result[1].steps[0].attributes.password", is(password)));;

    }

    @Test
    public void shouldReadTransformation() throws Exception {
        when(transformationService.get(tenant, GUID1))
                .thenReturn(ServiceResponseBuilder.<Transformation>ok()
                        .withResult(validTransformation1).build());

        getMockMvc().perform(MockMvcRequestBuilders
                .get(BASEPATH + "/" + GUID1)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.result.id", is(ID1)))
                .andExpect(jsonPath("$.result.name", is(NAME1)))
                .andExpect(jsonPath("$.result.description", is(DESCRIPTION1)))
                .andExpect(jsonPath("$.result.guid", is(GUID1)))
                .andExpect(jsonPath("$.result.steps", hasSize(1)))
                .andExpect(jsonPath("$.result.steps[0].type", is("REST")))
                .andExpect(jsonPath("$.result.steps[0].attributes.username", is(username)))
                .andExpect(jsonPath("$.result.steps[0].attributes.url", is(url1)))
                .andExpect(jsonPath("$.result.steps[0].attributes.password", is(password)));

    }

    @Test
    public void shouldCreateTransformation() throws Exception {
        Transformation newTransformation = Transformation.builder()
                .id(null)
                .guid(null)
                .name(NAME1 + "NEW")
                .description(DESCRIPTION1 + "NEW")
                .tenant(null)
                .steps(validTransformation1.getSteps())
                .build();

        Transformation newTransformationResult = Transformation.builder()
                .id(ID1)
                .guid(GUID1)
                .name(NAME1 + "NEW")
                .description(DESCRIPTION1 + "NEW")
                .tenant(null)
                .steps(validTransformation1.getSteps())
                .build();

        TransformationVO vo = new TransformationVO().apply(newTransformation);

        when(transformationService.register(tenant, newTransformation))
                .thenReturn(ServiceResponseBuilder.<Transformation> ok().withResult(newTransformationResult).build());

        getMockMvc().perform(MockMvcRequestBuilders
                .post(BASEPATH + "/")
                .content(getJson(vo))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.CREATED.value())))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.result.id", is(ID1)))
                .andExpect(jsonPath("$.result.name", is(NAME1 + "NEW")))
                .andExpect(jsonPath("$.result.description", is(DESCRIPTION1 + "NEW")))
                .andExpect(jsonPath("$.result.guid", is(GUID1)))
                .andExpect(jsonPath("$.result.steps", hasSize(1)))
                .andExpect(jsonPath("$.result.steps[0].type", is("REST")))
                .andExpect(jsonPath("$.result.steps[0].attributes.username", is(username)))
                .andExpect(jsonPath("$.result.steps[0].attributes.url", is(url1)))
                .andExpect(jsonPath("$.result.steps[0].attributes.password", is(password)));

    }

    @Test
    public void shouldUpdateTransformation() throws Exception {
        Transformation changedValues = Transformation.builder()
                .id(ID1)
                .guid(GUID1)
                .name(NAME1 + "CHANGED")
                .description(DESCRIPTION1 + "CHANGED")
                .tenant(null)
                .steps(validTransformation1.getSteps())
                .build();

        TransformationVO vo = new TransformationVO().apply(changedValues);

        when(transformationService.get(tenant, GUID1))
                .thenReturn(ServiceResponseBuilder.<Transformation>ok()
                        .withResult(validTransformation1).build());

        when(transformationService.update(tenant, GUID1, changedValues))
                .thenReturn(ServiceResponseBuilder.<Transformation> ok().withResult(changedValues).build());

        getMockMvc().perform(MockMvcRequestBuilders
                .put(BASEPATH + "/" + GUID1)
                .content(getJson(vo))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType("application/json;charset=UTF-8"));
    }

    @Test
    public void shouldDeleteDevice() throws Exception {
        when(transformationService.remove(tenant, GUID1))
                .thenReturn(ServiceResponseBuilder.<Transformation> ok().build());

        getMockMvc().perform(MockMvcRequestBuilders
                .delete(BASEPATH + GUID1)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

}
