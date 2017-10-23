package com.konkerlabs.platform.registry.test.web.controllers;

import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.services.api.*;
import com.konkerlabs.platform.registry.config.CdnConfig;
import com.konkerlabs.platform.registry.config.EmailConfig;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

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
        HotjarConfig.class, 
        EmailConfig.class
})
public class ControlPanelControllerTest extends WebLayerTestContext {

	@Autowired
	private Tenant tenant;

    @Autowired
    private Application application;

	@Autowired
	private DeviceRegisterService deviceRegisterService;

	@Autowired
	private EventRouteService eventRouteService;

	@Autowired
	private TransformationService transformationService;

	@Autowired
	private RestDestinationService restDestinationService;

	@Autowired
	private ApplicationService applicationService;

	@After
	public void tearDown() {
		Mockito.reset(deviceRegisterService);
		Mockito.reset(eventRouteService);
		Mockito.reset(transformationService);
		Mockito.reset(restDestinationService);
		Mockito.reset(applicationService);
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

		when(deviceRegisterService.countAll(tenant, application))
				.thenReturn(ServiceResponseBuilder.<Long>ok().withResult(1L).build());
		when(eventRouteService.getAll(tenant, application))
				.thenReturn(ServiceResponseBuilder.<List<EventRoute>>ok().withResult(routes).build());
		when(transformationService.getAll(tenant, application))
				.thenReturn(ServiceResponseBuilder.<List<Transformation>>ok().withResult(transformations).build());
		when(restDestinationService.findAll(tenant, application))
				.thenReturn(ServiceResponseBuilder.<List<RestDestination>>ok().withResult(destinations).build());
		when(applicationService.findAll(tenant))
				.thenReturn(ServiceResponseBuilder.<List<Application>> ok().withResult(Collections.singletonList(application)).build());

		getMockMvc().perform(get("/"))
			.andExpect(view().name("panel/index"))
			.andExpect(model().attribute("devicesCount", 1))
			.andExpect(model().attribute("routesCount", 2))
			.andExpect(model().attribute("transformationsCount", 3))
			.andExpect(model().attribute("restDestinationsCount", 4));
	}

}
