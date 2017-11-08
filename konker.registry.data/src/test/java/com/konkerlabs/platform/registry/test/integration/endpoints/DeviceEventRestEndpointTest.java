package com.konkerlabs.platform.registry.test.integration.endpoints;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;

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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.DeviceModel;
import com.konkerlabs.platform.registry.business.model.Location;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.DeviceConfigSetupService;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.business.services.api.DeviceConfigSetupService.Validations;
import com.konkerlabs.platform.registry.data.config.WebMvcConfig;
import com.konkerlabs.platform.registry.data.services.JedisTaskService;
import com.konkerlabs.platform.registry.integration.endpoints.DeviceEventRestEndpoint;
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
        DeviceEventRestEndpointTest.DeviceEventRestEndpointTestContextConfig.class
})
public class DeviceEventRestEndpointTest extends WebLayerTestContext {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    public DeviceEventRestEndpoint deviceEventRestEndpoint;

    @Autowired
    private DeviceEventProcessor deviceEventProcessor;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private JsonParsingService jsonParsingService;

    @Autowired
    private DeviceEventService deviceEventService;

    @Autowired
    private DeviceRegisterService deviceRegisterService;
    
    @Autowired
    private DeviceConfigSetupService deviceConfigSetupService;

    @Autowired
    private Executor executor;

    @Autowired
    private JedisTaskService jedisTaskService;

    private String DEVICE_USER = "tug6g6essh4m";
    private String VALID_CHANNEL = "data";
    private String INVALID_CHANNEL_SIZE = "abcabcabcabcabcabcabcabcabcabcabc";
    private String INVALID_CHANNEL_CHAR = "dataç";
    private Long OFFSET = 1475765814662l;
    private Long waitTime = 30000l;
    private Set<String> tags =new HashSet<>(Arrays.asList("tag1", "tag2"));

    @Before
    public void setUp() throws Exception {
        deviceEventProcessor = mock(DeviceEventProcessor.class);
        deviceEventRestEndpoint = new DeviceEventRestEndpoint(
                applicationContext,
                deviceEventProcessor,
                jsonParsingService,
                deviceEventService,
                deviceRegisterService,
                executor,
                jedisTaskService,
                deviceConfigSetupService);
    }

	@After
	public void tearDown() {
		Mockito.reset(jsonParsingService);
		Mockito.reset(deviceEventService);
		Mockito.reset(deviceRegisterService);
		Mockito.reset(jedisTaskService);
		Mockito.reset(deviceConfigSetupService);
	}

