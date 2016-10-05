package com.konkerlabs.platform.registry.test.web.controllers;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.node.JsonNodeType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Event.EventActor;
import com.konkerlabs.platform.registry.business.model.EventSchema;
import com.konkerlabs.platform.registry.business.model.EventSchema.SchemaField;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.EventSchemaService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.config.WebMvcConfig;
import com.konkerlabs.platform.registry.test.base.SecurityTestConfiguration;
import com.konkerlabs.platform.registry.test.base.WebLayerTestContext;
import com.konkerlabs.platform.registry.test.base.WebTestConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {
        WebMvcConfig.class,
        WebTestConfiguration.class,
        SecurityTestConfiguration.class,
        DeviceVisualizationControllerTest.DeviceTestContextConfig.class
})
public class DeviceVisualizationControllerTest extends WebLayerTestContext {

    private static final String DEVICE_GUID = "169897e9-ed44-41d1-978d-d244d78e9a67";
    private static final String CHANNEL = "datain";
    private static final String TENANT_DOMAIN = "inmetrics.com";
    
    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    EventSchemaService eventSchemaService;
    @Autowired
    DeviceRegisterService deviceRegisterService;
    @Autowired
    DeviceEventService deviceEventService;
    @Autowired
    private Tenant tenant;

    private List<String> channels;
	private EventSchema eventSchema;
	private List<Event> eventsList;
	private Instant startingTimestamp;
	private Instant endTimestamp;
	private String dateStart = "01/10/2016 17:47:20";
	private String dateEnd = "04/10/2016 17:47:20";
	
