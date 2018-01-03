package com.konkerlabs.platform.registry.web.controllers;

import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.services.api.*;
import com.konkerlabs.platform.registry.config.AmazonConfig;
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
    private DeviceModelService deviceModelService;
    @Autowired
    private LocationSearchService locationSearchService;
    @Autowired
    private AmazonConfig amazonConfig;
    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private Tenant tenant;
    @Autowired
    private Application application;
    private ApplicationContext applicationContext;

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
        EventRouteForm routeForm = new EventRouteForm();
        routeForm.setApplicationName(application.getName());

        ModelAndView model = new ModelAndView("routes/form")
            .addObject("route", routeForm)
            .addObject("kinesisRouteEnabled", amazonConfig.isKinesisRouteEnabled())
            .addObject("action", MessageFormat.format("/routes/{0}/save", application.getName()));

        return addCombos(tenant, application, model);
    }

    @RequestMapping(path = "/{applicationName}/save", method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('CREATE_DEVICE_ROUTE')")
    public ModelAndView save(@PathVariable("applicationName") String applicationName,
    						 @ModelAttribute("eventRouteForm") EventRouteForm eventRouteForm,
                             RedirectAttributes redirectAttributes, Locale locale) {

        Application application = applicationService.getByApplicationName(tenant, applicationName).getResult();

        return doSave(tenant,
                      application,
                      () -> {
                            eventRouteForm.setAdditionalSupplier(() -> tenant.getDomainName());
                            return eventRouteService.save(tenant, application, eventRouteForm.toModel());
                      },
                      eventRouteForm,
                      locale,
                      redirectAttributes,
                      "");

    }

    @RequestMapping(path = "/{applicationName}/{routeGUID}", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('SHOW_DEVICE_ROUTE')")
    public ModelAndView show(@PathVariable("applicationName") String applicationName, @PathVariable("routeGUID") String routeGUID) {
        Application application = applicationService.getByApplicationName(tenant, applicationName).getResult();
        ModelAndView model = new ModelAndView(
                        "routes/show",
                        "route", new EventRouteForm().fillFrom(eventRouteService.getByGUID(tenant, application, routeGUID).getResult())
                    )
                .addObject("kinesisRouteEnabled", amazonConfig.isKinesisRouteEnabled());

        return addCombos(tenant, application, model);
    }

    private ModelAndView addCombos(Tenant tenant, Application application, ModelAndView model) {

        model.addObject("allDevices", deviceRegisterService.findAll(tenant, application).getResult());
        model.addObject("allRestDestinations", restDestinationService.findAll(tenant, application).getResult());
        model.addObject("allDeviceModels", deviceModelService.findAll(tenant, application).getResult());
        model.addObject("allLocations", locationSearchService.findAll(tenant, application).getResult());
        model.addObject("allTransformations", transformationService.getAll(tenant, application).getResult());
        model.addObject("applicationName", application.getName());

        return model;

    }

    @RequestMapping("/{applicationName}/{routeGUID}/edit")
    @PreAuthorize("hasAuthority('EDIT_DEVICE_ROUTE')")
    public ModelAndView edit(@PathVariable("applicationName") String applicationName, @PathVariable String routeGUID) {
        Application application = applicationService.getByApplicationName(tenant, applicationName).getResult();

        ModelAndView model = new ModelAndView("routes/form")
            .addObject("route",new EventRouteForm().fillFrom(eventRouteService.getByGUID(tenant, application, routeGUID).getResult()))
            .addObject("kinesisRouteEnabled", amazonConfig.isKinesisRouteEnabled())
            .addObject("action", MessageFormat.format("/routes/{0}/{1}", applicationName, routeGUID))
            .addObject("method", "put");

        return addCombos(tenant, application, model);
    }

    @RequestMapping(path = "/{applicationName}/{routeGUID}", method = RequestMethod.PUT)
    @PreAuthorize("hasAuthority('EDIT_DEVICE_ROUTE')")
    public ModelAndView saveEdit(@PathVariable("applicationName") String applicationName,
    							 @PathVariable String routeGUID,
                                 @ModelAttribute("eventRouteForm") EventRouteForm eventRouteForm,
                                 RedirectAttributes redirectAttributes, Locale locale) {
        Application application = applicationService.getByApplicationName(tenant, applicationName).getResult();

        return doSave(tenant,
                      application,
                      () -> {
                            eventRouteForm.setAdditionalSupplier(() -> tenant.getDomainName());
                            return eventRouteService.update(tenant, application, routeGUID, eventRouteForm.toModel());
                      },
                      eventRouteForm,
                      locale,
                      redirectAttributes,
                      "put");

    }

    @RequestMapping("/{applicationName}/incoming/{incomingScheme}")
    public ModelAndView incomingFragment(@PathVariable("applicationName") String applicationName,
                                         @PathVariable String incomingScheme) {
        Application application = applicationService.getByApplicationName(tenant, applicationName).getResult();

        EventRouteForm route = new EventRouteForm();
        ModelAndView model;

        switch (incomingScheme) {
            case Device.URI_SCHEME:
                model = new ModelAndView("routes/device-incoming", "route", route);
                break;
            case DeviceModelLocation.URI_SCHEME :
                model = new ModelAndView("routes/model-location-incoming", "route", route);
                break;
            case Application.URI_SCHEME:
            	model = new ModelAndView("routes/application-incoming", "route", route);
            	break;
            default:
                return new ModelAndView("common/empty");
        }

        return addCombos(tenant, application, model);
    }

    @RequestMapping("/{applicationName}/outgoing/{outgoingScheme}")
    public ModelAndView outgoingFragment(@PathVariable("applicationName") String applicationName,
                                         @PathVariable String outgoingScheme) {
        Application application = applicationService.getByApplicationName(tenant, applicationName).getResult();

        EventRouteForm route = new EventRouteForm();
        ModelAndView model;

        switch (outgoingScheme) {
            case Device.URI_SCHEME:
                model = new ModelAndView("routes/device-outgoing", "route", route);
                break;
            case DeviceModelLocation.URI_SCHEME :
                model = new ModelAndView("routes/model-location-outgoing", "route", route);
                break;
            case RestDestination.URI_SCHEME:
                model = new ModelAndView("routes/rest-outgoing", "route", route);
                break;
            case AmazonKinesis.URI_SCHEME:
                model = new ModelAndView("routes/amazon-kinesis-outgoing", "route", route);
                break;
            default:
                return new ModelAndView("common/empty");
        }

        return addCombos(tenant, application, model);
    }

    private ModelAndView doSave(Tenant tenant,
                                Application application,
                                Supplier<ServiceResponse<EventRoute>> responseSupplier,
                                EventRouteForm eventRouteForm,
                                Locale locale,
                                RedirectAttributes redirectAttributes,
                                String method) {
        ServiceResponse<EventRoute> response = responseSupplier.get();

        switch (response.getStatus()) {
            case ERROR: {
                ModelAndView model = new ModelAndView("routes/form")
                        .addObject("errors",
                            response.getResponseMessages().entrySet().stream().map(message -> applicationContext.getMessage(message.getKey(), message.getValue(), locale)).collect(Collectors.toList())
                        )
                        .addObject("route", eventRouteForm)
                        .addObject("method", method);

                return addCombos(tenant, application, model);
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
    	Application application = applicationService.getByApplicationName(tenant, applicationName).getResult();
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
