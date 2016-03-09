package com.konkerlabs.platform.registry.web.controllers;

import com.konkerlabs.platform.registry.web.forms.TransformationForm;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("transformation")
@Scope("request")
public class TransformationController {

    @RequestMapping
    public ModelAndView index() {
        return new ModelAndView("transformations/index");
    }

    @RequestMapping("new")
    public ModelAndView newTransformation() {
        return new ModelAndView("transformations/form")
                .addObject("transformation", new TransformationForm())
                .addObject("action", "/transformation/save");
    }

    @RequestMapping(value = "save", method = RequestMethod.POST)
    public ModelAndView save(@ModelAttribute("routeTransformationForm") TransformationForm routeTransformationForm,
                             BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        return new ModelAndView("redirect:/transformation");
    }
}
