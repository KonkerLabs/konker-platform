package com.konkerlabs.platform.registry.web.controllers;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@Scope("request")
@RequestMapping("/")
public class ControlPanelController {

    @RequestMapping
    public ModelAndView panelPage() {
    	return new ModelAndView("panel/index");
    }

}
