package com.konkerlabs.platform.registry.test.web.controllers;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

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

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.RestTransformationStep;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.Transformation;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.business.services.api.TransformationService;
import com.konkerlabs.platform.registry.config.CdnConfig;
import com.konkerlabs.platform.registry.config.EmailConfig;
import com.konkerlabs.platform.registry.config.MessageSourceConfig;
import com.konkerlabs.platform.registry.config.WebConfig;
import com.konkerlabs.platform.registry.config.WebMvcConfig;
import com.konkerlabs.platform.registry.test.base.SecurityTestConfiguration;
import com.konkerlabs.platform.registry.test.base.WebLayerTestContext;
import com.konkerlabs.platform.registry.test.base.WebTestConfiguration;
import com.konkerlabs.platform.registry.web.controllers.TransformationController;
import com.konkerlabs.platform.registry.web.forms.TransformationForm;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = { WebMvcConfig.class, WebTestConfiguration.class, SecurityTestConfiguration.class,
        TransformationControllerTest.TransformationTestContextConfig.class, WebConfig.class,
        CdnConfig.class, EmailConfig.class, MessageSourceConfig.class})
public class TransformationControllerTest extends WebLayerTestContext {

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private TransformationService transformationService;
    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private Tenant tenant;

    private List<Transformation> transformations;
    private Transformation transformation;
    private TransformationForm transformationForm;
    private MultiValueMap<String, String> transformationData;

    private String method = "POST";
    private String url = "http://google.com/test";
    private String username = "root";
    private String password = "secret";

    private ServiceResponse<Transformation> serviceResponse;
    private ServiceResponse<List<Transformation>> listServiceResponse;
    private Application application;

    @Before
    public void setUp() {
        application = Application.builder()
                .name(tenant.getDomainName())
                .build();

        transformations = new ArrayList<>();
        transformation = Transformation.builder().name("TransformationTest").description("Test transformation")
                .step(RestTransformationStep.builder().attributes(new HashMap<String, Object>() {
                    {
                        put("method", method);
                        put("url", url);
                        put("username", username);
                        put("password", password);
                    }
                }).build())
                .application(application)
                .build();

        transformations.add(transformation);

        transformationForm = new TransformationForm();
        transformationForm.setName(transformation.getName());
        transformationForm.setDescription(transformation.getDescription());
        transformationForm.setSteps(transformation.getSteps().stream()
                .map(transformationStep -> new TransformationForm.TransformationStepForm(method, url, username, password))
                .collect(Collectors.toList()));
        transformationForm.setApplication(application);

        transformationData = new LinkedMultiValueMap<>();
        transformationData.add("name", transformation.getName());
        transformationData.add("description", transformation.getDescription());
        transformationData.add("steps[0].method", method);
        transformationData.add("steps[0].url", url);
        transformationData.add("steps[0].username", username);
        transformationData.add("steps[0].password", password);
        transformationData.add("application.name", application.getName());

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
        when(transformationService.getAll(tenant, application))
                .thenReturn(ServiceResponseBuilder.<List<Transformation>> ok().withResult(transformations).build());
        
        when(applicationService.findAll(tenant))
        	.thenReturn(ServiceResponseBuilder.<List<Application>> ok().withResult(Collections.singletonList(application)).build());

        getMockMvc().perform(get("/transformation")).andExpect(model().attribute("transformations", transformations))
                .andExpect(view().name("transformations/index"));
    }

    @Test
    @WithMockUser(authorities={"CREATE_TRANSFORMATION"})
    public void shouldOpenNewTransformationForm() throws Exception {
        getMockMvc().perform(get("/transformation/new"))
                .andExpect(model().attribute("transformation", new TransformationForm()))
                .andExpect(view().name("transformations/form"))
                .andExpect(model().attribute("action", MessageFormat.format("/transformation/{0}/save", application.getName())));
    }

    @Test
    @WithMockUser(authorities={"CREATE_TRANSFORMATION"})
    public void shouldBindAnErrorMessageOnSaveTransformationError() throws Exception {
        when(transformationService.register(eq(tenant), eq(application), any(Transformation.class))).thenReturn(ServiceResponseBuilder
                .<Transformation> error().withMessage(CommonValidations.RECORD_NULL.getCode()).build());
        
        when(applicationService.getByApplicationName(tenant, application.getName()))
    		.thenReturn(ServiceResponseBuilder.<Application> ok().withResult(application).build());

        getMockMvc().perform(post("/transformation/{0}/save", application.getName()).params(transformationData))
                .andExpect(model().attribute("transformation", transformationForm))
                .andExpect(model().attribute("errors",
                        Arrays.asList(new String[] { applicationContext
                                .getMessage(CommonValidations.RECORD_NULL.getCode(), null, Locale.ENGLISH) })))
                .andExpect(model().attribute("method", "")).andExpect(view().name("transformations/form"));


    }

    @Test
    @WithMockUser(authorities={"CREATE_TRANSFORMATION"})
    public void shouldSaveNewTransformationSuccessfully() throws Exception {
        serviceResponse = spy(ServiceResponseBuilder.<Transformation> ok().withResult(transformation).build());

        when(transformationService.register(eq(tenant), eq(application), any(Transformation.class))).thenReturn(serviceResponse);
        when(applicationService.getByApplicationName(tenant, application.getName()))
    		.thenReturn(ServiceResponseBuilder.<Application> ok().withResult(application).build());

        getMockMvc().perform(post("/transformation/{0}/save", application.getName()).params(transformationData))
                .andExpect(flash().attribute("message",
                        applicationContext.getMessage(
                                TransformationController.Messages.TRANSFORMATION_REGISTERED_SUCCESSFULLY.getCode(),
                                null, Locale.ENGLISH)))
                .andExpect(redirectedUrl(MessageFormat.format("/transformation/{0}/{1}", application.getName(), transformation.getId())));


    }

