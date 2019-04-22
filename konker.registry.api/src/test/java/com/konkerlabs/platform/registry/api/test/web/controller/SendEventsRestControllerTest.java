package com.konkerlabs.platform.registry.api.test.web.controller;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.text.MessageFormat;
import java.time.Instant;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.RestTemplate;

import com.konkerlabs.platform.registry.api.config.PubServerInternalConfig;
import com.konkerlabs.platform.registry.api.config.WebMvcConfig;
import com.konkerlabs.platform.registry.api.test.config.MongoTestConfig;
import com.konkerlabs.platform.registry.api.test.config.WebTestConfiguration;
import com.konkerlabs.platform.registry.api.web.controller.SendEventsRestController;
import com.konkerlabs.platform.registry.api.web.wrapper.CrudResponseAdvice;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = SendEventsRestController.class)
@AutoConfigureMockMvc(secure = false)
@ContextConfiguration(classes = {
        WebTestConfiguration.class,
        MongoTestConfig.class,
        WebMvcConfig.class,
        CrudResponseAdvice.class,
        PubServerInternalConfig.class,
        SendEventsRestControllerTest.SendEventsRestControllerTestConfig.class
})
public class SendEventsRestControllerTest extends WebLayerTestContext {

    private static final String NONEXIST_APPLICATION_NAME = "AppLost";

    @Autowired
    private DeviceEventService deviceEventService;

    @Autowired
    private ApplicationService applicationService;
    
    @Autowired
	private RestTemplate restTemplate;

    @Autowired
    private Tenant tenant;
    private Application application;
    
    private final String jsonPayload = "{\"tem\":27}";

    @Before
    public void setUp() {
        application = Application.builder()
        		.name("YOicfcBMre")
        		.friendlyName("Smart Frig")
        		.description("Description of smartff")
        		.qualifier(tenant.getName())
        		.registrationDate(Instant.now())
        		.build();


    }

    @After
    public void tearDown() {
        Mockito.reset(deviceEventService);
        Mockito.reset(applicationService);
    }

    @Test
    public void shouldTryToSendWithWrongApplication() throws Exception {

        when(applicationService.getByApplicationName(tenant, NONEXIST_APPLICATION_NAME))
            .thenReturn(ServiceResponseBuilder.<Application>error().withMessage(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode()).build());

        getMockMvc().perform(MockMvcRequestBuilders.post("/" + NONEXIST_APPLICATION_NAME + "/sendEvents")
        		.content(jsonPayload)
                .contentType("application/json")
                .accept(MediaType.APPLICATION_JSON))
        
        .andDo(print())
        .andExpect(status().is4xxClientError())
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andExpect(jsonPath("$.code", is(HttpStatus.NOT_FOUND.value())))
        .andExpect(jsonPath("$.status", is("error")))
        .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
        .andExpect(jsonPath("$.messages[0]", is("Application does not exist")))
        .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldSendEvents() throws Exception {
    	HttpEntity<String> request = new HttpEntity<>(jsonPayload);
        when(applicationService.getByApplicationName(tenant, application.getName()))
        	.thenReturn(ServiceResponseBuilder.<Application>ok().withResult(application).build());
       
        when(restTemplate.postForObject(
        		MessageFormat.format("http://localhost:8085/registry-data-processor/{0}/{1}/pub", tenant.getDomainName(), application.getName()),
        		request, 
        		String.class))
			.thenReturn("{status: 200}");
        
        getMockMvc().perform(MockMvcRequestBuilders.post("/" + application.getName() + "/sendEvents")
        		.content(jsonPayload)
        		.contentType("application/json")
                .accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andExpect(jsonPath("$.code", is(HttpStatus.CREATED.value())))
        .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)));

    }
    
    static class SendEventsRestControllerTestConfig {
    	
    	@Bean
    	public RestTemplate restTemplate() {
    		return Mockito.mock(RestTemplate.class);
    	}
    }

}
