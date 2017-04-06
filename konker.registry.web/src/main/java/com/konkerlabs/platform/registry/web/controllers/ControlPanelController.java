package com.konkerlabs.platform.registry.web.controllers;

import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.services.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@Scope("request")
@RequestMapping("/")
public class ControlPanelController {

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

	@RequestMapping
	public ModelAndView panelPage() {
		ModelAndView mv = new ModelAndView("panel/index");

		int devicesCount = 0;
		int routesCount = 0;
		int transformationsCount = 0;
		int restDestinationsCount = 0;

		ServiceResponse<List<Device>> deviceResponse = deviceRegisterService.findAll(tenant, null);
		if (deviceResponse.isOk()) {
			devicesCount = deviceResponse.getResult().size();
		}
		
		ServiceResponse<List<EventRoute>> routesResponse = eventRouteService.getAll(tenant);
		if (routesResponse.isOk()) {
			routesCount = routesResponse.getResult().size();
		}
		
		ServiceResponse<List<Transformation>> transformationResponse = transformationService.getAll(tenant, null);
		if (transformationResponse.isOk()) {
			transformationsCount = transformationResponse.getResult().size();
		}
		
		ServiceResponse<List<RestDestination>> destinationsResponse = restDestinationService.findAll(tenant);
		if (destinationsResponse.isOk()) {
			restDestinationsCount = destinationsResponse.getResult().size();
		}
		
		mv.addObject("devicesCount", devicesCount);
		mv.addObject("routesCount", routesCount);
		mv.addObject("transformationsCount", transformationsCount);
		mv.addObject("restDestinationsCount", restDestinationsCount);

		return mv;
	}

}