    @Test
    public void shouldReturnErrorOnSubscriptionWithInvalidChannel() throws Exception {
        Device device = Device.builder().deviceId("tug6g6essh4m")
                .active(true)
                .apiKey("e4399b2ed998")
                .guid("7d51c242-81db-11e6-a8c2-0746f010e945")
                .description("test")
                .tags(tags)
                .deviceId("device_id")
                .guid("67014de6-81db-11e6-a5bc-3f99b38315c6").build();

        SecurityContext context = SecurityContextHolder.getContext();
        Authentication auth = new UsernamePasswordAuthenticationToken(device, null);
        context.setAuthentication(auth);

        getMockMvc().perform(
                get("/sub/"+ DEVICE_USER +"/"+ INVALID_CHANNEL_CHAR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("offset", String.valueOf(OFFSET))
                        .param("waitTime", String.valueOf(waitTime)))
                .andExpect(status().isBadRequest());


        getMockMvc().perform(
                get("/sub/"+ DEVICE_USER +"/"+ INVALID_CHANNEL_SIZE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("offset", String.valueOf(OFFSET))
                        .param("waitTime", String.valueOf(waitTime))
                        .flashAttr("principal", device))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldRefuseRequestFromKonkerPlataform() throws Exception {
        Device device = Device.builder().deviceId("tug6g6essh4m")
                .active(true)
                .apiKey("e4399b2ed998")
                .guid("7d51c242-81db-11e6-a8c2-0746f010e945")
                .description("test")
                .tags(tags)
                .deviceId("device_id")
                .guid("67014de6-81db-11e6-a5bc-3f99b38315c6").build();

        String json = "{\"temperature\": 18, \"unit\": \"celsius\"}";

        SecurityContext context = SecurityContextHolder.getContext();
        Authentication auth = new UsernamePasswordAuthenticationToken(device, null);
        context.setAuthentication(auth);

        when(jsonParsingService.isValid(json)).thenReturn(true);

		getMockMvc().perform(
                post("/pub/"+ device.getApiKey() +"/"+ VALID_CHANNEL)
                	.flashAttr("principal", device)
                	.header("X-Konker-Version", "0.1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                	.andExpect(status().isForbidden())
                	.andExpect(content().string(org.hamcrest.Matchers.containsString("origin")));

    }
    
    @Test
    public void shouldReturnBadRequestInvalidResource() throws Exception {
        Device device = Device.builder().deviceId("tug6g6essh4m")
                .active(true)
                .apiKey("e4399b2ed998")
                .guid("7d51c242-81db-11e6-a8c2-0746f010e945")
                .description("test")
                .tags(tags)
                .deviceId("device_id")
                .guid("67014de6-81db-11e6-a5bc-3f99b38315c6").build();

        SecurityContext context = SecurityContextHolder.getContext();
        Authentication auth = new UsernamePasswordAuthenticationToken(device, null);
        context.setAuthentication(auth);

        getMockMvc().perform(
                get("/cfg/"+ DEVICE_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("offset", String.valueOf(OFFSET))
                        .param("waitTime", String.valueOf(waitTime)))
                .andExpect(status().isBadRequest());
   
    }
    
    @Test
    public void shouldReturnBadRequestDeviceNotFound() throws Exception {
        Device device = Device.builder().deviceId("tug6g6essh4m")
                .active(true)
                .apiKey("e4399b2ed998")
                .guid("7d51c242-81db-11e6-a8c2-0746f010e945")
                .description("test")
                .tags(tags)
                .deviceId("device_id")
                .guid("67014de6-81db-11e6-a5bc-3f99b38315c6").build();

        SecurityContext context = SecurityContextHolder.getContext();
        Authentication auth = new UsernamePasswordAuthenticationToken(device, null);
        context.setAuthentication(auth);

        getMockMvc().perform(
                get("/cfg/"+ device.getApiKey())
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("offset", String.valueOf(OFFSET))
                        .param("waitTime", String.valueOf(waitTime)))
                .andExpect(status().isBadRequest());
   
    }
    
    @Test
    public void shouldReturnDeviceConfigNotFound() throws Exception {
        Device device = Device.builder().deviceId("tug6g6essh4m")
                .active(true)
                .apiKey("e4399b2ed998")
                .guid("7d51c242-81db-11e6-a8c2-0746f010e945")
                .description("test")
                .tags(tags)
                .deviceId("device_id")
                .guid("67014de6-81db-11e6-a5bc-3f99b38315c6")
                .tenant(Tenant.builder().domainName("konker").name("Konker").build())
                .application(Application.builder().name("SmartAC").build())
                .deviceModel(DeviceModel.builder().name("SensorTemp").build())
                .location(Location.builder().name("sp_br").build())
                .build();
        
        when(deviceRegisterService.findByApiKey(device.getApiKey()))
        	.thenReturn(device);
        when(deviceConfigSetupService.findByModelAndLocation(device.getTenant(), device.getApplication(), device.getDeviceModel(), device.getLocation()))
        	.thenReturn(ServiceResponseBuilder.<String> error()
        			.withMessage(Validations.DEVICE_CONFIG_NOT_FOUND.getCode()).build());

        SecurityContext context = SecurityContextHolder.getContext();
        Authentication auth = new UsernamePasswordAuthenticationToken(device, null);
        context.setAuthentication(auth);

        getMockMvc().perform(
                get("/cfg/"+ device.getApiKey())
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("offset", String.valueOf(OFFSET))
                        .param("waitTime", String.valueOf(waitTime)))
                .andExpect(status().isNotFound());
   
    }
    
    @Test
    public void shouldReturnConfig() throws Exception {
        Device device = Device.builder().deviceId("tug6g6essh4m")
                .active(true)
                .apiKey("e4399b2ed998")
                .guid("7d51c242-81db-11e6-a8c2-0746f010e945")
                .description("test")
                .tags(tags)
                .deviceId("device_id")
                .guid("67014de6-81db-11e6-a5bc-3f99b38315c6")
                .tenant(Tenant.builder().domainName("konker").name("Konker").build())
                .application(Application.builder().name("SmartAC").build())
                .deviceModel(DeviceModel.builder().name("SensorTemp").build())
                .location(Location.builder().name("sp_br").build())
                .build();
        
        when(deviceRegisterService.findByApiKey(device.getApiKey()))
        	.thenReturn(device);
        when(deviceConfigSetupService.findByModelAndLocation(device.getTenant(), device.getApplication(), device.getDeviceModel(), device.getLocation()))
        	.thenReturn(ServiceResponseBuilder.<String> ok()
        			.withResult("{'minimalInterval': 10, "
        					+ "'unit': 'celsius' }").build());

        SecurityContext context = SecurityContextHolder.getContext();
        Authentication auth = new UsernamePasswordAuthenticationToken(device, null);
        context.setAuthentication(auth);

        getMockMvc().perform(
                get("/cfg/"+ device.getApiKey())
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("offset", String.valueOf(OFFSET))
                        .param("waitTime", String.valueOf(waitTime)))
                .andExpect(status().isOk());
   
    }

    @Configuration
    static class DeviceEventRestEndpointTestContextConfig {
        @Bean
        public DeviceRegisterService deviceRegisterService() {
            return Mockito.mock(DeviceRegisterService.class);
        }

        @Bean
        public DeviceEventService deviceEventService() {
            return Mockito.mock(DeviceEventService.class);
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
        public JedisTaskService jedisTaskService() {
            return Mockito.mock(JedisTaskService.class);
        }

        @Bean
        @SuppressWarnings("unchecked")
        public RedisTemplate<String, String> redisTemplate() {
            return Mockito.mock(RedisTemplate.class);
        }

        @Bean
        public Executor executor() {
            return Mockito.mock(Executor.class);
        }
        
        @Bean
        public DeviceConfigSetupService deviceConfigSetupService() {
        	return Mockito.mock(DeviceConfigSetupService.class);
        }
    }
}
