package com.konkerlabs.platform.registry.web.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.konkerlabs.platform.registry.audit.model.TenantLog;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.TenantLogService;

@Controller
@Scope("request")
@RequestMapping("/tenants/log")
public class TenantLogController {

	@Autowired
	private TenantLogService tenantLogService;

	@Autowired
	private Tenant tenant;

	@RequestMapping(value = "", method = RequestMethod.GET)
	public ModelAndView index(
			@RequestParam(name = "asc", defaultValue = "false", required = false) Boolean ascendingOrder) {

		ServiceResponse<List<TenantLog>> response = tenantLogService.findByTenant(tenant, ascendingOrder);

		if (!response.isOk()) {
			return new ModelAndView("tenants/log/index").addObject("message", response.getResponseMessages())
					.addObject("logs", response.getResult()).addObject("asc", ascendingOrder);
		}

		return new ModelAndView("tenants/log/index").addObject("logs", response.getResult()).addObject("asc",
				ascendingOrder);


	}

}
