package com.konkerlabs.platform.registry.web.controllers;

import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.services.api.*;
import com.konkerlabs.platform.registry.web.forms.EventRouteForm;
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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
    private TransformationService transformationService;
    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private Tenant tenant;
    @Autowired
    private Application application;
    private ApplicationContext applicationContext;

    @ModelAttribute("allDevices")
    public List<Device> allDevices() {
    	List<Application> applications = applicationService.findAll(tenant).getResult();
    	List<Device> devices = new ArrayList<>();

    	applications.forEach(app -> devices.addAll(deviceRegisterService.findAll(tenant, app).getResult()));

        return devices;
    }

    @ModelAttribute("allRestDestinations")
    public List<RestDestination> allRestDestinations() {
    	List<Application> applications = applicationService.findAll(tenant).getResult();
    	List<RestDestination> restDestinations = new ArrayList<>();

    	applications.forEach(app -> restDestinations.addAll(restDestinationService.findAll(tenant, app).getResult()));

        return restDestinations;
    }

    @ModelAttribute("allTransformations")
    public List<Transformation> allTransformations() {
    	List<Application> applications = applicationService.findAll(tenant).getResult();
    	List<Transformation> transformations = new ArrayList<>();

    	applications.forEach(app -> transformations.addAll(transformationService.getAll(tenant, app).getResult()));

        return transformations;
    }

    @RequestMapping
    @PreAuthorize("hasAuthority('LIST_ROUTES')")
    public ModelAndView index() {
    	List<Application> applications = applicationService.findAll(tenant).getResult();
    	List<EventRoute> routes = new ArrayList<>();

    	applications.forEach(app -> routes.addAll(eventRouteService.getAll(tenant, app).getResult()));

        return new ModelAndView("routes/index","routes", routes);
    }

    @RequestMapping("new")
    @PreAuthorize("hasAuthority('CREATE_DEVICE_ROUTE')")
    public ModelAndView newRoute() {
        return new ModelAndView("routes/form")
            .addObject("route",new EventRouteForm())
            .addObject("action",MessageFormat.format("/routes/{0}/save", application.getName()));
    }

    @RequestMapping(path = "/{applicationName}/save", method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('CREATE_DEVICE_ROUTE')")
    public ModelAndView save(@PathVariable("applicationName") String applicationName,
    						 @ModelAttribute("eventRouteForm") EventRouteForm eventRouteForm,
                             RedirectAttributes redirectAttributes, Locale locale) {

    	application = applicationService.getByApplicationName(tenant, applicationName).getResult();

        return doSave(() -> {
            eventRouteForm.setAdditionalSupplier(() -> tenant.getDomainName());
            return eventRouteService.save(tenant, application, eventRouteForm.toModel());
        },eventRouteForm,locale,redirectAttributes, "");

    }

    @RequestMapping(path = "/{applicationName}/{routeGUID}", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('SHOW_DEVICE_ROUTE')")
    public ModelAndView show(@PathVariable("applicationName") String applicationName, @PathVariable("routeGUID") String routeGUID) {
    	application = applicationService.getByApplicationName(tenant, applicationName).getResult();
        return new ModelAndView("routes/show","route",new EventRouteForm().fillFrom(eventRouteService.getByGUID(tenant, application, routeGUID).getResult()));
    }

    @RequestMapping("/{applicationName}/{routeId}/edit")
    @PreAuthorize("hasAuthority('EDIT_DEVICE_ROUTE')")
    public ModelAndView edit(@PathVariable("applicationName") String applicationName, @PathVariable String routeId) {
    	application = applicationService.getByApplicationName(tenant, applicationName).getResult();

        return new ModelAndView("routes/form")
            .addObject("route",new EventRouteForm().fillFrom(eventRouteService.getByGUID(tenant, application, routeId).getResult()))
            .addObject("action", MessageFormat.format("/routes/{0}/{1}", applicationName, routeId))
            .addObject("method", "put");
    }

    @RequestMapping(path = "/{applicationName}/{routeGUID}", method = RequestMethod.PUT)
    @PreAuthorize("hasAuthority('EDIT_DEVICE_ROUTE')")
    public ModelAndView saveEdit(@PathVariable("applicationName") String applicationName,
    							 @PathVariable String routeGUID,
                                 @ModelAttribute("eventRouteForm") EventRouteForm eventRouteForm,
                                 RedirectAttributes redirectAttributes, Locale locale) {
    	application = applicationService.getByApplicationName(tenant, applicationName).getResult();

        return doSave(() -> {
            eventRouteForm.setAdditionalSupplier(() -> tenant.getDomainName());
            return eventRouteService.update(tenant, application, routeGUID, eventRouteForm.toModel());
        },eventRouteForm,locale,redirectAttributes,"put");

    }

    @RequestMapping("/outgoing/{outgoingScheme}")
    public ModelAndView outgoingFragment(@PathVariable String outgoingScheme) {
        EventRouteForm route = new EventRouteForm();
        switch (outgoingScheme) {
            case "device": return new ModelAndView("routes/device-outgoing", "route", route);
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
                return new ModelAndView(MessageFormat.format("redirect:/routes/{0}/{1}",
                		response.getResult().getApplication().getName(),
                        response.getResult().getGuid()));
            }
        }
    }

    @RequestMapping(path = "/{applicationName}/{routeGUID}", method = RequestMethod.DELETE)
    @PreAuthorize("hasAuthority('REMOVE_DEVICE_ROUTE')")
    public ModelAndView remove(@PathVariable("applicationName") String applicationName,
    						   @PathVariable("routeGUID") String routeGUID,
                               RedirectAttributes redirectAttributes, Locale locale) {
    	application = applicationService.getByApplicationName(tenant, applicationName).getResult();
        ServiceResponse<EventRoute> serviceResponse = eventRouteService.remove(tenant, application, routeGUID);

        if (serviceResponse.isOk()) {
        	redirectAttributes.addFlashAttribute("message",
        			applicationContext.getMessage(Messages.ROUTE_REMOVED_SUCCESSFULLY.getCode(),null,locale));
        } else {
        	List<String> messages = serviceResponse.getResponseMessages()
        			.entrySet()
        			.stream()
        			.map(message -> applicationContext.getMessage(message.getKey(), message.getValue(), locale))
        			.collect(Collectors.toList());
        	redirectAttributes.addFlashAttribute("errors", messages);
        }


        return new ModelAndView("redirect:/routes");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
