package com.konkerlabs.platform.registry.web.controllers;

import com.konkerlabs.platform.registry.business.model.SmsDestination;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.NewServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.SmsDestinationService;
import com.konkerlabs.platform.registry.web.forms.SmsDestinationForm;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.text.MessageFormat.format;

@Controller
@Scope("request")
@RequestMapping("destinations/sms")
public class SmsDestinationController implements ApplicationContextAware {

    public enum Messages {
        SMSDEST_REGISTERED_SUCCESSFULLY("controller.smsdest.registered.successfully");

        private String code;

        public String getCode() {
            return code;
        }

        Messages(String code) {
            this.code = code;
        }
    }

    
    @Autowired
    private Tenant tenant;
    @Autowired
    private SmsDestinationService destinationService;
    
    private ApplicationContext applicationContext;


    @RequestMapping
    public ModelAndView index() {
        return new ModelAndView("destinations/sms/index")
                .addObject("allDestinations", destinationService.findAll(tenant).getResult());
    }

    @RequestMapping("new")
    public ModelAndView newDestination() {
        return new ModelAndView("destinations/sms/form")
                .addObject("destination", new SmsDestinationForm())
                .addObject("action", "/destinations/sms/save");
    }

    @RequestMapping(value = "save", method = RequestMethod.POST)
    public ModelAndView saveNew(@ModelAttribute("destinationForm") SmsDestinationForm destinationForm,
                                RedirectAttributes redirectAttributes, Locale locale) {
        return doSave(
                () -> destinationService.register(tenant,destinationForm.toModel()),
                destinationForm, locale,
                redirectAttributes, "");
    }

    @RequestMapping(value = "/{guid}", method = RequestMethod.GET)
    public ModelAndView show(@PathVariable("guid") String guid) {
        return new ModelAndView("destinations/sms/show")
                .addObject("destination",new SmsDestinationForm()
                        .fillFrom(destinationService.getByGUID(tenant,guid).getResult()));
    }

    @RequestMapping("/{guid}/edit")
    public ModelAndView edit(@PathVariable("guid") String guid) {
        return new ModelAndView("destinations/sms/form")
                .addObject("destination",new SmsDestinationForm()
                        .fillFrom(destinationService.getByGUID(tenant,guid).getResult()))
                .addObject("action",format("/destinations/sms/{0}",guid))
                .addObject("method", "put");
    }

    @RequestMapping(value = "/{guid}", method = RequestMethod.PUT)
    public ModelAndView saveEdit(@PathVariable String guid,
                                 @ModelAttribute("destinationForm") SmsDestinationForm destinationForm,
                                 RedirectAttributes redirectAttributes, Locale locale) {
        return doSave(
                () -> destinationService.update(tenant,guid,destinationForm.toModel()),
                destinationForm, locale,
                redirectAttributes, "put");
    }

    private ModelAndView doSave(Supplier<NewServiceResponse<SmsDestination>> responseSupplier,
                                SmsDestinationForm destinationForm, Locale locale,
                                RedirectAttributes redirectAttributes, String method) {

        NewServiceResponse<SmsDestination> serviceResponse = responseSupplier.get();

        switch (serviceResponse.getStatus()) {
            case ERROR: {
                return new ModelAndView("destinations/sms/form")
                        .addObject("errors", serviceResponse.getResponseMessages().entrySet().stream().map(message -> applicationContext.getMessage(message.getKey(), message.getValue(), locale)).collect(Collectors.toList()))
                        .addObject("method", method)
                        .addObject("destination", destinationForm);
            }
            default: {
                redirectAttributes.addFlashAttribute("message", applicationContext.getMessage(SmsDestinationController.Messages.SMSDEST_REGISTERED_SUCCESSFULLY.getCode(),null,locale));
                return new ModelAndView(
                        format("redirect:/destinations/sms/{0}", serviceResponse.getResult().getGuid())
                );
            }
        }
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
