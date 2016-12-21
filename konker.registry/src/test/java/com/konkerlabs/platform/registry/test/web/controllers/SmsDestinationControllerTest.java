package com.konkerlabs.platform.registry.test.web.controllers;

import static java.text.MessageFormat.format;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.LinkedMultiValueMap;

import com.konkerlabs.platform.registry.business.model.SmsDestination;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.business.services.api.SmsDestinationService;
import com.konkerlabs.platform.registry.config.WebMvcConfig;
import com.konkerlabs.platform.registry.test.base.SecurityTestConfiguration;
import com.konkerlabs.platform.registry.test.base.WebLayerTestContext;
import com.konkerlabs.platform.registry.test.base.WebTestConfiguration;
import com.konkerlabs.platform.registry.web.controllers.SmsDestinationController;
import com.konkerlabs.platform.registry.web.forms.SmsDestinationForm;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = { WebMvcConfig.class, WebTestConfiguration.class, SecurityTestConfiguration.class,
        SmsDestinationControllerTest.SmsDestinationTestContextConfig.class })
@ActiveProfiles("sms")
public class SmsDestinationControllerTest extends WebLayerTestContext {

    @Autowired
    private SmsDestinationService smsDestinationService;
    @Autowired
    private Tenant tenant;
    @Autowired
    private ApplicationContext applicationContext;

    private SmsDestination destination;
    private SmsDestination savedDestination;
    private List<SmsDestination> registeredDestinations;
    private LinkedMultiValueMap<String, String> destinationData;
    private SmsDestinationForm destinationForm;
    private ServiceResponse<SmsDestination> responseSingleDestination;

    @Before
    public void setUp() {
        destination = SmsDestination.builder().name("Name").description("Description").phoneNumber("+5511987654321")
                .active(true).build();

        savedDestination = SmsDestination.builder().guid("guid").name("Name").description("Description")
                .phoneNumber("+5511987654321").active(true).build();

        registeredDestinations = new ArrayList<SmsDestination>();
        registeredDestinations.add(destination);

        destinationData = new LinkedMultiValueMap<>();
        destinationData.add("name", destination.getName());
        destinationData.add("description", destination.getDescription());
        destinationData.add("phoneNumber", destination.getPhoneNumber());
        destinationData.add("active", Boolean.valueOf(destination.isActive()).toString());

        destinationForm = new SmsDestinationForm();
        destinationForm.setName(destination.getName());
        destinationForm.setDescription(destination.getDescription());
        destinationForm.setPhoneNumber(destination.getPhoneNumber());
        destinationForm.setActive(destination.isActive());
    }

    @After
    public void tearDown() {
        Mockito.reset(smsDestinationService);
    }

    @Test
    @WithMockUser(authorities={"LIST_SMS_DESTINATIONS"})
    public void shouldListAllRegisteredDestinations() throws Exception {
        when(smsDestinationService.findAll(tenant)).thenReturn(ServiceResponseBuilder.<List<SmsDestination>> ok()
                .withResult(registeredDestinations).<List<SmsDestination>> build());

        getMockMvc().perform(get("/destinations/sms"))
                .andExpect(model().attribute("allDestinations", equalTo(registeredDestinations)))
                .andExpect(view().name("destinations/sms/index"));
    }

    @Test
    @WithMockUser(authorities={"CREATE_SMS_DESTINATION"})
    public void shouldShowRegistrationForm() throws Exception {
        getMockMvc().perform(get("/destinations/sms/new")).andExpect(view().name("destinations/sms/form"))
                .andExpect(model().attribute("destination", any(SmsDestinationForm.class)))
                .andExpect(model().attribute("action", "/destinations/sms/save"));
    }

    @Test
    @WithMockUser(authorities={"CREATE_SMS_DESTINATION"})
    public void shouldBindErrorMessagesWhenRegistrationFailsAndGoBackToRegistrationForm() throws Exception {
        responseSingleDestination = ServiceResponseBuilder.<SmsDestination> error()
                .withMessage(CommonValidations.TENANT_NULL.getCode()).<SmsDestination> build();

        when(smsDestinationService.register(eq(tenant), eq(destination))).thenReturn(responseSingleDestination);

        getMockMvc().perform(post("/destinations/sms/save").params(destinationData))
                .andExpect(model().attribute("errors",
                        equalTo(Arrays.asList(new String[] { applicationContext
                                .getMessage(CommonValidations.TENANT_NULL.getCode(), null, Locale.ENGLISH) }))))
                .andExpect(model().attribute("destination", equalTo(destinationForm)))
                .andExpect(model().attribute("method", "")).andExpect(view().name("destinations/sms/form"));

        verify(smsDestinationService).register(eq(tenant), eq(destination));
    }

