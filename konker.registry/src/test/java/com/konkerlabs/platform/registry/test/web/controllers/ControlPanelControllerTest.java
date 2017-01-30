package com.konkerlabs.platform.registry.test.web.controllers;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.EventRoute;
import com.konkerlabs.platform.registry.business.model.RestDestination;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.Transformation;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.RestDestinationService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.business.services.api.TransformationService;
import com.konkerlabs.platform.registry.business.services.routes.api.EventRouteService;
import com.konkerlabs.platform.registry.config.CdnConfig;
import com.konkerlabs.platform.registry.config.HotjarConfig;
import com.konkerlabs.platform.registry.config.WebConfig;
import com.konkerlabs.platform.registry.config.WebMvcConfig;
import com.konkerlabs.platform.registry.test.base.SecurityTestConfiguration;
import com.konkerlabs.platform.registry.test.base.WebLayerTestContext;
import com.konkerlabs.platform.registry.test.base.WebTestConfiguration;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {
        WebMvcConfig.class,
        WebTestConfiguration.class,
        SecurityTestConfiguration.class,
        DeviceVisualizationControllerTest.DeviceTestContextConfig.class, 
        EventRouteControllerTest.EventRouteTestContextConfig.class,
        WebConfig.class,
        CdnConfig.class,
        HotjarConfig.class
})
public class ControlPanelControllerTest extends WebLayerTestContext {

	@Autowired
	private Tenant tenant;

	@Autowired
	private DeviceRegisterService deviceRegisterService;

	@Autowired
	private EventRouteService eventRouteService;

	@Autowired
	private TransformationService transformationService;

	@Autowired
	private RestDestinationService restDestinationService;

	@After
	public void tearDown() {
		Mockito.reset(deviceRegisterService);
		Mockito.reset(eventRouteService);
		Mockito.reset(transformationService);
		Mockito.reset(restDestinationService);
	}

	@Test
	@WithMockUser(authorities = { "ROLE_SUPER_USER", "ROLE_IOT_USER", "ROLE_ANALYTICS_USER" })
	public void shouldShowControlPanelHome() throws Exception {

		List<Device> devices = new ArrayList<>();
		devices.add(Device.builder().build());

		List<EventRoute> routes = new ArrayList<>();
		routes.add(EventRoute.builder().build());
		routes.add(EventRoute.builder().build());

		List<Transformation> transformations = new ArrayList<>();
		transformations.add(Transformation.builder().build());
		transformations.add(Transformation.builder().build());
		transformations.add(Transformation.builder().build());

		List<RestDestination> destinations = new ArrayList<>();
		destinations.add(RestDestination.builder().build());
		destinations.add(RestDestination.builder().build());
		destinations.add(RestDestination.builder().build());
		destinations.add(RestDestination.builder().build());

		when(deviceRegisterService.findAll(tenant))
				.thenReturn(ServiceResponseBuilder.<List<Device>>ok().withResult(devices).build());
		when(eventRouteService.getAll(tenant))
				.thenReturn(ServiceResponseBuilder.<List<EventRoute>>ok().withResult(routes).build());
		when(transformationService.getAll(tenant))
				.thenReturn(ServiceResponseBuilder.<List<Transformation>>ok().withResult(transformations).build());
		when(restDestinationService.findAll(tenant))
				.thenReturn(ServiceResponseBuilder.<List<RestDestination>>ok().withResult(destinations).build());

		getMockMvc().perform(get("/"))
			.andExpect(view().name("panel/index"))
			.andExpect(model().attribute("devicesCount", 1))
			.andExpect(model().attribute("routesCount", 2))
			.andExpect(model().attribute("transformationsCount", 3))
			.andExpect(model().attribute("restDestinationsCount", 4));
	}

}
