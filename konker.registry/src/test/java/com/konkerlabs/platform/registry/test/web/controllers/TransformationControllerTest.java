package com.konkerlabs.platform.registry.test.web.controllers;

import com.konkerlabs.platform.registry.business.model.RestTransformationStep;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.Transformation;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.TransformationService;
import com.konkerlabs.platform.registry.config.WebMvcConfig;
import com.konkerlabs.platform.registry.test.base.SecurityTestConfiguration;
import com.konkerlabs.platform.registry.test.base.WebLayerTestContext;
import com.konkerlabs.platform.registry.test.base.WebTestConfiguration;
import com.konkerlabs.platform.registry.web.forms.TransformationForm;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {
        WebMvcConfig.class,
        WebTestConfiguration.class,
        SecurityTestConfiguration.class,
        TransformationControllerTest.TransformationTestContextConfig.class
})
public class TransformationControllerTest extends WebLayerTestContext {

    @Autowired
    private TransformationService transformationService;
    @Autowired
    private Tenant tenant;

    private List<Transformation> transformations;
    private Transformation transformation;
    private TransformationForm transformationForm;
    private MultiValueMap<String, String> transformationData;

    private String url = "http://google.com/test";
    private String username = "root";
    private String password = "secret";

    @Before
    public void setUp() {
        transformations = new ArrayList<>();
        transformation = Transformation.builder()
                .name("TransformationTest")
                .step(RestTransformationStep.builder().attributes(new HashMap<String,String>(){{
                    put("url",url);
                    put("username",username);
                    put("password",password);
                }}).build())
                .build();

        transformations.add(transformation);

        transformationForm = new TransformationForm();
        transformationForm.setName(transformation.getName());
        transformationForm.setSteps(
            transformation.getSteps().stream()
                .map(transformationStep -> new TransformationForm.TransformationStepForm(url,username,password))
                .collect(Collectors.toList())
        );

        transformationData = new LinkedMultiValueMap<>();
        transformationData.add("name", transformation.getName());
        transformationData.add("steps[0].url", url);
        transformationData.add("steps[0].username", username);
        transformationData.add("steps[0].password", password);
    }

    @Test
    public void shouldReturnAllRegisteredTransformations() throws Exception {
        when(transformationService.getAll(tenant)).thenReturn(ServiceResponse.<List<Transformation>>builder()
                .status(ServiceResponse.Status.OK)
                .result(transformations).<List<Transformation>>build());

        getMockMvc().perform(get("/transformation")).andExpect(model()
                .attribute("transformations", transformations))
                .andExpect(view().name("transformations/index"));
    }

    @Test
    public void shouldOpenNewTransformationForm() throws Exception {
        getMockMvc().perform(get("/transformation/new"))
                .andExpect(model().attribute("transformation", new TransformationForm()))
                .andExpect(view().name("transformations/form"))
                .andExpect(model().attribute("action", "/transformation/save"));
    }

    @Test
    public void shouldBindAnErrorMessageOnSaveError() throws Exception {
        when(transformationService.register(eq(tenant), eq(transformation))).thenReturn(ServiceResponse.<Transformation>builder()
                .status(ServiceResponse.Status.ERROR)
                .responseMessage("Any errors").<Transformation>build());

        getMockMvc().perform(post("/transformation/save").params(transformationData))
                .andExpect(model().attribute("transformation", transformationForm))
                .andExpect(model().attribute("errors", Arrays.asList(new String[]{"Any errors"})))
                .andExpect(view().name("/transformations/form"))
                .andExpect(model().attribute("action", "/transformation/save"));
    }

    @Configuration
    public static class TransformationTestContextConfig {
        @Bean
        public TransformationService transformationService() {
            return Mockito.mock(TransformationService.class);
        }
    }
}