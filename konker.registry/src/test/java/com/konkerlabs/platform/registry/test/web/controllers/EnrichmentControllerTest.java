package com.konkerlabs.platform.registry.test.web.controllers;


import com.konkerlabs.platform.registry.business.model.DataEnrichmentExtension;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.behaviors.DeviceURIDealer;
import com.konkerlabs.platform.registry.business.model.enumerations.IntegrationType;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.services.api.*;
import com.konkerlabs.platform.registry.config.WebMvcConfig;
import com.konkerlabs.platform.registry.test.base.SecurityTestConfiguration;
import com.konkerlabs.platform.registry.test.base.WebLayerTestContext;
import com.konkerlabs.platform.registry.test.base.WebTestConfiguration;
import com.konkerlabs.platform.registry.web.controllers.EnrichmentController;
import com.konkerlabs.platform.registry.web.forms.EnrichmentForm;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.text.MessageFormat;
import java.util.*;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {
        WebMvcConfig.class,
        WebTestConfiguration.class,
        SecurityTestConfiguration.class,
        EnrichmentControllerTest.EnrichmentTestContextConfig.class
})
public class EnrichmentControllerTest extends WebLayerTestContext {

    @Autowired
    private DeviceRegisterService deviceRegisterService;
    @Autowired
    private DataEnrichmentExtensionService dataEnrichmentExtensionService;
    @Autowired
    private Tenant tenant;
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private MockHttpServletRequest request;

    private Device incomingDevice;
    private DeviceURIDealer deviceUriDealer;
    private DataEnrichmentExtension dataEnrichmentExtension;
    private NewServiceResponse<List<DataEnrichmentExtension>> listServiceResponse;
    private NewServiceResponse<DataEnrichmentExtension> serviceResponse;
    private EnrichmentForm enrichmentForm;
    private MultiValueMap<String, String> enrichmentData;

    private String enrichmentGuid = "aac07163-4192-4db2-89a8-6f2b2aac514c";

    @Before
    public void setUp() {
        when(deviceRegisterService.findAll(tenant))
        .thenReturn(ServiceResponseBuilder.<List<Device>>ok()
                .withResult(Collections.emptyList()).build());
        
        incomingDevice = Device.builder().tenant(tenant).deviceId("1").build();
        deviceUriDealer = new DeviceURIDealer() {
        };

        enrichmentForm = new EnrichmentForm();
        enrichmentForm.setName("EnrichmentTest1");
        enrichmentForm.setDescription("Testing the enrichment form.");
        enrichmentForm.setType("REST");
        enrichmentForm.setIncomingAuthority(incomingDevice.getDeviceId());
        enrichmentForm.setParameters(new HashMap<String, String>() {{
            put("URL", "http://my.enriching.service.com");
            put("User", "admin");
            put("Password", "secret");
        }});
        enrichmentForm.setContainerKey("fieldTest");
        enrichmentForm.setActive(true);

        enrichmentForm.setAdditionalSupplier(() -> tenant.getDomainName());

        dataEnrichmentExtension = DataEnrichmentExtension.builder()
                .name(enrichmentForm.getName())
                .description(enrichmentForm.getDescription())
                .incoming(deviceUriDealer.toDeviceRouteURI(tenant.getDomainName(), "1"))
                .type(IntegrationType.REST)
                .parameters(enrichmentForm.getParameters())
                .containerKey(enrichmentForm.getContainerKey())
                .active(enrichmentForm.isActive())
                .build();

        enrichmentData = new LinkedMultiValueMap<>();
        enrichmentData.add("name", enrichmentForm.getName());
        enrichmentData.add("type", enrichmentForm.getType());
        enrichmentData.add("description", enrichmentForm.getDescription());
        enrichmentData.add("incomingAuthority", enrichmentForm.getIncomingAuthority());
        enrichmentData.add("containerKey", enrichmentForm.getContainerKey());
        enrichmentData.add("parameters['URL']", enrichmentForm.getParameters().get("URL"));
        enrichmentData.add("parameters['User']", enrichmentForm.getParameters().get("User"));
        enrichmentData.add("parameters['Password']", enrichmentForm.getParameters().get("Password"));
        enrichmentData.add("active", String.valueOf(enrichmentForm.isActive()));

        serviceResponse = ServiceResponseBuilder.<DataEnrichmentExtension>ok()
                .withResult(dataEnrichmentExtension)
                .build();

        listServiceResponse = ServiceResponseBuilder.<List<DataEnrichmentExtension>>ok()
                .withResult(Arrays.asList(new DataEnrichmentExtension[]{dataEnrichmentExtension}))
                .build();
    }

