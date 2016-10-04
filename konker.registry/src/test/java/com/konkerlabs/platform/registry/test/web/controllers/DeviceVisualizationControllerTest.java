package com.konkerlabs.platform.registry.test.web.controllers;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    private static final String DEVICE_GUID = "7d51c242-81db-11e6-a8c2-0746f010e945";
    private static final String CHANNEL = "channelin";
    
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

    @Before
    public void setUp() {
    	channels = Arrays.asList("channelin", "channelout", "channelin2");

    	eventSchema = EventSchema.builder().channel(CHANNEL).deviceGuid(DEVICE_GUID)
    			.field(SchemaField.builder().build()).build();
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
    	
    	List<String> listMetrics = eventSchema.getFields().stream().map(m -> m.getPath()).collect(java.util.stream.Collectors.toList());
    	getMockMvc().perform(get("/visualization/loading/metrics/").param("deviceGuid", DEVICE_GUID).param("channel", CHANNEL))
    		.andExpect(model().attribute("metrics", equalTo(listMetrics)))
    		.andExpect(view().name("visualization/metrics"));
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