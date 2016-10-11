package com.konkerlabs.platform.registry.web.controllers;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

@ControllerAdvice("com.konkerlabs.platform.registry.web.controllers")
public class HotjarTrackingController {
	
	private static final Config config = ConfigFactory.load();
	
	@ModelAttribute
	public void setupConfig(Model model) {
		if (config.hasPath("hotjar")) {
			Config hotjarConfig = config.getConfig("hotjar");
			model.addAttribute("hotjarEnable", hotjarConfig.getBoolean("enable"));
			model.addAttribute("hotjarId", hotjarConfig.getInt("id"));
		}
	}

}
