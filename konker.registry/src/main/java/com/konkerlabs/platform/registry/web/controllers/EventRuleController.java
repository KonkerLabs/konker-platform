package com.konkerlabs.platform.registry.web.controllers;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.EventRule;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.rules.api.EventRuleService;
import com.konkerlabs.platform.registry.web.forms.EventRuleForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

@Controller
@RequestMapping("rules")
public class EventRuleController {

    @Autowired
    private EventRuleService eventRuleService;

    @RequestMapping
    public ModelAndView index() {
        return new ModelAndView("rules/index","rules",eventRuleService.getAll());
    }

    @RequestMapping("new")
    public ModelAndView newRule() {
        return new ModelAndView("rules/new-form","rule",new EventRuleForm());
    }

    @RequestMapping(value = "save", method = RequestMethod.POST)
    public ModelAndView save(@ModelAttribute("eventRuleForm") EventRuleForm eventRuleForm,
                             RedirectAttributes redirectAttributes) {
        EventRule.RuleTransformation contentFilterTransformation = new EventRule.RuleTransformation("CONTENT_MATCH");
        contentFilterTransformation.getData().put("value",eventRuleForm.getFilterClause());

        ServiceResponse response = null;
        try {
            EventRule rule = EventRule.builder()
                    .name(eventRuleForm.getName())
                    .description(eventRuleForm.getDescription())
                    .incoming(new EventRule.RuleActor(new URI("device",eventRuleForm.getIncomingAuthority(),null,null,null)))
                    .outgoing(new EventRule.RuleActor(new URI("device",eventRuleForm.getOutgoingAuthority(),null,null,null)))
                    .transformations(Arrays.asList(new EventRule.RuleTransformation[]{contentFilterTransformation}))
                    .active(eventRuleForm.isActive())
                    .build();
            response = eventRuleService.create(rule);
        } catch (BusinessException e) {
            response = ServiceResponse.builder()
                .status(ServiceResponse.Status.ERROR)
                .responseMessages(Arrays.asList(new String[]{e.getMessage()}))
                .build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        switch (response.getStatus()) {
            case ERROR: {
                return new ModelAndView("rules/new-form")
                    .addObject("errors",response.getResponseMessages())
                    .addObject("rule",eventRuleForm);
            }
            default: {
                redirectAttributes.addFlashAttribute("message", "Device registered successfully");
                return new ModelAndView("redirect:/rules");
            }
        }
    }

    @RequestMapping("/{ruleId}")
    public ModelAndView show(@PathVariable("ruleId") String ruleId) {
        return new ModelAndView("rules/show","rule",eventRuleService.findById(ruleId));
    }
}
