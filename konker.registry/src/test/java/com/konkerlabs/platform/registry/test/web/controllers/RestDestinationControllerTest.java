package com.konkerlabs.platform.registry.test.web.controllers;

import com.konkerlabs.platform.registry.business.model.RestDestination;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.RestDestinationService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.config.WebMvcConfig;
import com.konkerlabs.platform.registry.test.base.SecurityTestConfiguration;
import com.konkerlabs.platform.registry.test.base.WebLayerTestContext;
import com.konkerlabs.platform.registry.test.base.WebTestConfiguration;
import com.konkerlabs.platform.registry.web.forms.RestDestinationForm;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.text.MessageFormat.format;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {
        WebMvcConfig.class,
        WebTestConfiguration.class,
        SecurityTestConfiguration.class,
        RestDestinationControllerTest.RestDestinationTestContextConfig.class
})
public class RestDestinationControllerTest extends WebLayerTestContext {

    @Autowired
    private RestDestinationService restDestinationService;
    @Autowired
    private Tenant tenant;

    private List<RestDestination> registeredDestinations;
    private RestDestination destination;
    private LinkedMultiValueMap<String, String> destinationData;
    private RestDestinationForm destinationForm;

    ServiceResponse<RestDestination> response;
    private RestDestination savedDestination;

    @Before
    public void setUp() {
        destination = RestDestination.builder()
            .name("Destination name")
            .serviceURI("https://www.host.com:443/path?query=1")
            .serviceUsername("username")
            .servicePassword("password").active(true).build();

        savedDestination = RestDestination.builder()
                .guid("deviceId")
                .name(destination.getName())
                .serviceURI(destination.getServiceURI())
                .serviceUsername(destination.getServiceUsername())
                .servicePassword(destination.getServicePassword())
                .active(destination.isActive())
                .build();

        registeredDestinations = new ArrayList<>();
        registeredDestinations.add(destination);

        destinationData = new LinkedMultiValueMap<>();
        destinationData.add("name", destination.getName());
        destinationData.add("serviceURI", destination.getServiceURI());
        destinationData.add("serviceUsername", destination.getServiceUsername());
        destinationData.add("servicePassword", destination.getServicePassword());
        destinationData.add("active", Boolean.valueOf(destination.isActive()).toString());

        destinationForm = new RestDestinationForm();
        destinationForm.setName(destination.getName());
        destinationForm.setServiceURI(destination.getServiceURI());
        destinationForm.setServiceUsername(destination.getServiceUsername());
        destinationForm.setServicePassword(destination.getServicePassword());
        destinationForm.setActive(destination.isActive());
    }

    @After
    public void tearDown() {
        Mockito.reset(restDestinationService);
    }

    @Test
    public void shouldListAllRegisteredDestinations() throws Exception {
        when(
            restDestinationService.findAll(tenant)
        ).thenReturn(
            ServiceResponse.<List<RestDestination>>builder()
                    .status(ServiceResponse.Status.OK)
                    .result(registeredDestinations).<List<RestDestination>>build()
        );

        getMockMvc().perform(get("/destinations/rest"))
                .andExpect(model().attribute("allDestinations", equalTo(registeredDestinations)))
                .andExpect(view().name("destinations/rest/index"));
    }

    @Test
    public void shouldShowRegistrationForm() throws Exception {
        getMockMvc().perform(get("/destinations/rest/new"))
                .andExpect(view().name("destinations/rest/form"))
                .andExpect(model().attribute("destination",any(RestDestinationForm.class)))
                .andExpect(model().attribute("action","/destinations/rest/save"));
    }

    @Test
    public void shouldBindErrorMessagesWhenRegistrationFailsAndGoBackToRegistrationForm() throws Exception {
        response = ServiceResponse.<RestDestination>builder()
                .responseMessages(Arrays.asList(new String[]{"Some error"}))
                .status(ServiceResponse.Status.ERROR).<RestDestination>build();

        when(restDestinationService.register(eq(tenant),eq(destination))).thenReturn(response);

        getMockMvc().perform(post("/destinations/rest/save").params(destinationData))
                .andExpect(model().attribute("errors", equalTo(response.getResponseMessages())))
                .andExpect(model().attribute("destination", equalTo(destinationForm)))
                .andExpect(view().name("destinations/rest/form"));

        verify(restDestinationService).register(eq(tenant),eq(destination));
    }

