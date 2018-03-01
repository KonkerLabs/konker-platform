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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Event.EventActor;
import com.konkerlabs.platform.registry.business.model.EventSchema;
import com.konkerlabs.platform.registry.business.model.EventSchema.SchemaField;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.model.enumerations.DateFormat;
import com.konkerlabs.platform.registry.business.model.enumerations.Language;
import com.konkerlabs.platform.registry.business.model.enumerations.TimeZone;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.EventSchemaService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.config.EmailConfig;
import com.konkerlabs.platform.registry.config.EnvironmentConfig;
import com.konkerlabs.platform.registry.config.MessageSourceConfig;
import com.konkerlabs.platform.registry.config.WebConfig;
import com.konkerlabs.platform.registry.config.WebMvcConfig;
import com.konkerlabs.platform.registry.security.UserContextResolver;
import com.konkerlabs.platform.registry.test.base.SecurityTestConfiguration;
import com.konkerlabs.platform.registry.test.base.WebLayerTestContext;
import com.konkerlabs.platform.registry.test.base.WebTestConfiguration;


@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {
        WebMvcConfig.class,
        WebTestConfiguration.class,
        SecurityTestConfiguration.class,
        DeviceVisualizationControllerTest.DeviceTestContextConfig.class,
        WebConfig.class,
        EnvironmentConfig.class,
        EmailConfig.class,
        MessageSourceConfig.class
})
public class DeviceVisualizationControllerTest extends WebLayerTestContext {

    private static final String DEVICE_GUID = "169897e9-ed44-41d1-978d-d244d78e9a67";
    private static final String CHANNEL = "datain";
    private static final String TENANT_DOMAIN = "inmetrics.com";

    @Autowired
	private EventSchemaService eventSchemaService;
    @Autowired
    private DeviceEventService deviceEventService;
    @Autowired
	private DeviceRegisterService deviceRegisterService;
    @Autowired
    private Tenant tenant;
    @Autowired
    private Application application;
    @Autowired
    private UserContextResolver userContextResolver;
    @Autowired
    private User user;


    private List<String> channels;
	private EventSchema eventSchema;
	private List<Event> eventsList;
	private Instant startingTimestamp;
	private Instant endTimestamp;
	private String dateStart = "01/10/2016 17:47:20";
	private String dateEnd = "04/10/2016 17:47:20";

