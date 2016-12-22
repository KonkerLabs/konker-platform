package com.konkerlabs.platform.registry.test.web.controllers;

import com.konkerlabs.platform.registry.business.model.RestTransformationStep;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.Transformation;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.business.services.api.TransformationService;
import com.konkerlabs.platform.registry.config.WebMvcConfig;
import com.konkerlabs.platform.registry.test.base.SecurityTestConfiguration;
import com.konkerlabs.platform.registry.test.base.WebLayerTestContext;
import com.konkerlabs.platform.registry.test.base.WebTestConfiguration;
import com.konkerlabs.platform.registry.web.controllers.TransformationController;
import com.konkerlabs.platform.registry.web.forms.TransformationForm;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = { WebMvcConfig.class, WebTestConfiguration.class, SecurityTestConfiguration.class,
        TransformationControllerTest.TransformationTestContextConfig.class })
public class TransformationControllerTest extends WebLayerTestContext {

    @Autowired
    private ApplicationContext applicationContext;
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
        transformation = Transformation.builder().name("TransformationTest").description("Test transformation")
                .step(RestTransformationStep.builder().attributes(new HashMap<String, String>() {
                    {
                        put("url", url);
                        put("username", username);
                        put("password", password);
                    }
                }).build()).build();

        transformations.add(transformation);

        transformationForm = new TransformationForm();
        transformationForm.setName(transformation.getName());
        transformationForm.setDescription(transformation.getDescription());
        transformationForm.setSteps(transformation.getSteps().stream()
                .map(transformationStep -> new TransformationForm.TransformationStepForm(url, username, password))
                .collect(Collectors.toList()));

        transformationData = new LinkedMultiValueMap<>();
        transformationData.add("name", transformation.getName());
        transformationData.add("description", transformation.getDescription());
        transformationData.add("steps[0].url", url);
        transformationData.add("steps[0].username", username);
        transformationData.add("steps[0].password", password);

        serviceResponse = ServiceResponseBuilder.<Transformation> ok().withResult(transformation)
                .<Transformation> build();

