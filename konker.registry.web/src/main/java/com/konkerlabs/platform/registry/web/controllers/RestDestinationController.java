package com.konkerlabs.platform.registry.web.controllers;

import static java.text.MessageFormat.format;

import java.text.MessageFormat;
import java.util.ArrayList;
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

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.RestDestination;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.RestDestinationService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.web.forms.RestDestinationForm;

@Controller
@Scope("request")
@RequestMapping("destinations/rest")
public class RestDestinationController implements ApplicationContextAware {

    public enum Messages {
        ENRICHMENT_REGISTERED_SUCCESSFULLY("controller.rest_destination.registered.successfully"),
        REST_DESTINATION_REMOVED_SUCCESSFULLY("controller.rest_destination.removed_successfully");

        private String code;

        public String getCode() {
            return code;
        }

        Messages(String code) {
            this.code = code;
        }
    }

    @Autowired
    private RestDestinationService restDestinationService;
    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private Tenant tenant;
    @Autowired
    private Application application;
    private ApplicationContext applicationContext;

    @RequestMapping
    @PreAuthorize("hasAuthority('LIST_REST_DESTINATIONS')")
    public ModelAndView index() {
    	List<Application> applications = applicationService.findAll(tenant).getResult();
    	List<RestDestination> restDestinations = new ArrayList<>();
    	
    	applications.forEach(app -> restDestinations.addAll(restDestinationService.findAll(tenant, app).getResult()));
    	
        return new ModelAndView("destinations/rest/index")
            .addObject("allDestinations", restDestinations);
    }

    @RequestMapping("new")
    @PreAuthorize("hasAuthority('CREATE_REST_DESTINATION')")
    public ModelAndView newDestination() {
        RestDestinationForm destinationForm = new RestDestinationForm();
        destinationForm.setApplicationName(application.getName());
		return new ModelAndView("destinations/rest/form")
                .addObject("destination", destinationForm)
                .addObject("action", MessageFormat.format("/destinations/rest/{0}/save", application.getName()));
    }

    @RequestMapping(path = "/{applicationName}/save", method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('CREATE_REST_DESTINATION')")
    public ModelAndView saveNew(@PathVariable("applicationName") String applicationName,
    							@ModelAttribute("destinationForm") RestDestinationForm destinationForm,
    							RedirectAttributes redirectAttributes, Locale locale) {
    	
    	application = applicationService.getByApplicationName(tenant, applicationName).getResult();
    	
        return doSave(
                () -> restDestinationService.register(tenant, application, destinationForm.toModel()),
                destinationForm, locale,
                redirectAttributes, "");
    }

    @RequestMapping(value = "/{applicationName}/{guid}", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('SHOW_REST_DESTINATION')")
    public ModelAndView show(@PathVariable("applicationName") String applicationName,
    							@PathVariable("guid") String guid) {
    	application = applicationService.getByApplicationName(tenant, applicationName).getResult();
        return new ModelAndView("destinations/rest/show")
            .addObject("destination",new RestDestinationForm()
                    .fillFrom(restDestinationService.getByGUID(tenant, application, guid).getResult()));
    }

    @RequestMapping("/{applicationName}/{guid}/edit")
    @PreAuthorize("hasAuthority('EDIT_REST_DESTINATION')")
    public ModelAndView edit(@PathVariable("applicationName") String applicationName,
    							@PathVariable("guid") String guid) {
    	application = applicationService.getByApplicationName(tenant, applicationName).getResult();
        return new ModelAndView("destinations/rest/form")
                .addObject("destination",new RestDestinationForm()
                        .fillFrom(restDestinationService.getByGUID(tenant, application, guid).getResult()))
                .addObject("action",format("/destinations/rest/{0}/{1}", applicationName, guid))
                .addObject("method", "put");
    }

    @RequestMapping(value = "/{applicationName}/{guid}", method = RequestMethod.PUT)
    @PreAuthorize("hasAuthority('EDIT_REST_DESTINATION')")
    public ModelAndView saveEdit(@PathVariable("applicationName") String applicationName,
    							@PathVariable String guid,
                                @ModelAttribute("destinationForm") RestDestinationForm destinationForm,
                                RedirectAttributes redirectAttributes, Locale locale) {
    	application = applicationService.getByApplicationName(tenant, applicationName).getResult();
        return doSave(
                () -> restDestinationService.update(tenant, application, guid, destinationForm.toModel()),
                destinationForm, locale,
                redirectAttributes, "put");
    }

    private ModelAndView doSave(Supplier<ServiceResponse<RestDestination>> responseSupplier,
                                RestDestinationForm destinationForm, Locale locale,
                                RedirectAttributes redirectAttributes, String method) {

        ServiceResponse<RestDestination> serviceResponse = responseSupplier.get();

        switch (serviceResponse.getStatus()) {
            case ERROR: {
                return new ModelAndView("destinations/rest/form")
                        .addObject("errors",
                            serviceResponse.getResponseMessages().entrySet().stream().map(message -> applicationContext.getMessage(message.getKey(), message.getValue(), locale)).collect(Collectors.toList()))
                        .addObject("method", method)
                        .addObject("destination", destinationForm);
            }
            default: {
                redirectAttributes.addFlashAttribute("message",
                    applicationContext.getMessage(RestDestinationController.Messages.ENRICHMENT_REGISTERED_SUCCESSFULLY.getCode(),null,locale)
                );
                return new ModelAndView(
                        format("redirect:/destinations/rest/{0}/{1}", 
                        		serviceResponse.getResult().getApplication().getName(),
                        		serviceResponse.getResult().getGuid())
                );
            }
        }
    }

    @RequestMapping(path = "/{applicationName}/{guid}", method = RequestMethod.DELETE)
    @PreAuthorize("hasAuthority('REMOVE_REST_DESTINATION')")
    public ModelAndView remove(@PathVariable("applicationName") String applicationName,
    						   @PathVariable String guid,
    						   @ModelAttribute("destinationForm") RestDestinationForm destinationForm,
                               RedirectAttributes redirectAttributes, Locale locale) {
    	
    	application = applicationService.getByApplicationName(tenant, applicationName).getResult();
    	
        ServiceResponse<RestDestination> serviceResponse = restDestinationService.remove(tenant, application, guid);
        if (serviceResponse.isOk()) {
            redirectAttributes.addFlashAttribute("message",
                    applicationContext.getMessage(Messages.REST_DESTINATION_REMOVED_SUCCESSFULLY.getCode(), null, locale)
            );
        } else {
            List<String> messages = serviceResponse.getResponseMessages()
                    .entrySet().stream()
                    .map(message -> applicationContext.getMessage(message.getKey(), message.getValue(), locale))
                    .collect(Collectors.toList());
            redirectAttributes.addFlashAttribute("errors", messages);
        }

        return new ModelAndView("redirect:/destinations/rest");
    }
    
    @RequestMapping("/{applicationName}/body/{restType}")
    public ModelAndView bodyFragment(@PathVariable("applicationName") String applicationName,
                                         @PathVariable String restType) {
        ModelAndView model = null;
        RestDestinationForm destinationForm = new RestDestinationForm();

        if (restType.equals("FORWARD_MESSAGE")) {
        	model = new ModelAndView("destinations/rest/empty-body", "destination", destinationForm);
        } else {
        	model = new ModelAndView("destinations/rest/custom-body", "destination", destinationForm);
        }

        return model;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