    @Test
    @WithMockUser(authorities={"CREATE_SMS_DESTINATION"})
    public void shouldRedirectToShowAfterRegistrationSucceed() throws Exception {
        responseSingleDestination = ServiceResponseBuilder.<SmsDestination> ok().withResult(savedDestination)
                .<SmsDestination> build();

        when(smsDestinationService.register(eq(tenant), eq(destination))).thenReturn(responseSingleDestination);

        getMockMvc().perform(post("/destinations/sms/save").params(destinationData))
                .andExpect(flash().attribute("message",
                        applicationContext.getMessage(
                                SmsDestinationController.Messages.SMSDEST_REGISTERED_SUCCESSFULLY.getCode(), null,
                                Locale.ENGLISH)))
                .andExpect(redirectedUrl(format("/destinations/sms/{0}", savedDestination.getGuid())));

        verify(smsDestinationService).register(eq(tenant), eq(destination));
    }

    @Test
    @WithMockUser(authorities={"SHOW_SMS_DESTINATION"})
    public void shouldShowDestinationDetails() throws Exception {
        responseSingleDestination = ServiceResponseBuilder.<SmsDestination> ok().withResult(savedDestination)
                .<SmsDestination> build();

        when(smsDestinationService.getByGUID(eq(tenant), eq(savedDestination.getGuid())))
                .thenReturn(responseSingleDestination);

        getMockMvc().perform(get(format("/destinations/sms/{0}", savedDestination.getGuid())))
                .andExpect(
                        model().attribute("destination", equalTo(new SmsDestinationForm().fillFrom(savedDestination))))
                .andExpect(view().name("destinations/sms/show"));

        verify(smsDestinationService).getByGUID(eq(tenant), eq(savedDestination.getGuid()));
    }

    @Test
    public void shouldShowEditForm() throws Exception {
        responseSingleDestination = ServiceResponseBuilder.<SmsDestination> ok().withResult(savedDestination)
                .<SmsDestination> build();

        when(smsDestinationService.getByGUID(eq(tenant), eq(savedDestination.getGuid())))
                .thenReturn(responseSingleDestination);

        getMockMvc().perform(get(format("/destinations/sms/{0}/edit", savedDestination.getGuid())))
                .andExpect(
                        model().attribute("destination", equalTo(new SmsDestinationForm().fillFrom(savedDestination))))
                .andExpect(model().attribute("action", format("/destinations/sms/{0}", savedDestination.getGuid())))
                .andExpect(model().attribute("method", "put")).andExpect(view().name("destinations/sms/form"));

        verify(smsDestinationService).getByGUID(eq(tenant), eq(savedDestination.getGuid()));
    }

    @Test
    public void shouldBindErrorMessagesWhenEditFailsAndGoBackToEditForm() throws Exception {
        responseSingleDestination = ServiceResponseBuilder.<SmsDestination> error()
                .withMessage(CommonValidations.TENANT_NULL.getCode()).<SmsDestination> build();

        when(smsDestinationService.update(eq(tenant), eq(savedDestination.getGuid()), eq(destination)))
                .thenReturn(responseSingleDestination);

        getMockMvc().perform(put(format("/destinations/sms/{0}", savedDestination.getGuid())).params(destinationData))
                .andExpect(model().attribute("errors",
                        equalTo(Arrays.asList(new String[] { applicationContext
                                .getMessage(CommonValidations.TENANT_NULL.getCode(), null, Locale.ENGLISH) }))))
                .andExpect(model().attribute("destination", equalTo(destinationForm)))
                .andExpect(model().attribute("method", "put")).andExpect(view().name("destinations/sms/form"));

        verify(smsDestinationService).update(eq(tenant), eq(savedDestination.getGuid()), eq(destination));
    }

    @Test
    public void shouldRedirectToShowAfterEditSucceed() throws Exception {
        responseSingleDestination = ServiceResponseBuilder.<SmsDestination> ok().withResult(savedDestination)
                .<SmsDestination> build();

        when(smsDestinationService.update(eq(tenant), eq(savedDestination.getGuid()), eq(destination)))
                .thenReturn(responseSingleDestination);

        getMockMvc().perform(put(format("/destinations/sms/{0}", savedDestination.getGuid())).params(destinationData))
                .andExpect(flash().attribute("message",
                        applicationContext.getMessage(
                                SmsDestinationController.Messages.SMSDEST_REGISTERED_SUCCESSFULLY.getCode(), null,
                                Locale.ENGLISH)))
                .andExpect(redirectedUrl(format("/destinations/sms/{0}", savedDestination.getGuid())));

        verify(smsDestinationService).update(eq(tenant), eq(savedDestination.getGuid()), eq(destination));
    }
    
    @Test
    @WithMockUser(authorities={"LIST_SMS_DESTINATIONS"})
    public void shouldCheckSmsIsEnable() throws Exception {
        when(smsDestinationService.findAll(tenant)).thenReturn(ServiceResponseBuilder.<List<SmsDestination>> ok()
                .withResult(registeredDestinations).<List<SmsDestination>> build());

        getMockMvc().perform(get("/destinations/sms"))
                .andExpect(model().attribute("allDestinations", equalTo(registeredDestinations)))
                .andExpect(view().name("destinations/sms/index"));
    }

    @Configuration
    static class SmsDestinationTestContextConfig {
        @Bean
        public SmsDestinationService restDestinationService() {
            return mock(SmsDestinationService.class);
        }
    }
}