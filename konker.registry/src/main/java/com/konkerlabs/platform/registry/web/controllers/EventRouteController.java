package com.konkerlabs.platform.registry.web.controllers;

import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.RestDestinationService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.TransformationService;
import com.konkerlabs.platform.registry.business.services.routes.api.EventRouteService;
import com.konkerlabs.platform.registry.web.forms.EventRouteForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.MessageFormat;
import java.util.List;

@Controller
@Scope("request")
@RequestMapping("routes")
public class EventRouteController {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventRouteController.class);

    @Autowired
    private EventRouteService eventRouteService;
    @Autowired
    private DeviceRegisterService deviceRegisterService;
    @Autowired
    private RestDestinationService restDestinationService;
    @Autowired
    private TransformationService transformationService;
    @Autowired
    private Tenant tenant;

    @ModelAttribute("allDevices")
    public List<Device> allDevices() {
        return deviceRegisterService.findAll(tenant).getResult();
    }

    @ModelAttribute("allRestDestinations")
    public List<RestDestination> allRestDestinations() {
        return restDestinationService.findAll(tenant).getResult();
    }

    @ModelAttribute("allTransformations")
    public List<Transformation> allTransformations() {
        return transformationService.getAll(tenant).getResult();
    }

    @RequestMapping
    public ModelAndView index() {
        return new ModelAndView("routes/index","routes", eventRouteService.getAll(tenant));
    }

    @RequestMapping("new")
    public ModelAndView newRoute() {
        return new ModelAndView("routes/form")
            .addObject("route",new EventRouteForm())
            .addObject("action","/routes/save");
    }

    @RequestMapping(value = "save", method = RequestMethod.POST)
    public ModelAndView save(@ModelAttribute("eventRouteForm") EventRouteForm eventRouteForm,
                             RedirectAttributes redirectAttributes) {
        ServiceResponse<EventRoute> response;
        eventRouteForm.setAdditionalSupplier(() -> tenant.getDomainName());
        response = eventRouteService.save(tenant, eventRouteForm.toModel());

        switch (response.getStatus()) {
            case ERROR: {
                return new ModelAndView("routes/form")
                    .addObject("errors",response.getResponseMessages())
                    .addObject("route", eventRouteForm);
            }
            default: {
                redirectAttributes.addFlashAttribute("message", "Route registered successfully");
                return new ModelAndView(MessageFormat.format("redirect:/routes/{0}",
                        response.getResult().getId()));
            }
        }
    }

    @RequestMapping(value = "/{routeId}", method = RequestMethod.GET)
    public ModelAndView show(@PathVariable("routeId") String routeId) {
        return new ModelAndView("routes/show","route",new EventRouteForm().fillFrom(eventRouteService.getById(tenant, routeId).getResult()));
    }

    @RequestMapping("/{routeId}/edit")
    public ModelAndView edit(@PathVariable String routeId) {
        return new ModelAndView("routes/form")
            .addObject("route",new EventRouteForm().fillFrom(eventRouteService.getById(tenant, routeId).getResult()))
            .addObject("action", MessageFormat.format("/routes/{0}",routeId));
    }

    @RequestMapping(path = "/{routeId}", method = RequestMethod.POST)
    public ModelAndView saveEdit(@PathVariable String routeId,
                                 @ModelAttribute("eventRouteForm") EventRouteForm eventRouteForm,
                                 RedirectAttributes redirectAttributes) {
        eventRouteForm.setId(routeId);
        return save(eventRouteForm, redirectAttributes);
    }

    @RequestMapping("/outgoing/{outgoingScheme}")
    public ModelAndView outgoingFragment(@PathVariable String outgoingScheme) {
        EventRouteForm route = new EventRouteForm();
        switch (outgoingScheme) {
            case "device": return new ModelAndView("routes/device-outgoing", "route", route);
            case "sms" : return new ModelAndView("routes/sms-outgoing", "route", route);
            case "rest" : return new ModelAndView("routes/rest-outgoing", "route", route);
            //FIXME: Check for a way to render an empty HTTP body without an empty html file
            default: return new ModelAndView("common/empty");
        }
    }
}
