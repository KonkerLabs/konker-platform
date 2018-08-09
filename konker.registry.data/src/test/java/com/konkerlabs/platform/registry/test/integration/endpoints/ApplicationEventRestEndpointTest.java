package com.konkerlabs.platform.registry.test.integration.endpoints;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.text.MessageFormat;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.business.services.api.TenantService;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService.Validations;
import com.konkerlabs.platform.registry.data.config.WebMvcConfig;
import com.konkerlabs.platform.registry.integration.endpoints.ApplicationEventRestEndpoint;
import com.konkerlabs.platform.registry.integration.endpoints.ApplicationEventRestEndpoint.Messages;
import com.konkerlabs.platform.registry.integration.processors.DeviceEventProcessor;
import com.konkerlabs.platform.registry.test.data.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.data.base.SecurityTestConfiguration;
import com.konkerlabs.platform.registry.test.data.base.WebLayerTestContext;
import com.konkerlabs.platform.registry.test.data.base.WebTestConfiguration;
import com.konkerlabs.platform.utilities.parsers.json.JsonParsingService;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {
        BusinessTestConfiguration.class,
        WebMvcConfig.class,
        WebTestConfiguration.class,
        SecurityTestConfiguration.class,
        ApplicationEventRestEndpointTest.DeviceEventRestEndpointTestContextConfig.class
})
@ActiveProfiles("dataInternal")
public class ApplicationEventRestEndpointTest extends WebLayerTestContext {


	@Rule
    public ExpectedException thrown = ExpectedException.none();

    public ApplicationEventRestEndpoint applicationEventRestEndpoint;

    @Autowired
    private DeviceEventProcessor deviceEventProcessor;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private JsonParsingService jsonParsingService;
    
    @Autowired
    private TenantService tenantService;
    
    @Autowired
    private ApplicationService applicationService;

    private String json;
    private final String tenantDomain = "anyTenant";
    private final String applicationName = "smartff";
    private final Tenant tenant = Tenant.builder().domainName(tenantDomain).build();
    private final Application application = Application.builder().name(applicationName).build();

    @Before
    public void setUp() {
        deviceEventProcessor = mock(DeviceEventProcessor.class);
        applicationEventRestEndpoint = new ApplicationEventRestEndpoint(
                applicationContext,
                deviceEventProcessor,
                jsonParsingService,
                tenantService,
                applicationService);
        
        
        json = "[ "+
        	"{ "+
			" \"deviceId\": \"CurrentSensor\", "+
			" \"channel\": \"in\", "+
			" \"payload\": { "+
			" 	\"_lon\": -46.6910183, "+ 
			"	\"_lat\": -23.5746571,  "+
			"	\"_hdop\": 10,  "+
			"	\"_elev\": 3.66,  "+
			"	\"_ts\": \"1510847419000\", "+ 
			"	\"volts\": 12  "+
			"	} "+
                '}' +
			", { "+
			" \"deviceId\": \"TempSensor\", "+
			" \"channel\": \"temp\", "+
			" \"payload\": { "+
			"	\"_lon\": -46.6910183, "+ 
			"	\"_lat\": -23.5746571,  "+
			"	\"_hdop\": 10,  "+
			"	\"_elev\": 3.66,  "+
			"	\"_ts\": \"1510847419000\", "+ 
			"	\"temperature\": 27  "+
			"	} "+
			"} "+
                ']';
    }

	@After
	public void tearDown() {
		Mockito.reset(jsonParsingService);
		Mockito.reset(tenantService);
		Mockito.reset(applicationService);
	}
         
