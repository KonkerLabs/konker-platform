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
        ModelAndView mav = new ModelAndView("login");
        mav.addObject("recaptchaEnabled", recaptchaConfig.isEnabled());
        if (recaptchaConfig.isEnabled()) {
            mav.addObject("recaptchaSiteKey", recaptchaConfig.getSiteKey());
        }
		return mav;
    }

}
