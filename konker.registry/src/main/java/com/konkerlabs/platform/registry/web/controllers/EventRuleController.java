package com.konkerlabs.platform.registry.web.controllers;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.EventRule;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.rules.api.EventRuleService;
import com.konkerlabs.platform.registry.web.forms.EventRuleForm;
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
import java.util.Arrays;
import java.util.List;

@Controller
@Scope("request")
@RequestMapping("rules")
public class EventRuleController {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventRuleController.class);

    @Autowired
    private EventRuleService eventRuleService;
    @Autowired
    private DeviceRegisterService deviceRegisterService;
    @Autowired
    private Tenant tenant;

    @ModelAttribute("allDevices")
    public List<Device> allDevices() {
        return deviceRegisterService.getAll(tenant);
    }

    @RequestMapping
    public ModelAndView index() {
        return new ModelAndView("rules/index","rules",eventRuleService.getAll(tenant));
    }

    @RequestMapping("new")
    public ModelAndView newRule() {
        return new ModelAndView("rules/form")
            .addObject("rule",new EventRuleForm())
            .addObject("action","/rules/save");
    }

    @RequestMapping(value = "save", method = RequestMethod.POST)
    public ModelAndView save(@ModelAttribute("eventRuleForm") EventRuleForm eventRuleForm,
                             RedirectAttributes redirectAttributes) {
        ServiceResponse<EventRule> response;
        try {
            eventRuleForm.setAdditionalSupplier(() -> tenant.getDomainName());
            response = eventRuleService.save(tenant,eventRuleForm.toModel());
        } catch (BusinessException e) {
            response = ServiceResponse.<EventRule>builder()
                .status(ServiceResponse.Status.ERROR)
                .responseMessages(Arrays.asList(new String[]{e.getMessage()}))
                .<EventRule>build();
        }

        switch (response.getStatus()) {
            case ERROR: {
                return new ModelAndView("rules/form")
                    .addObject("errors",response.getResponseMessages())
                    .addObject("rule",eventRuleForm);
            }
            default: {
                redirectAttributes.addFlashAttribute("message", "Rule registered successfully");
                return new ModelAndView(MessageFormat.format("redirect:/rules/{0}",
                        response.getResult().getId()));
            }
        }
    }

    @RequestMapping(value = "/{ruleId}", method = RequestMethod.GET)
    public ModelAndView show(@PathVariable("ruleId") String ruleId) {
        return new ModelAndView("rules/show","rule",new EventRuleForm().fillFrom(eventRuleService.getById(tenant, ruleId).getResult()));
    }

    @RequestMapping("/{ruleId}/edit")
    public ModelAndView edit(@PathVariable String ruleId) {
        return new ModelAndView("rules/form")
            .addObject("rule",new EventRuleForm().fillFrom(eventRuleService.getById(tenant, ruleId).getResult()))
            .addObject("action", MessageFormat.format("/rules/{0}",ruleId));
    }

    @RequestMapping(path = "/{ruleId}", method = RequestMethod.POST)
    public ModelAndView saveEdit(@PathVariable String ruleId,
                                 @ModelAttribute("eventRuleForm") EventRuleForm eventRuleForm,
                                 RedirectAttributes redirectAttributes) {
        eventRuleForm.setId(ruleId);
        return save(eventRuleForm, redirectAttributes);
    }

    @RequestMapping("/outgoing/{outgoingScheme}")
    public ModelAndView outgoingFragment(@PathVariable String outgoingScheme) {
        EventRuleForm rule = new EventRuleForm();
        switch (outgoingScheme) {
            case "device": return new ModelAndView("rules/device-outgoing","rule",rule);
            case "sms" : return new ModelAndView("rules/sms-outgoing","rule",rule);
            //FIXME: Check for a way to render an empty HTTP body without an empty html file
            default: return new ModelAndView("common/empty");
        }
    }
}