    @After
    public void tearDown() {
        Mockito.reset(dataEnrichmentExtensionService);
        Mockito.reset(deviceRegisterService);
    }

    @Test
    public void shouldListAllSavedEnrichmentRegisters() throws Exception {
        when(dataEnrichmentExtensionService.getAll(tenant)).thenReturn(listServiceResponse);

        getMockMvc().perform(get("/enrichment")).andExpect(model().attribute("dataEnrichmentExtensions", equalTo(listServiceResponse.getResult())))
                .andExpect(view().name("enrichment/index"));
    }

    @Test
    public void shouldShowCreationForm() throws Exception {
        getMockMvc().perform(get("/enrichment/new"))
                .andExpect(view().name("enrichment/form"))
                .andExpect(model().attribute("dataEnrichmentExtension", new EnrichmentForm()))
                .andExpect(model().attribute("action", "/enrichment/save"));
    }

    @Test
    public void shouldBindErrorMessagesWhenRegistrationFailsAndGoBackToCreationForm() throws Exception {
        String exceptionMessage = CommonValidations.RECORD_NULL.getCode();

        serviceResponse = spy(ServiceResponseBuilder.<DataEnrichmentExtension>error()
                .withMessage(exceptionMessage)
                .<DataEnrichmentExtension>build());

        when(dataEnrichmentExtensionService.register(eq(tenant), eq(dataEnrichmentExtension))).thenReturn(serviceResponse);

        getMockMvc().perform(post("/enrichment/save").params(enrichmentData))
                .andExpect(model().attribute("errors",
                    equalTo(Arrays.asList(new String[]{
                        applicationContext.getMessage(exceptionMessage, null, Locale.ENGLISH)
                    }))))
                .andExpect(model().attribute("dataEnrichmentExtension", equalTo(enrichmentForm)))
                .andExpect(model().attribute("method", ""))
                .andExpect(view().name("enrichment/form"));

        verify(dataEnrichmentExtensionService).register(eq(tenant), eq(dataEnrichmentExtension));
    }

    @Test
    public void shouldRedirectToShowAfterSuccessfulEnrichmentCreation() throws Exception {
        serviceResponse = spy(ServiceResponseBuilder.<DataEnrichmentExtension>ok()
                .withResult(dataEnrichmentExtension)
                .<DataEnrichmentExtension>build());

        when(dataEnrichmentExtensionService.register(eq(tenant), eq(dataEnrichmentExtension))).thenReturn(serviceResponse);

        getMockMvc().perform(post("/enrichment/save").params(enrichmentData))
                .andExpect(flash().attribute("message", "Enrichment registered successfully"))
                .andExpect(redirectedUrl(MessageFormat.format("/enrichment/{0}", dataEnrichmentExtension.getGuid())));


        verify(dataEnrichmentExtensionService).register(tenant, dataEnrichmentExtension);
    }

    @Test
    public void shouldShowEditForm() throws Exception {
        when(dataEnrichmentExtensionService.getByGUID(tenant, dataEnrichmentExtension.getName())).thenReturn(serviceResponse);

        getMockMvc().perform(get(MessageFormat.format("/enrichment/{0}/edit", dataEnrichmentExtension.getName())))
                .andExpect(view().name("enrichment/form"))
                .andExpect(model().attribute("dataEnrichmentExtension", new EnrichmentForm().fillFrom(dataEnrichmentExtension)))
                .andExpect(model().attribute("action", MessageFormat.format("/enrichment/{0}", dataEnrichmentExtension.getName())))
                .andExpect(model().attribute("method", "put"));
    }

