package com.konkerlabs.platform.registry.web.controllers;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Scope("request")
public class LoginController {

    @RequestMapping("/login")
    public String loginPage() {
        return "login";
    }

}
