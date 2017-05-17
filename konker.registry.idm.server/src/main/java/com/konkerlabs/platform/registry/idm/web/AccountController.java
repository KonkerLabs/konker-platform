package com.konkerlabs.platform.registry.idm.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by andre on 17/05/17.
 */
@Controller
@RequestMapping("/account")
public class AccountController {

    @RequestMapping("/login")
    ModelAndView account(){
        return new ModelAndView("login");
    }
}
