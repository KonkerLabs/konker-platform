package com.konkerlabs.platform.registry.test.integration.endpoints;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Gateway;
import com.konkerlabs.platform.registry.business.model.Location;
import com.konkerlabs.platform.registry.business.model.OauthClientDetails;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.data.config.WebMvcConfig;
import com.konkerlabs.platform.registry.idm.services.OAuthClientDetailsService;
import com.konkerlabs.platform.registry.integration.endpoints.GatewayEventRestEndpoint;
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
        GatewayEventRestEndpointTest.DeviceEventRestEndpointTestContextConfig.class
})
public class GatewayEventRestEndpointTest extends WebLayerTestContext {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    public GatewayEventRestEndpoint gatewayEventRestEndpoint;

    @Autowired
    private DeviceEventProcessor deviceEventProcessor;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private JsonParsingService jsonParsingService;

    @Autowired
    private DeviceRegisterService deviceRegisterService;
    
    @Autowired
    private OAuthClientDetailsService oAuthClientDetailsService;

    private Gateway gateway;
    private String json;

    @Before
    public void setUp() throws Exception {
        deviceEventProcessor = mock(DeviceEventProcessor.class);
        gatewayEventRestEndpoint = new GatewayEventRestEndpoint(
                applicationContext,
                deviceEventProcessor,
                jsonParsingService,
                deviceRegisterService,
                oAuthClientDetailsService);
        
        gateway = Gateway.builder()
        		.active(true)
        		.application(Application.builder().name("default").build())
        		.description("GW smart")
        		.guid("7d51c242-81db-11e6-a8c2-0746f010e945")
        		.id("gateway1")
        		.location(Location.builder().defaultLocation(true).id("BR").build())
        		.name("Gateway 1")
        		.tenant(Tenant.builder().id("commonTenant").domainName("common").build())
        		.build();
        
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
			"}"+
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
			"]";
    }

	@After
	public void tearDown() {
		Mockito.reset(jsonParsingService);
		Mockito.reset(deviceRegisterService);
		Mockito.reset(oAuthClientDetailsService);}
         
    @Test
    public void shouldRefuseRequestFromKonkerPlataform() throws Exception {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication auth = new TestingAuthenticationToken("gateway://i3k9jfe5/1c6e7df7-fe10-4c53-acae-913e0ceec883", null);
        context.setAuthentication(auth);

        when(oAuthClientDetailsService.loadClientByIdAsRoot("gateway://i3k9jfe5/1c6e7df7-fe10-4c53-acae-913e0ceec883"))
        	.thenReturn(ServiceResponseBuilder.<OauthClientDetails>ok()
        			.withResult(OauthClientDetails.builder().parentGateway(gateway).build()).build());
        when(jsonParsingService.isValid(json)).thenReturn(true);

		getMockMvc().perform(
                post("/gateway/pub")
                	.flashAttr("principal", gateway)
                	.header("X-Konker-Version", "0.1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                	.andExpect(status().isForbidden())
                	.andExpect(content().string(org.hamcrest.Matchers.containsString("origin")));

    }
    
    @Test
    public void shouldRaiseExceptionInvalidJsonPub() throws Exception {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication auth = new TestingAuthenticationToken("gateway://i3k9jfe5/1c6e7df7-fe10-4c53-acae-913e0ceec883", null);
        context.setAuthentication(auth);

        when(oAuthClientDetailsService.loadClientByIdAsRoot("gateway://i3k9jfe5/1c6e7df7-fe10-4c53-acae-913e0ceec883"))
        	.thenReturn(ServiceResponseBuilder.<OauthClientDetails>ok()
        			.withResult(OauthClientDetails.builder().parentGateway(gateway).build()).build());
        when(jsonParsingService.isValid("[{'a': 10}")).thenReturn(false);

		getMockMvc().perform(
                post("/gateway/pub")
                	.flashAttr("principal", gateway)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("[{'a': 10}"))
                	.andExpect(status().isBadRequest())
                	.andExpect(content().string(org.hamcrest.Matchers.containsString("{\"code\":\"integration.rest.invalid.body\",\"message\":\"Event content is in invalid format. Expected to be a valid JSON string\"}")));

    }
    
    @Test
    public void shouldPubToKonkerPlataform() throws Exception {
    	SecurityContext context = SecurityContextHolder.getContext();
        Authentication auth = new TestingAuthenticationToken("gateway://i3k9jfe5/1c6e7df7-fe10-4c53-acae-913e0ceec883", null);
        context.setAuthentication(auth);

        when(oAuthClientDetailsService.loadClientByIdAsRoot("gateway://i3k9jfe5/1c6e7df7-fe10-4c53-acae-913e0ceec883"))
        	.thenReturn(ServiceResponseBuilder.<OauthClientDetails>ok()
        			.withResult(OauthClientDetails.builder().parentGateway(gateway).build()).build());
        when(jsonParsingService.isValid(json)).thenReturn(true);

		getMockMvc().perform(
                post("/gateway/pub")
                	.flashAttr("principal", gateway)
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
        public OAuthClientDetailsService oAuthClientDetailsService() {
            return Mockito.mock(OAuthClientDetailsService.class);
        }

    }
}
