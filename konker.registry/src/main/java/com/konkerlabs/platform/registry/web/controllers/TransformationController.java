package com.konkerlabs.platform.registry.web.controllers;

import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.Transformation;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.TransformationService;
import com.konkerlabs.platform.registry.web.forms.TransformationForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.MessageFormat;

@Controller
@RequestMapping("transformation")
@Scope("request")
public class TransformationController {

    @Autowired
    private TransformationService transformationService;
    @Autowired
    private Tenant tenant;

    @RequestMapping
    public ModelAndView index() {
        return new ModelAndView("transformations/index").addObject("transformations", transformationService.getAll(tenant).getResult());
    }

    @RequestMapping("new")
    public ModelAndView newTransformation() {
        return new ModelAndView("transformations/form")
                .addObject("transformation", new TransformationForm())
                .addObject("action", "/transformation/save");
    }

    @RequestMapping(value = "save", method = RequestMethod.POST)
    public ModelAndView save(@ModelAttribute("transformation") TransformationForm transformationForm,
                             BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        ServiceResponse<Transformation> serviceResponse = transformationService.register(tenant, transformationForm.toModel());

        switch (serviceResponse.getStatus()) {
            case ERROR:
                return new ModelAndView("/transformations/form")
                        .addObject("errors", serviceResponse.getResponseMessages())
                        .addObject("transformation", transformationForm);
            default:
                redirectAttributes.addFlashAttribute("message", "Transformation registered successfully");
                return new ModelAndView(MessageFormat.format("redirect:/transformation/{0}", serviceResponse.getResult().getId()));
        }
    }

    @RequestMapping(value = "/{transformationId}", method = RequestMethod.GET)
    public ModelAndView show(@PathVariable("transformationId") String transformationId) {
        return new ModelAndView("transformations/show","transformation",new TransformationForm().fillFrom(transformationService.get(tenant, transformationId).getResult()));
    }

    @RequestMapping("/{transformationId}/edit")
    public ModelAndView edit(@PathVariable("transformationId") String transformationId) {
        return new ModelAndView("transformations/form")
                .addObject("transformation",new TransformationForm().fillFrom(transformationService.get(tenant, transformationId).getResult()))
                .addObject("action", MessageFormat.format("/transformation/{0}",transformationId))
                .addObject("method", "put");
    }

    @RequestMapping(path = "/{transformationId}", method = RequestMethod.PUT)
    public ModelAndView saveEdit(@PathVariable String transformationId,
                                 @ModelAttribute("transformation") TransformationForm transformationForm,
                                 RedirectAttributes redirectAttributes) {

        ServiceResponse<Transformation> response = transformationService.update(tenant, transformationId, transformationForm.toModel());

        switch (response.getStatus()) {
            case ERROR: {
                return new ModelAndView("transformations/form")
                        .addObject("errors", response.getResponseMessages())
                        .addObject("transformation", transformationForm);
            }
            default: {
                redirectAttributes.addFlashAttribute("message", "Transformation updated successfully");
                return new ModelAndView(MessageFormat.format("redirect:/transformation/{0}",
                        response.getResult().getId()));
            }
        }
    }
}