    @Before
    public void setUp() {
    	channels = Arrays.asList("channelin", "channelout", "channelin2");

    	eventSchema = EventSchema.builder().channel(CHANNEL).deviceGuid(DEVICE_GUID)
    			.field(SchemaField.builder().build()).build();
    	
    	eventsList = new ArrayList<>();
    	
		Event event = Event.builder().timestamp(Instant.ofEpochSecond(1475603097l))
    			.incoming(EventActor.builder().tenantDomain(TENANT_DOMAIN).deviceGuid(DEVICE_GUID).channel(CHANNEL).build())
    			.outgoing(EventActor.builder().tenantDomain(TENANT_DOMAIN).deviceGuid(DEVICE_GUID).channel(CHANNEL).build())
    			.payload("{\"a\": 109, \"b\": 111}").deleted(null).build();
		eventsList.add(event);
    	
    	LocalDateTime start = LocalDateTime.parse(dateStart, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    	LocalDateTime end = LocalDateTime.parse(dateEnd, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    	ZonedDateTime zonedDateStart = ZonedDateTime.of(start, ZoneId.of("America/Sao_Paulo"));
    	ZonedDateTime zonedDateEnd = ZonedDateTime.of(end, ZoneId.of("America/Sao_Paulo"));
    	startingTimestamp = zonedDateStart.toInstant();
    	endTimestamp = zonedDateEnd.toInstant();
    }

    @After
    public void tearDown() {
        Mockito.reset(eventSchemaService);
    }
    
    @Test
    public void shouldLoadChannels() throws Exception {
    	when(eventSchemaService.findKnownIncomingChannelsBy(tenant, DEVICE_GUID))
    		.thenReturn(ServiceResponseBuilder.<List<String>>ok()
    				.withResult(channels).build());
    	
    	getMockMvc().perform(get("/visualization/loading/channel/").param("deviceGuid", DEVICE_GUID))
    		.andExpect(model().attribute("channels", equalTo(channels)))
    		.andExpect(view().name("visualization/channels"));
    }
    
    @Test
    public void shouldLoadMetrics() throws Exception {
    	when(eventSchemaService.findIncomingBy(DEVICE_GUID, CHANNEL))
    		.thenReturn(ServiceResponseBuilder.<EventSchema>ok()
    				.withResult(eventSchema).build());
    	
    	List<String> listMetrics = eventSchema.getFields()
				.stream()
				.filter(schemaField -> schemaField.getKnownTypes().contains(JsonNodeType.NUMBER))
				.map(m -> m.getPath()).collect(java.util.stream.Collectors.toList());
    	getMockMvc().perform(get("/visualization/loading/metrics/").param("deviceGuid", DEVICE_GUID).param("channel", CHANNEL))
    		.andExpect(model().attribute("metrics", equalTo(listMetrics)))
    		.andExpect(view().name("visualization/metrics"));
    }
    
    @Test
    public void shouldReturnDeviceIsMandatoryMessage() throws Exception {
    	
    	getMockMvc().perform(get("/visualization/load/").param("dateStart", "").param("dateEnd", "").param("online", "false")
    			.param("deviceGuid", "").param("channel", CHANNEL))
    			.andExpect(content().json("[{'message':'Device is mandatory'}]"));
    }
    
    @Test
    public void shouldReturnChannelIsMandatoryMessage() throws Exception {
    	
    	getMockMvc().perform(get("/visualization/load/").param("dateStart", "").param("dateEnd", "").param("online", "false")
    			.param("deviceGuid", DEVICE_GUID).param("channel", ""))
    			.andExpect(content().json("[{'message':'Channel is mandatory'}]"));
    }
    
    @Test
    public void shouldReturnDateStartIsMandatoryMessage() throws Exception {
    	
    	getMockMvc().perform(get("/visualization/load/").param("dateStart", "").param("dateEnd", "").param("online", "false")
    			.param("deviceGuid", DEVICE_GUID).param("channel", CHANNEL))
    			.andExpect(content().json("[{'message':'Dt/hr start is mandatory'}]"));
    }
    
    @Test
    public void shouldReturnDateEndIsMandatoryMessage() throws Exception {
    	
    	getMockMvc().perform(get("/visualization/load/").param("dateStart", dateStart).param("dateEnd", "").param("online", "false")
    			.param("deviceGuid", DEVICE_GUID).param("channel", CHANNEL))
    			.andExpect(content().json("[{'message':'Dt/hr end is mandatory'}]"));
    }
    
    @Test
    public void shouldReturnDataOnline() throws Exception {
    	when(deviceEventService.findIncomingBy(tenant, DEVICE_GUID, CHANNEL, null, null, false, 100))
			.thenReturn(ServiceResponseBuilder.<List<Event>>ok()
				.withResult(eventsList).build());
    	
    	getMockMvc().perform(get("/visualization/load/").param("dateStart", "").param("dateEnd", "").param("online", "true")
    			.param("deviceGuid", DEVICE_GUID).param("channel", CHANNEL))
    			.andExpect(content().json("[{'timestamp':{'nano':0,'epochSecond':1475603097},"
    					+ "'incoming':{"
    					+ "'tenantDomain':'inmetrics.com','deviceGuid':'169897e9-ed44-41d1-978d-d244d78e9a67','channel':'datain'},"
    					+ "'outgoing':{'tenantDomain':'inmetrics.com','deviceGuid':'169897e9-ed44-41d1-978d-d244d78e9a67','channel':'datain'},"
    					+ "'payload':'{\"a\": 109, \"b\": 111}','deleted':null}]"));
    }
    
    @Test
    public void shouldReturnDataByDateRange() throws Exception {
    	when(deviceEventService.findIncomingBy(tenant, DEVICE_GUID, CHANNEL, startingTimestamp, endTimestamp, false, 100))
			.thenReturn(ServiceResponseBuilder.<List<Event>>ok()
				.withResult(eventsList).build());
    	
    	getMockMvc().perform(get("/visualization/load/").param("dateStart", dateStart).param("dateEnd", dateEnd).param("online", "false")
    			.param("deviceGuid", DEVICE_GUID).param("channel", CHANNEL))
    			.andExpect(content().json("[{'timestamp':{'nano':0,'epochSecond':1475603097},"
    					+ "'incoming':{"
    					+ "'tenantDomain':'inmetrics.com','deviceGuid':'169897e9-ed44-41d1-978d-d244d78e9a67','channel':'datain'},"
    					+ "'outgoing':{'tenantDomain':'inmetrics.com','deviceGuid':'169897e9-ed44-41d1-978d-d244d78e9a67','channel':'datain'},"
    					+ "'payload':'{\"a\": 109, \"b\": 111}','deleted':null}]"));
    }

    @Configuration
    static class DeviceTestContextConfig {
    	@Bean
        public DeviceRegisterService deviceRegisterService() {
            return Mockito.mock(DeviceRegisterService.class);
        }
        @Bean
        public DeviceEventService deviceEventService() { return Mockito.mock(DeviceEventService.class); }
        
        @Bean
        public EventSchemaService eventSchemaService() {
        	return Mockito.mock(EventSchemaService.class);
        }
    }
}