    @Before
    public void setUp() {

		user.setDateFormat(DateFormat.DDMMYYYY);
		user.setLanguage(Language.PT_BR);
		user.setZoneId(TimeZone.AMERICA_SAO_PAULO);
    	channels = Arrays.asList("channelin", "channelout", "channelin2");

    	eventSchema = EventSchema.builder().channel(CHANNEL).deviceGuid(DEVICE_GUID)
    			.field(SchemaField.builder().build()).build();

    	eventsList = new ArrayList<>();

		Event event = Event.builder().creationTimestamp(Instant.ofEpochSecond(1475603097l))
    			.incoming(EventActor.builder().tenantDomain(TENANT_DOMAIN).deviceGuid(DEVICE_GUID).channel(CHANNEL).build())
    			.outgoing(EventActor.builder().tenantDomain(TENANT_DOMAIN).deviceGuid(DEVICE_GUID).channel(CHANNEL).build())
    			.payload("{\"a\": 109, \"b\": 111}").build();
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
    @WithMockUser(authorities={"VIEW_DEVICE_CHART"})
    public void shouldLoadChannels() throws Exception {
    	when(eventSchemaService.findKnownIncomingChannelsBy(tenant, null, DEVICE_GUID))
    		.thenReturn(ServiceResponseBuilder.<List<String>>ok()
    				.withResult(channels).build());

    	getMockMvc().perform(get("/devices/visualization/loading/channel/").param("deviceGuid", DEVICE_GUID))
    		.andExpect(model().attribute("channels", equalTo(channels)))
    		.andExpect(view().name("devices/visualization/channels"));
    }

    @Test
    @WithMockUser(authorities={"VIEW_DEVICE_CHART"})
    public void shouldLoadMetrics() throws Exception {
    	List<String> listMetrics = eventSchema.getFields()
				.stream()
				.filter(schemaField -> schemaField.getKnownTypes().contains(JsonNodeType.NUMBER))
				.map(m -> m.getPath()).collect(java.util.stream.Collectors.toList());

        when(deviceRegisterService.findByTenantDomainNameAndDeviceGuid(tenant.getDomainName(), DEVICE_GUID))
                .thenReturn(Device.builder().application(application).build());

        when(eventSchemaService.findKnownIncomingMetricsBy(tenant, application, DEVICE_GUID, CHANNEL, JsonNodeType.NUMBER))
			.thenReturn(ServiceResponseBuilder.<List<String>>ok()
			.withResult(new ArrayList<String>(listMetrics)).build());

    	getMockMvc().perform(get("/devices/visualization/loading/metrics/").param("deviceGuid", DEVICE_GUID).param("channel", CHANNEL))
    		.andExpect(model().attribute("metrics", equalTo(listMetrics)))
    		.andExpect(view().name("devices/visualization/metrics"));
    }

    @Test
    @WithMockUser(authorities={"VIEW_DEVICE_CHART"})
    public void shouldReturnDeviceIsMandatoryMessage() throws Exception {

    	getMockMvc().perform(get("/devices/visualization/load/").param("dateStart", "").param("dateEnd", "").param("online", "false")
    			.param("deviceGuid", "").param("channel", CHANNEL))
    			.andExpect(content().json("[{'message':'Device is mandatory'}]"));
    }

    @Test
    @WithMockUser(authorities={"VIEW_DEVICE_CHART"})
    public void shouldReturnChannelIsMandatoryMessage() throws Exception {

    	getMockMvc().perform(get("/devices/visualization/load/").param("dateStart", "").param("dateEnd", "").param("online", "false")
    			.param("deviceGuid", DEVICE_GUID).param("channel", ""))
    			.andExpect(content().json("[{'message':'Channel is mandatory'}]"));
    }

    @Test
    @WithMockUser(authorities={"VIEW_DEVICE_CHART"})
    public void shouldReturnDateStartIsMandatoryMessage() throws Exception {

    	getMockMvc().perform(get("/devices/visualization/load/").param("dateStart", "").param("dateEnd", "").param("online", "false")
    			.param("deviceGuid", DEVICE_GUID).param("channel", CHANNEL))
    			.andExpect(content().json("[{'message':'Dt/hr start is mandatory'}]"));
    }

    @Test
    @WithMockUser(authorities={"VIEW_DEVICE_CHART"})
    public void shouldReturnDateEndIsMandatoryMessage() throws Exception {

    	getMockMvc().perform(get("/devices/visualization/load/").param("dateStart", dateStart).param("dateEnd", "").param("online", "false")
    			.param("deviceGuid", DEVICE_GUID).param("channel", CHANNEL))
    			.andExpect(content().json("[{'message':'Dt/hr end is mandatory'}]"));
    }

    @Test
    @WithMockUser(authorities={"VIEW_DEVICE_CHART"})
    public void shouldReturnDataOnline() throws Exception {
    	when(userContextResolver.getObject()).thenReturn(user);

        when(deviceRegisterService.findByTenantDomainNameAndDeviceGuid(tenant.getDomainName(), DEVICE_GUID))
                .thenReturn(Device.builder().application(application).build());

        when(deviceEventService.findIncomingBy(tenant, application, DEVICE_GUID, CHANNEL, null, null, false, 100))
			.thenReturn(ServiceResponseBuilder.<List<Event>>ok()
				.withResult(eventsList).build());

    	getMockMvc().perform(get("/devices/visualization/load/").param("dateStart", "").param("dateEnd", "").param("online", "true")
    			.param("deviceGuid", DEVICE_GUID).param("channel", CHANNEL))
    			.andExpect(content().json("[{'timestampFormated': '04/10/2016 14:44:57.000 BRT',"
    					+ "'timestamp': 1475603097000,"
    					+ "'incoming':{"
    					+ "'tenantDomain':'inmetrics.com','deviceGuid':'169897e9-ed44-41d1-978d-d244d78e9a67','channel':'datain'},"
    					+ "'payload':'{\"a\": 109, \"b\": 111}'}]"));
    }

    @Test
    @WithMockUser(authorities={"VIEW_DEVICE_CHART"})
    public void shouldReturnDataByDateRange() throws Exception {
    	when(userContextResolver.getObject()).thenReturn(user);

        when(deviceRegisterService.findByTenantDomainNameAndDeviceGuid(tenant.getDomainName(), DEVICE_GUID))
                .thenReturn(Device.builder().application(application).build());

        when(deviceEventService.findIncomingBy(tenant, application, DEVICE_GUID, CHANNEL, startingTimestamp, endTimestamp, false, 100))
			.thenReturn(ServiceResponseBuilder.<List<Event>>ok()
				.withResult(eventsList).build());

		getMockMvc().perform(get("/devices/visualization/load/").param("dateStart", dateStart).param("dateEnd", dateEnd).param("online", "false")
    			.param("deviceGuid", DEVICE_GUID).param("channel", CHANNEL))
    			.andExpect(content().json("[{'timestampFormated': '04/10/2016 14:44:57.000 BRT',"
    					+ "'timestamp': 1475603097000,"
    					+ "'incoming':{"
    					+ "'tenantDomain':'inmetrics.com','deviceGuid':'169897e9-ed44-41d1-978d-d244d78e9a67','channel':'datain'},"
    					+ "'payload':'{\"a\": 109, \"b\": 111}'}]"));
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