    @Test
    @WithMockUser(authorities={"EDIT_TRANSFORMATION"})
    public void shouldBindAnErrorMessageOnEditTransformationError() throws Exception {
        when(transformationService.update(eq(tenant), eq(application), eq("123"), any(Transformation.class))).thenReturn(ServiceResponseBuilder
                .<Transformation> error().withMessage(CommonValidations.RECORD_NULL.getCode()).build());
        when(applicationService.getByApplicationName(tenant, application.getName()))
    		.thenReturn(ServiceResponseBuilder.<Application> ok().withResult(application).build());

        getMockMvc().perform(put(MessageFormat.format("/transformation/{0}/{1}", application.getName(), "123")).params(transformationData))
                .andExpect(model().attribute("transformation", transformationForm))
                .andExpect(model().attribute("errors",
                        Arrays.asList(new String[] { applicationContext
                                .getMessage(CommonValidations.RECORD_NULL.getCode(), null, Locale.ENGLISH) })))
                .andExpect(model().attribute("method", "put")).andExpect(view().name("transformations/form"));


    }

    @Test
    @WithMockUser(authorities={"EDIT_TRANSFORMATION"})
    public void shouldUpdateTransformationSuccessfully() throws Exception {
        serviceResponse = spy(
                ServiceResponseBuilder.<Transformation> ok().withResult(transformation).<Transformation> build());

        when(transformationService.update(eq(tenant),eq(application), eq("123"), any(Transformation.class))).thenReturn(serviceResponse);
        when(applicationService.getByApplicationName(tenant, application.getName()))
    		.thenReturn(ServiceResponseBuilder.<Application> ok().withResult(application).build());

        getMockMvc().perform(put(MessageFormat.format("/transformation/{0}/{1}", application.getName(), "123")).params(transformationData))
                .andExpect(flash().attribute("message",
                        applicationContext.getMessage(
                                TransformationController.Messages.TRANSFORMATION_REGISTERED_SUCCESSFULLY.getCode(),
                                null, Locale.ENGLISH)))
                .andExpect(redirectedUrl(MessageFormat.format("/transformation/{0}/{1}", application.getName(), transformation.getId())));


    }

    @Test
    @WithMockUser(authorities={"SHOW_TRANSFORMATION"})
    public void shouldShowDetailsOfASelectedTransformation() throws Exception {
        transformation.setId("123");
        serviceResponse = spy(
                ServiceResponseBuilder.<Transformation> ok().withResult(transformation).<Transformation> build());

        when(transformationService.get(tenant, application, transformation.getId())).thenReturn(serviceResponse);
        when(applicationService.getByApplicationName(tenant, application.getName()))
    		.thenReturn(ServiceResponseBuilder.<Application> ok().withResult(application).build());

        getMockMvc().perform(get(MessageFormat.format("/transformation/{0}/{1}", application.getName(), transformation.getId())))
                .andExpect(view().name("transformations/show"))
                .andExpect(model().attribute("transformation", new TransformationForm().fillFrom(transformation)));
    }

    @Test
    @WithMockUser(authorities={"EDIT_TRANSFORMATION"})
    public void shouldShowEditForm() throws Exception {
        transformation.setId("123");
        serviceResponse = spy(
                ServiceResponseBuilder.<Transformation> ok().withResult(transformation).<Transformation> build());

        when(transformationService.get(tenant, application, transformation.getId())).thenReturn(serviceResponse);
        when(applicationService.getByApplicationName(tenant, application.getName()))
        	.thenReturn(ServiceResponseBuilder.<Application> ok().withResult(application).build());

        getMockMvc().perform(get(MessageFormat.format("/transformation/{0}/{1}/edit", application.getName(), transformation.getId())))
                .andExpect(view().name("transformations/form"))
                .andExpect(model().attribute("transformation", new TransformationForm().fillFrom(transformation)))
                .andExpect(model().attribute("method", "put")).andExpect(model().attribute("action",
                        MessageFormat.format("/transformation/{0}/{1}", application.getName(), transformation.getId())));
    }

    @Configuration
    public static class TransformationTestContextConfig {
        @Bean
        public TransformationService transformationService() {
            return Mockito.mock(TransformationService.class);
        }
        @Bean
        public ApplicationService applicationService() {
            return Mockito.mock(ApplicationService.class);
        }
    }
    
    @Test
    @WithMockUser(authorities={"REMOVE_TRANSFORMATION"})
    public void shouldRedirectToTransformationIndexAfterRemoval() throws Exception {
    	transformation.setId("123");
    	spy(serviceResponse);
    	spy(listServiceResponse);
    	
    	when(transformationService.remove(tenant, application, transformation.getId())).thenReturn(serviceResponse);
    	when(transformationService.getAll(tenant, application)).thenReturn(listServiceResponse);
    	when(applicationService.getByApplicationName(tenant, application.getName()))
    		.thenReturn(ServiceResponseBuilder.<Application> ok().withResult(application).build());
    	
    	getMockMvc().perform(delete("/transformation/{0}/{1}", application.getName(), transformation.getId()))
    		.andExpect(flash().attribute("message", 
    				applicationContext.getMessage(TransformationController.Messages.TRANSFORMATION_REMOVED_SUCCESSFULLY.getCode(), null, Locale.ENGLISH)))
    		.andExpect(redirectedUrl("/transformation"));
    	
    	verify(transformationService).remove(tenant, application, transformation.getId());
    }
}