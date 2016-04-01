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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@Scope("request")
@RequestMapping("enrichment")
public class EnrichmentController {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnrichmentController.class);

    @Autowired
    private DeviceRegisterService deviceRegisterService;
    @Autowired
    private DataEnrichmentExtensionService dataEnrichmentExtensionService;
    @Autowired
    private Tenant tenant;

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
                             RedirectAttributes redirectAttributes) {

        enrichmentForm.setTenantDomainSupplier(() -> tenant.getDomainName());
        ServiceResponse<DataEnrichmentExtension> response = dataEnrichmentExtensionService.register(tenant, enrichmentForm.toModel());

        switch (response.getStatus()) {
            case ERROR: {
                return new ModelAndView("enrichment/form")
                        .addObject("errors", response.getResponseMessages())
                        .addObject("method","")
                        .addObject("dataEnrichmentExtension", enrichmentForm);
            }
            default: {
                redirectAttributes.addFlashAttribute("message", "Enrichment registered successfully");
                return new ModelAndView(MessageFormat.format("redirect:/enrichment/{0}",
                        response.getResult().getGuid()));
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
                                 RedirectAttributes redirectAttributes) {

        enrichmentForm.setTenantDomainSupplier(() -> tenant.getDomainName());
        ServiceResponse<DataEnrichmentExtension> response = dataEnrichmentExtensionService.update(tenant, dataEnrichmentExtensionGUID, enrichmentForm.toModel());

        switch (response.getStatus()) {
            case ERROR: {
                return new ModelAndView("enrichment/form")
                        .addObject("errors", response.getResponseMessages())
                        .addObject("method","put")
                        .addObject("dataEnrichmentExtension", enrichmentForm);
            }
            default: {
                redirectAttributes.addFlashAttribute("message", "Enrichment updated successfully");
                return new ModelAndView(MessageFormat.format("redirect:/enrichment/{0}",
                        response.getResult().getGuid()));
            }
        }
    }

    @RequestMapping(path = "/{dataEnrichmentExtensionGUID}", method = RequestMethod.DELETE)
    public ModelAndView remove(@PathVariable("dataEnrichmentExtensionGUID") String dataEnrichmentExtensionGUID,
                               RedirectAttributes redirectAttributes) {
        ServiceResponse<DataEnrichmentExtension> serviceResponse = dataEnrichmentExtensionService.remove(tenant, dataEnrichmentExtensionGUID);

        redirectAttributes.addFlashAttribute("message",
                MessageFormat.format("Enrichment {0} was successfully removed", serviceResponse.getResult().getName()));

        return new ModelAndView("redirect:/enrichment");
    }
}
