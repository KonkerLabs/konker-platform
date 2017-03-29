package com.konkerlabs.platform.registry.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.konkerlabs.platform.registry.config.HotjarConfig;

@ControllerAdvice("com.konkerlabs.platform.registry.web.controllers")
public class HotjarTrackingController {
	
	@Autowired
	private HotjarConfig hotjarConfig;
	
	@ModelAttribute
	public void setupConfig(Model model) {
		model.addAttribute("hotjarEnable", hotjarConfig.isEnable());
		model.addAttribute("hotjarId", hotjarConfig.getId());
	}

}
