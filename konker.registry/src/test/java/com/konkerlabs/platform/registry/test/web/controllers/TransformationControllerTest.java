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
import com.konkerlabs.platform.registry.web.forms.EnrichmentForm;
import com.konkerlabs.platform.registry.web.forms.TransformationForm;
import org.junit.After;
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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    private ServiceResponse<Transformation> serviceResponse;
    private ServiceResponse<List<Transformation>> listServiceResponse;

    @Before
    public void setUp() {
        transformations = new ArrayList<>();
        transformation = Transformation.builder()
                .name("TransformationTest")
                .description("Test transformation")
                .step(RestTransformationStep.builder().attributes(new HashMap<String, String>() {{
                    put("url", url);
                    put("username", username);
                    put("password", password);
                }}).build())
                .build();

        transformations.add(transformation);

        transformationForm = new TransformationForm();
        transformationForm.setName(transformation.getName());
        transformationForm.setDescription(transformation.getDescription());
        transformationForm.setSteps(
                transformation.getSteps().stream()
                        .map(transformationStep -> new TransformationForm.TransformationStepForm(url, username, password))
                        .collect(Collectors.toList())
        );

        transformationData = new LinkedMultiValueMap<>();
        transformationData.add("name", transformation.getName());
        transformationData.add("description", transformation.getDescription());
        transformationData.add("steps[0].url", url);
        transformationData.add("steps[0].username", username);
        transformationData.add("steps[0].password", password);

        serviceResponse = ServiceResponse.<Transformation>builder()
                .status(ServiceResponse.Status.OK)
                .result(transformation).<Transformation>build();

        listServiceResponse = ServiceResponse.<List<Transformation>>builder()
                .status(ServiceResponse.Status.OK)
                .result(transformations).<List<Transformation>>build();
    }

    @After
    public void tearDown() {
        Mockito.reset(transformationService);
    }

    @Test
    public void shouldReturnAllRegisteredTransformations() throws Exception {
        when(transformationService.getAll(tenant))
                .thenReturn(ServiceResponse.<List<Transformation>>builder()
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
    public void shouldBindAnErrorMessageOnSaveTransformationError() throws Exception {
        when(transformationService.register(eq(tenant), eq(transformation)))
                .thenReturn(ServiceResponse.<Transformation>builder()
                        .status(ServiceResponse.Status.ERROR)
                        .responseMessage("Any errors").<Transformation>build());

        getMockMvc().perform(post("/transformation/save").params(transformationData))
                .andExpect(model().attribute("transformation", transformationForm))
                .andExpect(model().attribute("errors", Arrays.asList(new String[]{"Any errors"})))
                .andExpect(view().name("/transformations/form"));

        verify(transformationService).register(tenant, transformation);
    }

    @Test
    public void shouldSaveNewTransformationSuccessfully() throws Exception {
        serviceResponse = spy(ServiceResponse.<Transformation>builder()
                .status(ServiceResponse.Status.OK)
                .result(transformation).<Transformation>build());

        when(transformationService.register(eq(tenant), eq(transformation))).thenReturn(serviceResponse);

        getMockMvc().perform(post("/transformation/save").params(transformationData))
                .andExpect(flash().attribute("message", "Transformation registered successfully"))
                .andExpect(redirectedUrl(MessageFormat.format("/transformation/{0}", transformation.getId())));

        verify(transformationService).register(tenant, transformation);
    }

    @Test
    public void shouldBindAnErrorMessageOnEditTransformationError() throws Exception {
        when(transformationService.update(eq(tenant), eq("123"), eq(transformation)))
                .thenReturn(ServiceResponse.<Transformation>builder()
                        .status(ServiceResponse.Status.ERROR)
                        .responseMessage("Any errors").<Transformation>build());

        getMockMvc().perform(post(MessageFormat.format("/transformation/{0}", "123")).params(transformationData))
                .andExpect(model().attribute("transformation", transformationForm))
                .andExpect(model().attribute("errors", Arrays.asList(new String[]{"Any errors"})))
                .andExpect(view().name("transformations/form"));

        verify(transformationService).update(tenant, "123", transformation);
    }

    @Test
    public void shouldUpdateTransformationSuccessfully() throws Exception {
        serviceResponse = spy(ServiceResponse.<Transformation>builder()
                .status(ServiceResponse.Status.OK)
                .result(transformation).<Transformation>build());

        when(transformationService.update(eq(tenant), eq("123"), eq(transformation))).thenReturn(serviceResponse);

        getMockMvc().perform(post(MessageFormat.format("/transformation/{0}", "123")).params(transformationData))
                .andExpect(flash().attribute("message", "Transformation updated successfully"))
                .andExpect(redirectedUrl(MessageFormat.format("/transformation/{0}", transformation.getId())));

        verify(transformationService).update(tenant, "123", transformation);
    }

    @Test
    public void shouldShowDetailsOfASelectedTransformation() throws Exception {
        transformation.setId("123");
        serviceResponse = spy(ServiceResponse.<Transformation>builder()
                .status(ServiceResponse.Status.OK)
                .result(transformation).<Transformation>build());

        when(transformationService.get(tenant, transformation.getId())).thenReturn(serviceResponse);

        getMockMvc().perform(get(MessageFormat.format("/transformation/{0}", transformation.getId())))
                .andExpect(view().name("transformations/show"))
                .andExpect(model().attribute("transformation", new TransformationForm().fillFrom(transformation)));
    }

    @Test
    public void shouldShowEditForm() throws Exception {
        transformation.setId("123");
        serviceResponse = spy(ServiceResponse.<Transformation>builder()
                .status(ServiceResponse.Status.OK)
                .result(transformation).<Transformation>build());

        when(transformationService.get(tenant, transformation.getId())).thenReturn(serviceResponse);

        getMockMvc().perform(get(MessageFormat.format("/transformation/{0}/edit", transformation.getId())))
                .andExpect(view().name("transformations/form"))
                .andExpect(model().attribute("transformation", new TransformationForm().fillFrom(transformation)))
                .andExpect(model().attribute("action", MessageFormat.format("/transformation/{0}", transformation.getId())));
    }


    @Configuration
    public static class TransformationTestContextConfig {
        @Bean
        public TransformationService transformationService() {
            return Mockito.mock(TransformationService.class);
        }
    }
}