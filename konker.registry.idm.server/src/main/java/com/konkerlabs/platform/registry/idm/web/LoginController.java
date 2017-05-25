package com.konkerlabs.platform.registry.idm.web;

import com.konkerlabs.platform.registry.idm.config.RecaptchaConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;


@Controller
public class LoginController {

    @Autowired
    private RecaptchaConfig recaptchaConfig;

    @RequestMapping("/login")
    public ModelAndView account(){
        ModelAndView mav = new ModelAndView("login");
        mav.addObject("recaptchaEnabled", recaptchaConfig.isEnabled());
        if (recaptchaConfig.isEnabled()) {
            mav.addObject("recaptchaSiteKey", recaptchaConfig.getSiteKey());
        }
        return mav;
    }
}
