package com.konkerlabs.platform.registry.idm.web;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ProbeController {

    @RequestMapping("/probe")
    ModelAndView probe(){
        return new ModelAndView("index");
    }
}
