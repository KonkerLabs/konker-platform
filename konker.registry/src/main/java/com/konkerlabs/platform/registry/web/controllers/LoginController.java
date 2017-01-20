package com.konkerlabs.platform.registry.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.konkerlabs.platform.registry.config.RecaptchaConfig;

@Controller
@Scope("request")
public class LoginController {

	@Autowired
	private RecaptchaConfig recaptchaConfig;

    @RequestMapping("/login")
	public ModelAndView loginPage() {
		return new ModelAndView("login").addObject("siteKey", recaptchaConfig.getSiteKey());
    }

}
