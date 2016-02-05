package com.konkerlabs.platform.registry.test.web.controllers;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.EventRule;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.rules.api.EventRuleService;
import com.konkerlabs.platform.registry.config.WebMvcConfig;
import com.konkerlabs.platform.registry.test.base.WebLayerTestContext;
import com.konkerlabs.platform.registry.web.forms.EventRuleForm;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {WebMvcConfig.class, EventRuleControllerTest.EventRuleTestContextConfig.class})
public class EventRuleControllerTest extends WebLayerTestContext {

    @Autowired
    private EventRuleService eventRuleService;

    private Device incomingDevice;
    private Device outgoingDevice;
    private EventRule rule;
    private List<EventRule> registeredRules;
    private ServiceResponse response;
    private MultiValueMap<String,String> ruleData;
    private EventRuleForm ruleForm;

    private String ruleId = "71fb0d48-674b-4f64-a3e5-0256ff3a63af";

    @Before
    public void setUp() throws Exception {
        incomingDevice = Device.builder().deviceId("0000000000000004").build();
        outgoingDevice = Device.builder().deviceId("0000000000000005").build();

        ruleForm = new EventRuleForm();
        ruleForm.setName("Rule name");
        ruleForm.setDescription("Rule description");
        ruleForm.setIncomingAuthority(incomingDevice.getDeviceId());
        ruleForm.setIncomingChannel("command");
        ruleForm.setOutgoingAuthority(outgoingDevice.getDeviceId());
        ruleForm.setOutgoingChannel("in");
        ruleForm.setFilterClause("LEDSwitch");
        ruleForm.setActive(true);

        ruleData = new LinkedMultiValueMap<>();
        ruleData.add("name",ruleForm.getName());
        ruleData.add("description",ruleForm.getDescription());
        ruleData.add("incomingAuthority",ruleForm.getIncomingAuthority());
        ruleData.add("incomingChannel", ruleForm.getIncomingChannel());
        ruleData.add("outgoingAuthority",ruleForm.getOutgoingAuthority());
        ruleData.add("outgoingChannel", ruleForm.getOutgoingChannel());
        ruleData.add("filterClause",ruleForm.getFilterClause());
        ruleData.add("active","true");

        EventRule.RuleTransformation contentMatchTransformation = new EventRule.RuleTransformation("CONTENT_MATCH");
        contentMatchTransformation.getData().put("value",ruleForm.getFilterClause());

        rule = EventRule.builder()
            .name(ruleForm.getName())
            .description(ruleForm.getDescription())
            .incoming(new EventRule.RuleActor(new URI("device",ruleForm.getIncomingAuthority(),null,null,null)))
            .outgoing(new EventRule.RuleActor(new URI("device",ruleForm.getOutgoingAuthority(),null,null,null)))
            .transformations(Arrays.asList(new EventRule.RuleTransformation[]{contentMatchTransformation}))
            .active(ruleForm.isActive())
            .build();

        rule.getIncoming().getData().put("channel", ruleForm.getIncomingChannel());
        rule.getOutgoing().getData().put("channel", ruleForm.getOutgoingChannel());

        registeredRules = new ArrayList<EventRule>(Arrays.asList(new EventRule[]{rule}));
    }

    @After
    public void tearDown() {
        Mockito.reset(eventRuleService);
    }

    @Test
    public void shouldListAllRegisteredDevices() throws Exception {
        when(eventRuleService.getAll()).thenReturn(registeredRules);

        getMockMvc().perform(get("/rules")).andExpect(model().attribute("rules", equalTo(registeredRules)))
                .andExpect(view().name("rules/index"));
    }

    @Test
    public void shouldShowRegistrationForm() throws Exception {
        getMockMvc().perform(get("/rules/new"))
            .andExpect(view().name("rules/new-form"))
            .andExpect(model().attribute("rule",new EventRuleForm()));
    }

    @Test
    public void shouldBindErrorMessagesWhenRegistrationFailsAndGoBackToRegistrationForm() throws Exception {
        response = ServiceResponse.builder().responseMessages(Arrays.asList(new String[] { "Some error" }))
                .status(ServiceResponse.Status.ERROR).build();

        when(eventRuleService.create(eq(rule))).thenReturn(response);

        getMockMvc().perform(post("/rules/save").params(ruleData))
                .andExpect(model().attribute("errors", equalTo(response.getResponseMessages())))
                .andExpect(model().attribute("rule", equalTo(ruleForm))).andExpect(view().name("rules/new-form"));

        verify(eventRuleService).create(eq(rule));
    }

    @Test
    public void shouldBindBusinessExceptionMessageWhenRegistrationFailsAndGoBackToRegistrationForm() throws Exception {
        String exceptionMessage = "Some business exception message";

        when(eventRuleService.create(eq(rule))).thenThrow(new BusinessException(exceptionMessage));

        getMockMvc().perform(post("/rules/save").params(ruleData))
                .andExpect(model().attribute("errors", equalTo(Arrays.asList(new String[] { exceptionMessage }))))
                .andExpect(model().attribute("rule", equalTo(ruleForm)))
                .andExpect(view().name("rules/new-form"));

        verify(eventRuleService).create(eq(rule));
    }

    @Test
    public void shouldRedirectToShowAfterRuleCreationSucceed() throws Exception {
        response = spy(ServiceResponse.builder()
                .status(ServiceResponse.Status.OK)
                .result(rule)
                .build());

        when(eventRuleService.create(eq(rule))).thenReturn(response);

        getMockMvc().perform(post("/rules/save").params(ruleData))
                .andExpect(flash().attribute("message", "Rule registered successfully"))
                .andExpect(redirectedUrl(MessageFormat.format("/rules/{0}",rule.getId())));

        verify(eventRuleService).create(eq(rule));
    }

    @Test
    public void shouldShowRuleDetails() throws Exception {
        rule.setId(ruleId);
        when(eventRuleService.findById(rule.getId())).thenReturn(rule);

        getMockMvc().perform(
            get("/rules/{0}",rule.getId())
        ).andExpect(model().attribute("rule",rule))
         .andExpect(view().name("rules/show"));

        verify(eventRuleService).findById(rule.getId());
    }

    @Configuration
    static class EventRuleTestContextConfig {
        @Bean
        public EventRuleService eventRuleService() {
            return Mockito.mock(EventRuleService.class);
        }
        @Bean
        public DeviceRegisterService deviceRegisterService() {
            return mock(DeviceRegisterService.class);
        }
    }
}
