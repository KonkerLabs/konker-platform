package com.konkerlabs.platform.registry.web.controllers;

import com.konkerlabs.platform.registry.business.model.DataEnrichmentExtension;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.enumerations.IntegrationType;
import com.konkerlabs.platform.registry.business.services.api.DataEnrichmentExtensionService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.web.forms.EnrichmentForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@Scope("request")
@RequestMapping("enrichment")
public class EnrichmentController implements ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnrichmentController.class);

    public enum Messages {
        ENRICHMENT_REGISTERED_SUCCESSFULLY("controller.enrichment.registered.successfully"),
        ENRICHMENT_REMOVED_SUCCESSFULLY("controller.enrichment.removed.successfully");

        private String code;

        public String getCode() {
            return code;
        }

        Messages(String code) {
            this.code = code;
        }
    }

    @Autowired
    private DeviceRegisterService deviceRegisterService;
    @Autowired
    private DataEnrichmentExtensionService dataEnrichmentExtensionService;
    @Autowired
    private Tenant tenant;
    private ApplicationContext applicationContext;

    @ModelAttribute("allDevices")
    public List<Device> allDevices() {
        return deviceRegisterService.findAll(tenant).getResult();
    }

    @ModelAttribute("enrichmentTypes")
    public Set<String> enrichmentTypes() {
        Set<String> enrichmentTypes = new HashSet<>();
        for (IntegrationType enrichmentType : IntegrationType.values()) {
            enrichmentTypes.add(enrichmentType.name());
        }
        return enrichmentTypes;
    }

    @RequestMapping
    public ModelAndView index() {

        return new ModelAndView("enrichment/index", "dataEnrichmentExtensions", dataEnrichmentExtensionService.getAll(tenant).getResult());
    }

    @RequestMapping("new")
    public ModelAndView newDataEnrichmentExtension() {
        return new ModelAndView("enrichment/form")
                .addObject("dataEnrichmentExtension", new EnrichmentForm())
                .addObject("action", "/enrichment/save");
    }

    @RequestMapping(value = "save", method = RequestMethod.POST)
    public ModelAndView save(@ModelAttribute("dataEnrichmentExtension") EnrichmentForm enrichmentForm,
                             RedirectAttributes redirectAttributes, Locale locale) {

        enrichmentForm.setTenantDomainSupplier(() -> tenant.getDomainName());
        ServiceResponse<DataEnrichmentExtension> serviceResponse = dataEnrichmentExtensionService.register(tenant, enrichmentForm.toModel());

        switch (serviceResponse.getStatus()) {
            case ERROR: {
                List<String> messages = new ArrayList<>();
                for (Map.Entry<String, Object[]> message : serviceResponse.getResponseMessages().entrySet()) {
                    messages.add(applicationContext.getMessage(message.getKey(),message.getValue(),locale));
                }
                return new ModelAndView("enrichment/form")
                        .addObject("errors", messages)
                        .addObject("method","")
                        .addObject("dataEnrichmentExtension", enrichmentForm);
            }
            default: {
                redirectAttributes.addFlashAttribute("message",
                    applicationContext.getMessage(Messages.ENRICHMENT_REGISTERED_SUCCESSFULLY.getCode(),null,locale)
                );
                return new ModelAndView(MessageFormat.format("redirect:/enrichment/{0}",
                        serviceResponse.getResult().getGuid()));
            }
        }
    }

    @RequestMapping(value = "/{dataEnrichmentExtensionGUID}", method = RequestMethod.GET)
    public ModelAndView show(@PathVariable("dataEnrichmentExtensionGUID") String dataEnrichmentExtensionGUID) {
        return new ModelAndView("enrichment/show","dataEnrichmentExtension",new EnrichmentForm().fillFrom(dataEnrichmentExtensionService.getByGUID(tenant, dataEnrichmentExtensionGUID).getResult()));
    }

    @RequestMapping("/{dataEnrichmentExtensionGUID}/edit")
    public ModelAndView edit(@PathVariable("dataEnrichmentExtensionGUID") String dataEnrichmentExtensionGUID) {
        return new ModelAndView("enrichment/form")
                .addObject("dataEnrichmentExtension",new EnrichmentForm().fillFrom(dataEnrichmentExtensionService.getByGUID(tenant, dataEnrichmentExtensionGUID).getResult()))
                .addObject("action", MessageFormat.format("/enrichment/{0}",dataEnrichmentExtensionGUID))
                .addObject("method","put");
    }

    @RequestMapping(path = "/{dataEnrichmentExtensionGUID}", method = RequestMethod.PUT)
    public ModelAndView saveEdit(@PathVariable String dataEnrichmentExtensionGUID,
                                 @ModelAttribute("enrichmentForm") EnrichmentForm enrichmentForm,
                                 RedirectAttributes redirectAttributes, Locale locale) {

        enrichmentForm.setTenantDomainSupplier(() -> tenant.getDomainName());
        ServiceResponse<DataEnrichmentExtension> serviceResponse = dataEnrichmentExtensionService.update(tenant, dataEnrichmentExtensionGUID, enrichmentForm.toModel());

        switch (serviceResponse.getStatus()) {
            case ERROR: {
                return new ModelAndView("enrichment/form")
                        .addObject("errors",
                            serviceResponse.getResponseMessages().entrySet().stream().map(message -> applicationContext.getMessage(message.getKey(), message.getValue(), locale)).collect(Collectors.toList()))
                        .addObject("method","put")
                        .addObject("dataEnrichmentExtension", enrichmentForm);
            }
            default: {
                redirectAttributes.addFlashAttribute("message",
                    applicationContext.getMessage(Messages.ENRICHMENT_REGISTERED_SUCCESSFULLY.getCode(),null,locale)
                );
                return new ModelAndView(MessageFormat.format("redirect:/enrichment/{0}",
                        serviceResponse.getResult().getGuid()));
            }
        }
    }

    @RequestMapping(path = "/{dataEnrichmentExtensionGUID}", method = RequestMethod.DELETE)
    public ModelAndView remove(@PathVariable("dataEnrichmentExtensionGUID") String dataEnrichmentExtensionGUID,
                               RedirectAttributes redirectAttributes, Locale locale) {
        ServiceResponse<DataEnrichmentExtension> serviceResponse = dataEnrichmentExtensionService.remove(tenant, dataEnrichmentExtensionGUID);

        redirectAttributes.addFlashAttribute("message",
            applicationContext.getMessage(Messages.ENRICHMENT_REMOVED_SUCCESSFULLY.getCode(),null,locale)
        );


        return new ModelAndView("redirect:/enrichment");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
