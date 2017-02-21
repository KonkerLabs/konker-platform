package com.konkerlabs.platform.registry.web.controllers;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.EventRoute;
import com.konkerlabs.platform.registry.business.model.RestDestination;
import com.konkerlabs.platform.registry.business.model.SmsDestination;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.Transformation;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.EventRouteService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.RestDestinationService;
import com.konkerlabs.platform.registry.business.services.api.SmsDestinationService;
import com.konkerlabs.platform.registry.business.services.api.TransformationService;
import com.konkerlabs.platform.registry.web.forms.EventRouteForm;

@Controller
@Scope("request")
@RequestMapping("routes")
public class EventRouteController implements ApplicationContextAware {

    public enum Messages {
        ROUTE_REGISTERED_SUCCESSFULLY("controller.event_route.registered.succesfully"),
        ROUTE_REMOVED_SUCCESSFULLY("controller.event_route.removed.succesfully");

        private String code;

        public String getCode() {
            return code;
        }

        Messages(String code) {
            this.code = code;
        }
    }

    @Autowired
    private EventRouteService eventRouteService;
    @Autowired
    private DeviceRegisterService deviceRegisterService;
    @Autowired
    private RestDestinationService restDestinationService;
    @Autowired
    private SmsDestinationService smsDestinationService;
    @Autowired
    private TransformationService transformationService;
    @Autowired
    private Tenant tenant;
    private ApplicationContext applicationContext;

    @ModelAttribute("allDevices")
    public List<Device> allDevices() {
        return deviceRegisterService.findAll(tenant).getResult();
    }

    @ModelAttribute("allRestDestinations")
    public List<RestDestination> allRestDestinations() {
        return restDestinationService.findAll(tenant).getResult();
    }

    @ModelAttribute("allSmsDestinations")
    public List<SmsDestination> allSmsDestinations() {
        return smsDestinationService.findAll(tenant).getResult();
    }

    @ModelAttribute("allTransformations")
    public List<Transformation> allTransformations() {
        return transformationService.getAll(tenant).getResult();
    }

    @RequestMapping
    @PreAuthorize("hasAuthority('LIST_ROUTES')")
    public ModelAndView index() {
        return new ModelAndView("routes/index","routes", eventRouteService.getAll(tenant).getResult());
    }

    @RequestMapping("new")
    @PreAuthorize("hasAuthority('CREATE_DEVICE_ROUTE')")
    public ModelAndView newRoute() {
        return new ModelAndView("routes/form")
            .addObject("route",new EventRouteForm())
            .addObject("action","/routes/save");
    }

    @RequestMapping(value = "save", method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('CREATE_DEVICE_ROUTE')")
    public ModelAndView save(@ModelAttribute("eventRouteForm") EventRouteForm eventRouteForm,
                             RedirectAttributes redirectAttributes, Locale locale) {

        return doSave(() -> {
            eventRouteForm.setAdditionalSupplier(() -> tenant.getDomainName());
            return eventRouteService.save(tenant, eventRouteForm.toModel());
        },eventRouteForm,locale,redirectAttributes, "");

    }

    @RequestMapping(value = "/{routeGUID}", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('SHOW_DEVICE_ROUTE')")
    public ModelAndView show(@PathVariable("routeGUID") String routeGUID) {
        return new ModelAndView("routes/show","route",new EventRouteForm().fillFrom(eventRouteService.getByGUID(tenant, routeGUID).getResult()));
    }

    @RequestMapping("/{routeId}/edit")
    @PreAuthorize("hasAuthority('EDIT_DEVICE_ROUTE')")
    public ModelAndView edit(@PathVariable String routeId) {
        return new ModelAndView("routes/form")
            .addObject("route",new EventRouteForm().fillFrom(eventRouteService.getByGUID(tenant, routeId).getResult()))
            .addObject("action", MessageFormat.format("/routes/{0}",routeId))
            .addObject("method", "put");
    }

    @RequestMapping(path = "/{routeGUID}", method = RequestMethod.PUT)
    @PreAuthorize("hasAuthority('EDIT_DEVICE_ROUTE')")
    public ModelAndView saveEdit(@PathVariable String routeGUID,
                                 @ModelAttribute("eventRouteForm") EventRouteForm eventRouteForm,
                                 RedirectAttributes redirectAttributes, Locale locale) {

        return doSave(() -> {
            eventRouteForm.setAdditionalSupplier(() -> tenant.getDomainName());            
            return eventRouteService.update(tenant, routeGUID, eventRouteForm.toModel());
        },eventRouteForm,locale,redirectAttributes,"put");

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

    private ModelAndView doSave(Supplier<ServiceResponse<EventRoute>> responseSupplier,
                                EventRouteForm eventRouteForm, Locale locale,
                                RedirectAttributes redirectAttributes, String method) {
        ServiceResponse<EventRoute> response = responseSupplier.get();

        switch (response.getStatus()) {
            case ERROR: {
                return new ModelAndView("routes/form")
                        .addObject("errors",
                            response.getResponseMessages().entrySet().stream().map(message -> applicationContext.getMessage(message.getKey(), message.getValue(), locale)).collect(Collectors.toList())
                        )
                        .addObject("route", eventRouteForm)
                        .addObject("method",method);
            }
            default: {
                redirectAttributes.addFlashAttribute("message",
                    applicationContext.getMessage(Messages.ROUTE_REGISTERED_SUCCESSFULLY.getCode(),null,locale)
                );
                return new ModelAndView(MessageFormat.format("redirect:/routes/{0}",
                        response.getResult().getGuid()));
            }
        }
    }

    @RequestMapping(path = "/{routeGUID}", method = RequestMethod.DELETE)
    @PreAuthorize("hasAuthority('REMOVE_DEVICE_ROUTE')")
    public ModelAndView remove(@PathVariable("routeGUID") String routeGUID,
                               RedirectAttributes redirectAttributes, Locale locale) {
        ServiceResponse<EventRoute> serviceResponse = eventRouteService.remove(tenant, routeGUID);

        redirectAttributes.addFlashAttribute("message",
                applicationContext.getMessage(Messages.ROUTE_REMOVED_SUCCESSFULLY.getCode(),null,locale)
        );

        return new ModelAndView("redirect:/routes");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
