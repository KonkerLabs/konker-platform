package com.konkerlabs.platform.registry.test.integration.endpoints;

import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.config.RabbitMQConfig;
import com.konkerlabs.platform.registry.data.config.WebMvcConfig;
import com.konkerlabs.platform.registry.data.core.services.JedisTaskService;
import com.konkerlabs.platform.registry.idm.services.OAuthClientDetailsService;
import com.konkerlabs.platform.registry.integration.endpoints.GatewayEventRestEndpoint;
import com.konkerlabs.platform.registry.data.core.integration.processors.DeviceEventProcessor;
import com.konkerlabs.platform.registry.integration.processors.GatewayEventProcessor;
import com.konkerlabs.platform.registry.test.data.base.BusinessDataTestConfiguration;
import com.konkerlabs.platform.registry.test.data.base.SecurityTestConfiguration;
import com.konkerlabs.platform.registry.test.data.base.WebLayerTestContext;
import com.konkerlabs.platform.registry.test.data.base.WebTestConfiguration;
import com.konkerlabs.platform.utilities.parsers.json.JsonParsingService;
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
import org.springframework.http.*;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.RestTemplate;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

import static java.text.MessageFormat.format;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {
        BusinessDataTestConfiguration.class,
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
    private GatewayEventProcessor gatewayEventProcessor;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private JsonParsingService jsonParsingService;

    @Autowired
    private DeviceRegisterService deviceRegisterService;

    @Autowired
    private OAuthClientDetailsService oAuthClientDetailsService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RabbitMQConfig rabbitMQConfig;

    @Autowired
    private DeviceEventService deviceEventService;

    @Autowired
    private JedisTaskService jedisTaskService;

    @Autowired
    private Executor executor;

    private Gateway gateway;
    private Device device;
    private Device deviceNotChild;
    private Event event;
    private List<Event> events;
    private String json;
    private static final String DEVICE_ID = "device001";
    private static final String CHANNEL = "data";
    private final String INVALID_CHANNEL_SIZE = "abcabcabcabcabcabcabcabcabcabcabcabcabc";
    private final String INVALID_CHANNEL_CHAR = "dataç";

    @Before
    public void setUp() {
        gatewayEventProcessor = mock(GatewayEventProcessor.class);
        gatewayEventRestEndpoint = new GatewayEventRestEndpoint(
                applicationContext,
                gatewayEventProcessor,
                jsonParsingService,
                deviceRegisterService,
                oAuthClientDetailsService,
                restTemplate,
                rabbitMQConfig,
                deviceEventService,
                jedisTaskService,
                executor);

        gateway = Gateway.builder()
        		.active(true)
        		.application(Application.builder().name("default").build())
        		.description("GW smart")
        		.guid("7d51c242-81db-11e6-a8c2-0746f010e945")
        		.id("gateway1")
        		.location(Location.builder().defaultLocation(true).id("BR").name("Brasil").build())
        		.name("Gateway 1")
        		.tenant(Tenant.builder().id("commonTenant").domainName("common").build())
        		.build();

        device = Device.builder()
                .tenant(Tenant.builder().id("commonTenant").domainName("common").build())
                .application(Application.builder().name("default").build())
                .location(Location.builder().defaultLocation(true).id("BR").name("Brasil").build())
                .active(true)
                .guid("67014de6-81db-11e6-a5bc-3f99b38315c6")
                .description("test")
                .deviceId(DEVICE_ID).build();

        deviceNotChild = Device.builder()
                .tenant(Tenant.builder().id("commonTenant").domainName("common").build())
                .application(Application.builder().name("default").build())
                .location(Location.builder().defaultLocation(true).id("default").build())
                .active(true)
                .guid("7d51c242-81db-11e6-a8c2-0746f010e945")
                .description("test not child")
                .deviceId("tug6g6essh6n").build();

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

        event = Event.builder()
                .outgoing(Event.EventActor.builder()
                        .tenantDomain("common")
                        .applicationName("default")
                        .locationGuid("67014de6-81db-11e6-a5bc-3f99b38351c9")
                        .deviceGuid("67014de6-81db-11e6-a5bc-3f99b38315c6")
                        .channel("data")
                        .deviceId(DEVICE_ID)
                        .build())
                .creationTimestamp(Instant.now())
                .ingestedTimestamp(Instant.now())
                .payload("{\"temp\":29}")
                .build();
        events = Collections.singletonList(event);
    }

	@After
	public void tearDown() {
		Mockito.reset(jsonParsingService);
		Mockito.reset(deviceRegisterService);
		Mockito.reset(oAuthClientDetailsService);}

    @Test
    public void shouldRefuseRequestFromKonkerPlatform() throws Exception {
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
    public void shouldPubToKonkerPlatform() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        String encodedCredentials = Base64Utils
                .encodeToString(format("{0}:{1}", rabbitMQConfig.getUsername(), rabbitMQConfig.getPassword()).getBytes());
        headers.add("Authorization", format("Basic {0}", encodedCredentials));
        HttpEntity<String> entity = new HttpEntity(
                null,
                headers
        );

    	SecurityContext context = SecurityContextHolder.getContext();
        Authentication auth = new TestingAuthenticationToken("gateway://i3k9jfe5/1c6e7df7-fe10-4c53-acae-913e0ceec883", null);
        context.setAuthentication(auth);

        when(oAuthClientDetailsService.loadClientByIdAsRoot("gateway://i3k9jfe5/1c6e7df7-fe10-4c53-acae-913e0ceec883"))
        	.thenReturn(ServiceResponseBuilder.<OauthClientDetails>ok()
        			.withResult(OauthClientDetails.builder().parentGateway(gateway).build()).build());
        when(jsonParsingService.isValid(json)).thenReturn(true);
        when(restTemplate.exchange(
                format("http://{0}:{1}/{2}", rabbitMQConfig.getHostname(), rabbitMQConfig.getApiPort(), "api/healthchecks/node"),
                HttpMethod.GET,
                entity,
                String.class)).thenReturn(new ResponseEntity<String>(HttpStatus.OK));


		getMockMvc().perform(
                post("/gateway/pub")
                	.flashAttr("principal", gateway)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                	.andExpect(status().isOk())
                	.andExpect(content().string(org.hamcrest.Matchers.containsString("{\"code\":\"200\",\"message\":\"OK\"}")));

    }

    @Test
    public void shouldThrowDeviceNotExistOnSubToKonkerPlatformGateway() throws Exception {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication auth = new TestingAuthenticationToken("gateway://i3k9jfe5/1c6e7df7-fe10-4c53-acae-913e0ceec883", null);
        context.setAuthentication(auth);

        when(oAuthClientDetailsService.loadClientByIdAsRoot("gateway://i3k9jfe5/1c6e7df7-fe10-4c53-acae-913e0ceec883"))
                .thenReturn(ServiceResponseBuilder.<OauthClientDetails>ok()
                        .withResult(OauthClientDetails.builder().parentGateway(gateway).build()).build());

        when(deviceRegisterService.findByDeviceId(
                any(Tenant.class),
                any(Application.class),
                anyString()))
                .thenReturn(ServiceResponseBuilder.<Device>error()
                        .withMessage(DeviceRegisterService.Validations.DEVICE_ID_DOES_NOT_EXIST.getCode())
                        .build());


        getMockMvc().perform(
                get(MessageFormat.format("/gateway/data/sub/{0}/{1}", DEVICE_ID, CHANNEL))
                        .flashAttr("principal", gateway)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void shouldThrowDeviceNotAChildOnSubToKonkerPlatformGateway() throws Exception {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication auth = new TestingAuthenticationToken("gateway://i3k9jfe5/1c6e7df7-fe10-4c53-acae-913e0ceec883", null);
        context.setAuthentication(auth);

        when(oAuthClientDetailsService.loadClientByIdAsRoot("gateway://i3k9jfe5/1c6e7df7-fe10-4c53-acae-913e0ceec883"))
                .thenReturn(ServiceResponseBuilder.<OauthClientDetails>ok()
                        .withResult(OauthClientDetails.builder().parentGateway(gateway).build()).build());

        when(deviceRegisterService.findByDeviceId(
                any(Tenant.class),
                any(Application.class),
                anyString()))
                .thenReturn(ServiceResponseBuilder.<Device>ok()
                        .withResult(deviceNotChild)
                        .build());


        getMockMvc().perform(
                get(MessageFormat.format("/gateway/data/sub/{0}/{1}", DEVICE_ID, CHANNEL))
                        .flashAttr("principal", gateway)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void shouldThrowInvalidWaitTimeOnSubToKonkerPlatformGateway() throws Exception {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication auth = new TestingAuthenticationToken("gateway://i3k9jfe5/1c6e7df7-fe10-4c53-acae-913e0ceec883", null);
        context.setAuthentication(auth);

        when(oAuthClientDetailsService.loadClientByIdAsRoot("gateway://i3k9jfe5/1c6e7df7-fe10-4c53-acae-913e0ceec883"))
                .thenReturn(ServiceResponseBuilder.<OauthClientDetails>ok()
                        .withResult(OauthClientDetails.builder().parentGateway(gateway).build()).build());

        when(deviceRegisterService.findByDeviceId(
                any(Tenant.class),
                any(Application.class),
                anyString()))
                .thenReturn(ServiceResponseBuilder.<Device>ok()
                        .withResult(device)
                        .build());


        getMockMvc().perform(
                get(MessageFormat.format("/gateway/data/sub/{0}/{1}", DEVICE_ID, CHANNEL))
                        .flashAttr("principal", gateway)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("waitTime", String.valueOf(40000l))
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldThrowInvalidChannelOnSubToKonkerPlatformGateway() throws Exception {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication auth = new TestingAuthenticationToken("gateway://i3k9jfe5/1c6e7df7-fe10-4c53-acae-913e0ceec883", null);
        context.setAuthentication(auth);

        when(oAuthClientDetailsService.loadClientByIdAsRoot("gateway://i3k9jfe5/1c6e7df7-fe10-4c53-acae-913e0ceec883"))
                .thenReturn(ServiceResponseBuilder.<OauthClientDetails>ok()
                        .withResult(OauthClientDetails.builder().parentGateway(gateway).build()).build());

        when(deviceRegisterService.findByDeviceId(
                any(Tenant.class),
                any(Application.class),
                anyString()))
                .thenReturn(ServiceResponseBuilder.<Device>ok()
                        .withResult(device)
                        .build());


        getMockMvc().perform(
                get(MessageFormat.format("/gateway/data/sub/{0}/{1}", DEVICE_ID, INVALID_CHANNEL_CHAR))
                        .flashAttr("principal", gateway)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest());

        getMockMvc().perform(
                get(MessageFormat.format("/gateway/data/sub/{0}/{1}", DEVICE_ID, INVALID_CHANNEL_SIZE))
                        .flashAttr("principal", gateway)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldSubToKonkerPlatformGateway() throws Exception {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication auth = new TestingAuthenticationToken("gateway://i3k9jfe5/1c6e7df7-fe10-4c53-acae-913e0ceec883", null);
        context.setAuthentication(auth);

        when(oAuthClientDetailsService.loadClientByIdAsRoot("gateway://i3k9jfe5/1c6e7df7-fe10-4c53-acae-913e0ceec883"))
                .thenReturn(ServiceResponseBuilder.<OauthClientDetails>ok()
                        .withResult(OauthClientDetails.builder().parentGateway(gateway).build()).build());

        when(deviceRegisterService.findByDeviceId(
                any(Tenant.class),
                any(Application.class),
                anyString()))
                .thenReturn(ServiceResponseBuilder.<Device>ok()
                        .withResult(device)
                        .build());

        when(deviceEventService.findOutgoingBy(
                any(Tenant.class),
                any(Application.class),
                anyString(),
                anyString(),
                anyString(),
                any(Instant.class),
                any(),
                anyBoolean(),
                anyInt()))
                .thenReturn(ServiceResponseBuilder.<List<Event>>ok()
                        .withResult(events)
                        .build());


        getMockMvc().perform(
                get(MessageFormat.format("/gateway/data/sub/{0}/{1}", DEVICE_ID, CHANNEL))
                        .flashAttr("principal", gateway)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
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
        public GatewayEventProcessor gatewayEventProcessor() {
            return Mockito.mock(GatewayEventProcessor.class);
        }

        @Bean
        public JsonParsingService jsonParsingService() {
            return Mockito.mock(JsonParsingService.class);
        }

        @Bean
        public OAuthClientDetailsService oAuthClientDetailsService() {
            return Mockito.mock(OAuthClientDetailsService.class);
        }

        @Bean
        public RestTemplate restTemplate() {
            return Mockito.mock(RestTemplate.class);
        }

        @Bean
        public RabbitMQConfig rabbitMQConfig() {
            return Mockito.mock(RabbitMQConfig.class);
        }

        @Bean
        public Executor executor() {
            return Mockito.mock(Executor.class);
        }

        @Bean
        public JedisTaskService jedisTaskService() {
            return Mockito.mock(JedisTaskService.class);
        }

        @Bean
        public RedisTemplate redisTemplate() {
            return Mockito.mock(RedisTemplate.class);
        }

    }
}
