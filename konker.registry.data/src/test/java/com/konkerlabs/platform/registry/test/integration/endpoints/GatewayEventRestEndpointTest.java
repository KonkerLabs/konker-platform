package com.konkerlabs.platform.registry.test.integration.endpoints;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.spy;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.data.config.WebMvcConfig;
import com.konkerlabs.platform.registry.integration.endpoints.GatewayEventRestEndpoint;
import com.konkerlabs.platform.registry.integration.processors.DeviceEventProcessor;
import com.konkerlabs.platform.registry.test.data.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.data.base.SecurityTestConfiguration;
import com.konkerlabs.platform.registry.test.data.base.WebLayerTestContext;
import com.konkerlabs.platform.registry.test.data.base.WebTestConfiguration;
import com.konkerlabs.platform.utilities.parsers.json.JsonParsingService;
import com.konkerlabs.platform.utilities.parsers.json.JsonParsingService.JsonPathData;
import com.konkerlabs.platform.utilities.parsers.json.JsonParsingServiceImpl;

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

    private Device device;
    private String DEVICE_USER = "tug6g6essh4m";
    private String VALID_CHANNEL = "data";
    private String INVALID_CHANNEL_SIZE = "abcabcabcabcabcabcabcabcabcabcabc";
    private String INVALID_CHANNEL_CHAR = "dataç";
    private Set<String> tags =new HashSet<>(Arrays.asList("tag1", "tag2"));
    private String json;

    @Before
    public void setUp() throws Exception {
        deviceEventProcessor = mock(DeviceEventProcessor.class);
        gatewayEventRestEndpoint = new GatewayEventRestEndpoint(
                applicationContext,
                deviceEventProcessor,
                jsonParsingService,
                deviceRegisterService);
        
        device = Device.builder().deviceId("tug6g6essh4m")
                .active(true)
                .apiKey("e4399b2ed998")
                .guid("7d51c242-81db-11e6-a8c2-0746f010e945")
                .description("test")
                .tags(tags)
                .deviceId("device_id")
                .guid("67014de6-81db-11e6-a5bc-3f99b38315c6").build();
        
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
	}
         
    @Test
    public void shouldRefuseRequestFromKonkerPlataform() throws Exception {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication auth = new UsernamePasswordAuthenticationToken(device, null);
        context.setAuthentication(auth);

        when(jsonParsingService.isValid(json)).thenReturn(true);

		getMockMvc().perform(
                post("/gw/pub")
                	.flashAttr("principal", device)
                	.header("X-Konker-Version", "0.1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                	.andExpect(status().isForbidden())
                	.andExpect(content().string(org.hamcrest.Matchers.containsString("origin")));

    }
    
    @Test
    public void shouldPubToKonkerPlataform() throws Exception {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication auth = new UsernamePasswordAuthenticationToken(device, null);
        context.setAuthentication(auth);
        
        Map<String, JsonPathData> flatMap = new JsonParsingServiceImpl().toFlatMap(json);
        JSONArray jsonObject = new JSONArray(json);
        when(jsonParsingService.isValid(json)).thenReturn(true);

		getMockMvc().perform(
                post("/gw/pub")
                	.flashAttr("principal", device)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                	.andExpect(status().isForbidden())
                	.andExpect(content().string(org.hamcrest.Matchers.containsString("origin")));

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

    }
}
