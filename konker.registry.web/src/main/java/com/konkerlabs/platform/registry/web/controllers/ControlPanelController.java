package com.konkerlabs.platform.registry.web.controllers;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import com.konkerlabs.platform.registry.business.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.EventRouteService;
import com.konkerlabs.platform.registry.business.services.api.RestDestinationService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.TransformationService;

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
	private ApplicationService applicationService;

	@Autowired
	private TransformationService transformationService;

	@Autowired
	private RestDestinationService restDestinationService;

	@RequestMapping
	public ModelAndView panelPage() throws NoSuchAlgorithmException {
		ModelAndView mv = new ModelAndView("panel/index");

		int devicesCount = 0;
		int routesCount = 0;
		int transformationsCount = 0;
		int restDestinationsCount = 0;

		List<Application> applications = applicationService.findAll(tenant).getResult();

		for (Application app : applications) {
			ServiceResponse<Long> deviceResponse = deviceRegisterService.countAll(tenant, app);
			if (deviceResponse.isOk()) {
				devicesCount += deviceResponse.getResult();
			}

			ServiceResponse<List<EventRoute>> routesResponse = eventRouteService.getAll(tenant, app);
			if (routesResponse.isOk()) {
				routesCount += routesResponse.getResult().size();
			}

			ServiceResponse<List<Transformation>> transformationResponse = transformationService.getAll(tenant, app);
			if (transformationResponse.isOk()) {
				transformationsCount += transformationResponse.getResult().size();
			}

			ServiceResponse<List<RestDestination>> destinationsResponse = restDestinationService.findAll(tenant, app);
			if (destinationsResponse.isOk()) {
				restDestinationsCount += destinationsResponse.getResult().size();
			}
		}

		User principal = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		byte[] hash = digest.digest(principal.getEmail().getBytes(StandardCharsets.UTF_8));

		mv.addObject("devicesCount", devicesCount);
		mv.addObject("routesCount", routesCount);
		mv.addObject("transformationsCount", transformationsCount);
		mv.addObject("restDestinationsCount", restDestinationsCount);
		mv.addObject("hashUser", new String(Hex.encode(hash)));

		return mv;
	}

}