    @Test
    public void shouldRedirectToShowAfterRegistrationSucceed() throws Exception {
        response = ServiceResponse.<RestDestination>builder()
                .status(ServiceResponse.Status.OK)
                .result(savedDestination)
                .<RestDestination>build();

        when(restDestinationService.register(eq(tenant),eq(destination))).thenReturn(response);

        getMockMvc().perform(post("/destinations/rest/save").params(destinationData))
                .andExpect(flash().attribute("message", "Destination saved successfully"))
                .andExpect(redirectedUrl(format("/destinations/rest/{0}", savedDestination.getGuid())));

        verify(restDestinationService).register(eq(tenant),eq(destination));
    }

    @Test
    public void shouldShowDestinationDetails() throws Exception {
        response = ServiceResponse.<RestDestination>builder()
                .status(ServiceResponse.Status.OK)
                .result(savedDestination)
                .<RestDestination>build();

        when(restDestinationService.getByGUID(eq(tenant),eq(savedDestination.getGuid()))).thenReturn(response);

        getMockMvc().perform(get(format("/destinations/rest/{0}", savedDestination.getGuid())))
                .andExpect(model().attribute("destination", equalTo(new RestDestinationForm().fillFrom(savedDestination))))
                .andExpect(view().name("destinations/rest/show"));

        verify(restDestinationService).getByGUID(eq(tenant),eq(savedDestination.getGuid()));
    }

    @Test
    public void shouldShowEditForm() throws Exception {
        response = ServiceResponse.<RestDestination>builder()
                .status(ServiceResponse.Status.OK)
                .result(savedDestination)
                .<RestDestination>build();

        when(restDestinationService.getByGUID(eq(tenant),eq(savedDestination.getGuid()))).thenReturn(response);

        getMockMvc().perform(get(format("/destinations/rest/{0}/edit", savedDestination.getGuid())))
                .andExpect(model().attribute("destination", equalTo(new RestDestinationForm().fillFrom(savedDestination))))
                .andExpect(model().attribute("action", format("/destinations/rest/{0}",savedDestination.getGuid())))
                .andExpect(model().attribute("method", "put"))
                .andExpect(view().name("destinations/rest/form"));

        verify(restDestinationService).getByGUID(eq(tenant),eq(savedDestination.getGuid()));
    }

    @Test
    public void shouldBindErrorMessagesWhenEditFailsAndGoBackToEditForm() throws Exception {
        response = ServiceResponse.<RestDestination>builder()
                .responseMessages(Arrays.asList(new String[]{"Some error"}))
                .status(ServiceResponse.Status.ERROR).<RestDestination>build();

        when(restDestinationService.update(eq(tenant),eq(savedDestination.getGuid()),eq(destination))).thenReturn(response);

        getMockMvc().perform(put(format("/destinations/rest/{0}",savedDestination.getGuid())).params(destinationData))
                .andExpect(model().attribute("errors", equalTo(response.getResponseMessages())))
                .andExpect(model().attribute("destination", equalTo(destinationForm)))
                .andExpect(view().name("destinations/rest/form"));

        verify(restDestinationService).update(eq(tenant),eq(savedDestination.getGuid()),eq(destination));
    }

    @Test
    public void shouldRedirectToShowAfterEditSucceed() throws Exception {
        response = ServiceResponse.<RestDestination>builder()
                .status(ServiceResponse.Status.OK)
                .result(savedDestination)
                .<RestDestination>build();

        when(restDestinationService.update(eq(tenant),eq(savedDestination.getGuid()),eq(destination))).thenReturn(response);

        getMockMvc().perform(put(format("/destinations/rest/{0}",savedDestination.getGuid())).params(destinationData))
                .andExpect(flash().attribute("message", "Destination saved successfully"))
                .andExpect(redirectedUrl(format("/destinations/rest/{0}", savedDestination.getGuid())));

        verify(restDestinationService).update(eq(tenant),eq(savedDestination.getGuid()),eq(destination));
    }

    @Configuration
    static class RestDestinationTestContextConfig {
        @Bean
        public RestDestinationService restDestinationService() {
            return mock(RestDestinationService.class);
        }
    }
}