    @Test
    public void shouldRefuseRequestFromKonkerPlatform() throws Exception {
    	when(tenantService.findByDomainName(tenantDomain))
    		.thenReturn(ServiceResponseBuilder.<Tenant>ok().withResult(tenant).build());
    	
    	when(applicationService.getByApplicationName(tenant, applicationName))
    		.thenReturn(ServiceResponseBuilder.<Application>ok().withResult(application).build());
    	
        when(jsonParsingService.isValid(json)).thenReturn(true);

		getMockMvc().perform(
                post(MessageFormat.format("/{0}/{1}/pub", tenantDomain, applicationName))
                	.header("X-Konker-Version", "0.1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                	.andExpect(status().isForbidden())
                	.andExpect(content().string(org.hamcrest.Matchers.containsString("origin")));

    }
    
    @Test
    public void shouldRaiseExceptionInvalidJsonPub() throws Exception {
    	when(tenantService.findByDomainName(tenantDomain))
		.thenReturn(ServiceResponseBuilder.<Tenant>ok().withResult(tenant).build());
	
		when(applicationService.getByApplicationName(tenant, applicationName))
			.thenReturn(ServiceResponseBuilder.<Application>ok().withResult(application).build());
		
        when(jsonParsingService.isValid("[{'a': 10}")).thenReturn(false);

		getMockMvc().perform(
                post(MessageFormat.format("/{0}/{1}/pub", tenantDomain, applicationName))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("[{'a': 10}"))
                	.andExpect(status().isBadRequest())
                	.andExpect(content().string(org.hamcrest.Matchers.containsString("{\"code\":\"integration.rest.invalid.body\",\"message\":\"Event content is in invalid format. Expected to be a valid JSON string\"}")));

    }
    
    @Test
    public void shouldReturnExceptionInvalidTenant() throws Exception {
    	when(tenantService.findByDomainName(tenantDomain))
			.thenReturn(ServiceResponseBuilder.<Tenant>error().withMessage(Messages.INVALID_TENANT.getCode()).build());
    	
    	getMockMvc().perform(
                post(MessageFormat.format("/{0}/{1}/pub", tenantDomain, applicationName))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                	.andExpect(status().is4xxClientError())
                	.andExpect(content().string(org.hamcrest.Matchers.containsString("{\"code\":\"integration.rest.invalid_tenant\",\"message\":\"Invalid tenant\"}")));
    }
    
    @Test
    public void shouldReturnExceptionInvalidApplication() throws Exception {
    	when(tenantService.findByDomainName(tenantDomain))
			.thenReturn(ServiceResponseBuilder.<Tenant>ok().withResult(tenant).build());
    	
    	when(applicationService.getByApplicationName(tenant, applicationName))
			.thenReturn(ServiceResponseBuilder.<Application>error().withMessage(Validations.APPLICATION_DOES_NOT_EXIST.getCode()).build());
    	
    	getMockMvc().perform(
                post(MessageFormat.format("/{0}/{1}/pub", tenantDomain, applicationName))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                	.andExpect(status().is4xxClientError())
                	.andExpect(content().string(org.hamcrest.Matchers.containsString("{\"code\":\"service.application.does.not.exist\",\"message\":\"Application does not exist\"}")));
    }
    
    @Test
    public void shouldPubToKonkerPlatform() throws Exception {
    	when(tenantService.findByDomainName(tenantDomain))
		.thenReturn(ServiceResponseBuilder.<Tenant>ok().withResult(tenant).build());
	
		when(applicationService.getByApplicationName(tenant, applicationName))
			.thenReturn(ServiceResponseBuilder.<Application>ok().withResult(application).build());
		
        when(jsonParsingService.isValid(json)).thenReturn(true);

		getMockMvc().perform(
                post(MessageFormat.format("/{0}/{1}/pub", tenantDomain, applicationName))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                	.andExpect(status().isOk())
                	.andExpect(content().string(org.hamcrest.Matchers.containsString("{\"code\":\"200\",\"message\":\"OK\"}")));

    }

    @Configuration
    static class DeviceEventRestEndpointTestContextConfig {
        @Bean
        public DeviceRegisterService deviceRegisterService() {
            return Mockito.mock(DeviceRegisterService.class);
        }

        @Bean
        public DeviceEventProcessor deviceEventProcessor() {
            return Mockito.mock(DeviceEventProcessor.class);
        }

        @Bean
        public JsonParsingService jsonParsingService() {
            return Mockito.mock(JsonParsingService.class);
        }
        
        @Bean
        public TenantService tenantService() {
        	return Mockito.mock(TenantService.class);
        }
        
        @Bean
        public ApplicationService applicationService() {
        	return Mockito.mock(ApplicationService.class);
        }
    }
}