        listServiceResponse = ServiceResponseBuilder.<List<Transformation>> ok().withResult(transformations)
                .<Transformation> build();
    }

    @After
    public void tearDown() {
        Mockito.reset(transformationService);
    }

    @Test
    @WithMockUser(authorities={"LIST_TRANSFORMATION"})
    public void shouldReturnAllRegisteredTransformations() throws Exception {
        when(transformationService.getAll(tenant))
                .thenReturn(ServiceResponseBuilder.<List<Transformation>> ok().withResult(transformations).build());

        getMockMvc().perform(get("/transformation")).andExpect(model().attribute("transformations", transformations))
                .andExpect(view().name("transformations/index"));
    }

    @Test
    @WithMockUser(authorities={"CREATE_TRANSFORMATION"})
    public void shouldOpenNewTransformationForm() throws Exception {
        getMockMvc().perform(get("/transformation/new"))
                .andExpect(model().attribute("transformation", new TransformationForm()))
                .andExpect(view().name("transformations/form"))
                .andExpect(model().attribute("action", "/transformation/save"));
    }

    @Test
    @WithMockUser(authorities={"CREATE_TRANSFORMATION"})
    public void shouldBindAnErrorMessageOnSaveTransformationError() throws Exception {
        when(transformationService.register(eq(tenant), eq(transformation))).thenReturn(ServiceResponseBuilder
                .<Transformation> error().withMessage(CommonValidations.RECORD_NULL.getCode()).build());

        getMockMvc().perform(post("/transformation/save").params(transformationData))
                .andExpect(model().attribute("transformation", transformationForm))
                .andExpect(model().attribute("errors",
                        Arrays.asList(new String[] { applicationContext
                                .getMessage(CommonValidations.RECORD_NULL.getCode(), null, Locale.ENGLISH) })))
                .andExpect(model().attribute("method", "")).andExpect(view().name("transformations/form"));

        verify(transformationService).register(tenant, transformation);
    }

    @Test
    @WithMockUser(authorities={"CREATE_TRANSFORMATION"})
    public void shouldSaveNewTransformationSuccessfully() throws Exception {
        serviceResponse = spy(ServiceResponseBuilder.<Transformation> ok().withResult(transformation).build());

        when(transformationService.register(eq(tenant), eq(transformation))).thenReturn(serviceResponse);

        getMockMvc().perform(post("/transformation/save").params(transformationData))
                .andExpect(flash().attribute("message",
                        applicationContext.getMessage(
                                TransformationController.Messages.TRANSFORMATION_REGISTERED_SUCCESSFULLY.getCode(),
                                null, Locale.ENGLISH)))
                .andExpect(redirectedUrl(MessageFormat.format("/transformation/{0}", transformation.getId())));

        verify(transformationService).register(tenant, transformation);
    }

    @Test
    @WithMockUser(authorities={"EDIT_TRANSFORMATION"})
    public void shouldBindAnErrorMessageOnEditTransformationError() throws Exception {
        when(transformationService.update(eq(tenant), eq("123"), eq(transformation))).thenReturn(ServiceResponseBuilder
                .<Transformation> error().withMessage(CommonValidations.RECORD_NULL.getCode()).build());

        getMockMvc().perform(put(MessageFormat.format("/transformation/{0}", "123")).params(transformationData))
                .andExpect(model().attribute("transformation", transformationForm))
                .andExpect(model().attribute("errors",
                        Arrays.asList(new String[] { applicationContext
                                .getMessage(CommonValidations.RECORD_NULL.getCode(), null, Locale.ENGLISH) })))
                .andExpect(model().attribute("method", "put")).andExpect(view().name("transformations/form"));

        verify(transformationService).update(tenant, "123", transformation);
    }

    @Test
    @WithMockUser(authorities={"EDIT_TRANSFORMATION"})
    public void shouldUpdateTransformationSuccessfully() throws Exception {
        serviceResponse = spy(
                ServiceResponseBuilder.<Transformation> ok().withResult(transformation).<Transformation> build());

        when(transformationService.update(eq(tenant), eq("123"), eq(transformation))).thenReturn(serviceResponse);

        getMockMvc().perform(put(MessageFormat.format("/transformation/{0}", "123")).params(transformationData))
                .andExpect(flash().attribute("message",
                        applicationContext.getMessage(
                                TransformationController.Messages.TRANSFORMATION_REGISTERED_SUCCESSFULLY.getCode(),
                                null, Locale.ENGLISH)))
                .andExpect(redirectedUrl(MessageFormat.format("/transformation/{0}", transformation.getId())));

        verify(transformationService).update(tenant, "123", transformation);
    }

    @Test
    @WithMockUser(authorities={"SHOW_TRANSFORMATION"})
    public void shouldShowDetailsOfASelectedTransformation() throws Exception {
        transformation.setId("123");
        serviceResponse = spy(
                ServiceResponseBuilder.<Transformation> ok().withResult(transformation).<Transformation> build());

        when(transformationService.get(tenant, transformation.getId())).thenReturn(serviceResponse);

        getMockMvc().perform(get(MessageFormat.format("/transformation/{0}", transformation.getId())))
                .andExpect(view().name("transformations/show"))
                .andExpect(model().attribute("transformation", new TransformationForm().fillFrom(transformation)));
    }

    @Test
    @WithMockUser(authorities={"EDIT_TRANSFORMATION"})
    public void shouldShowEditForm() throws Exception {
        transformation.setId("123");
        serviceResponse = spy(
                ServiceResponseBuilder.<Transformation> ok().withResult(transformation).<Transformation> build());

        when(transformationService.get(tenant, transformation.getId())).thenReturn(serviceResponse);

        getMockMvc().perform(get(MessageFormat.format("/transformation/{0}/edit", transformation.getId())))
                .andExpect(view().name("transformations/form"))
                .andExpect(model().attribute("transformation", new TransformationForm().fillFrom(transformation)))
                .andExpect(model().attribute("method", "put")).andExpect(model().attribute("action",
                        MessageFormat.format("/transformation/{0}", transformation.getId())));
    }

    @Configuration
    public static class TransformationTestContextConfig {
        @Bean
        public TransformationService transformationService() {
            return Mockito.mock(TransformationService.class);
        }
    }
    
    @Test
    @WithMockUser(authorities={"REMOVE_TRANSFORMATION"})
    public void shouldRedirectToTransformationIndexAfterRemoval() throws Exception {
    	transformation.setId("123");
    	spy(serviceResponse);
    	spy(listServiceResponse);
    	
    	when(transformationService.remove(tenant, transformation.getId())).thenReturn(serviceResponse);
    	when(transformationService.getAll(tenant)).thenReturn(listServiceResponse);
    	
    	getMockMvc().perform(delete("/transformation/{0}", transformation.getId()))
    		.andExpect(flash().attribute("message", 
    				applicationContext.getMessage(TransformationController.Messages.TRANSFORMATION_REMOVED_SUCCESSFULLY.getCode(), null, Locale.ENGLISH)))
    		.andExpect(redirectedUrl("/transformation"));
    	
    	verify(transformationService).remove(tenant, transformation.getId());
    }
}