    @Test
    public void shouldBindErrorMessagesWhenUpdateFailsAndGoBackToEditForm() throws Exception {
        String exceptionMessage = CommonValidations.RECORD_NULL.getCode();

        serviceResponse = spy(ServiceResponseBuilder.<DataEnrichmentExtension>error()
                .withMessage(exceptionMessage)
                .<DataEnrichmentExtension>build());

        when(dataEnrichmentExtensionService.update(eq(tenant), eq(enrichmentGuid), eq(dataEnrichmentExtension)))
                .thenReturn(serviceResponse);

        getMockMvc().perform(put(MessageFormat.format("/enrichment/{0}", enrichmentGuid)).params(enrichmentData))
                .andExpect(model().attribute("errors",
                    equalTo(Arrays.asList(new String[]{applicationContext.getMessage(CommonValidations.RECORD_NULL.getCode(),null, Locale.ENGLISH)}))
                ))
                .andExpect(model().attribute("method","put"))
                .andExpect(model().attribute("dataEnrichmentExtension", equalTo(enrichmentForm)))
                .andExpect(view().name("enrichment/form"));

        verify(dataEnrichmentExtensionService).update(eq(tenant), eq(enrichmentGuid), eq(dataEnrichmentExtension));
    }

    @Test
    public void shouldRedirectToShowAfterSuccessfulEnrichmentEdit() throws Exception {
        serviceResponse = spy(ServiceResponseBuilder.<DataEnrichmentExtension>ok()
                .withResult(dataEnrichmentExtension)
                .<DataEnrichmentExtension>build());

        when(dataEnrichmentExtensionService.update(eq(tenant), eq(enrichmentGuid), eq(dataEnrichmentExtension))).thenReturn(serviceResponse);

        getMockMvc().perform(put(MessageFormat.format("/enrichment/{0}", enrichmentGuid)).params(enrichmentData))
                .andExpect(flash().attribute("message",
                    applicationContext.getMessage(EnrichmentController.Messages.ENRICHMENT_REGISTERED_SUCCESSFULLY.getCode(),null,Locale.ENGLISH)
                ))
                .andExpect(redirectedUrl(MessageFormat.format("/enrichment/{0}", dataEnrichmentExtension.getGuid())));

        verify(dataEnrichmentExtensionService).update(eq(tenant), eq(enrichmentGuid), eq(dataEnrichmentExtension));
    }

    @Test
    public void shouldShowEnrichmentDetails() throws Exception {
        when(dataEnrichmentExtensionService.getByGUID(tenant, dataEnrichmentExtension.getName())).thenReturn(serviceResponse);

        getMockMvc().perform(get(MessageFormat.format("/enrichment/{0}", dataEnrichmentExtension.getName())))
                .andExpect(view().name("enrichment/show"))
                .andExpect(model().attribute("dataEnrichmentExtension", new EnrichmentForm().fillFrom(dataEnrichmentExtension)));
    }

    @Test
    public void shoudlRedirectToRouteIndexAfterRouteRemoval() throws Exception {
        dataEnrichmentExtension.setGuid(enrichmentGuid);

        spy(serviceResponse);
        spy(listServiceResponse);

        when(dataEnrichmentExtensionService.remove(tenant, dataEnrichmentExtension.getGuid())).thenReturn(serviceResponse);
        when(dataEnrichmentExtensionService.getAll(eq(tenant))).thenReturn(listServiceResponse);

        getMockMvc().perform(delete("/enrichment/{0}", dataEnrichmentExtension.getGuid()))
                .andExpect(flash().attribute("message",
                    applicationContext.getMessage(EnrichmentController.Messages.ENRICHMENT_REMOVED_SUCCESSFULLY.getCode(),null,Locale.ENGLISH)))
                .andExpect(redirectedUrl("/enrichment"));

        verify(dataEnrichmentExtensionService).remove(tenant, dataEnrichmentExtension.getGuid());
    }

    @Configuration
    static class EnrichmentTestContextConfig {
        @Bean
        public DataEnrichmentExtensionService dataEnrichmentExtensionService() {
            return mock(DataEnrichmentExtensionService.class);
        }

        @Bean
        public DeviceRegisterService deviceRegisterService() {
            return mock(DeviceRegisterService.class);
        }
    }
}
