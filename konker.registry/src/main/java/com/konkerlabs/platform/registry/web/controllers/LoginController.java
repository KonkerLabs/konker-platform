package com.konkerlabs.platform.registry.web.controllers;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

@Controller
@Scope("request")
public class LoginController {

	private static final Config recaptchaConfig = ConfigFactory.load().getConfig("recaptcha");
	private static final String siteKey = recaptchaConfig.getString("siteKey");

    @RequestMapping("/login")
	public ModelAndView loginPage() {
		return new ModelAndView("login").addObject("siteKey", siteKey);
    }

}
