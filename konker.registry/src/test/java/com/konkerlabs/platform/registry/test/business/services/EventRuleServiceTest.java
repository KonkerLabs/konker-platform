package com.konkerlabs.platform.registry.test.business.services;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.EventRule;
import com.konkerlabs.platform.registry.business.repositories.EventRuleRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.rules.EventRuleExecutorImpl;
import com.konkerlabs.platform.registry.business.services.rules.api.EventRuleService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.test.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoTestConfiguration.class, BusinessTestConfiguration.class })
public class EventRuleServiceTest extends BusinessLayerTestSupport {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private EventRuleService subject;

    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private EventRuleRepository eventRuleRepository;

    private EventRule rule;

    private String ruleId = "71fb0d48-674b-4f64-a3e5-0256ff3a63af";

    @Before
    public void setUp() throws Exception {
        rule = spy(EventRule.builder()
                .name("Rule name")
                .description("Description")
                .incoming(new EventRule.RuleActor(new URI("device","0000000000000004",null,null,null)))
                .outgoing(new EventRule.RuleActor(new URI("device","0000000000000005",null,null,null)))
                .transformations(Arrays.asList(new EventRule.RuleTransformation[]{
                        new EventRule.RuleTransformation(EventRuleExecutorImpl.RuleTransformationType.EXPRESSION_LANGUAGE.name())
                }))
                .active(true)
                .build());
    }
    @Test
    public void shouldRaiseAnExceptionIfRecordIsNull() throws Exception {
        thrown.expect(BusinessException.class);
        thrown.expectMessage("Record cannot be null");

        subject.save(null);
    }
    @Test
    @UsingDataSet(locations = "/fixtures/tenants.json")
    public void shouldReturnResponseMessagesIfRecordIsInvalid() throws Exception {
        List<String> errorMessages = Arrays.asList(new String[] { "Some error" });
        when(rule.applyValidations()).thenReturn(errorMessages);

        ServiceResponse response = subject.save(rule);

        assertThat(response, notNullValue());
        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.ERROR));
        assertThat(response.getResponseMessages(), equalTo(errorMessages));
    }
    @Test
    @UsingDataSet(locations = "/fixtures/empty-tenants.json")
    public void shouldReturnResponseMessagesIfDefaultTenantDoesNotExist() throws Exception {
        List<String> errorMessages = Arrays.asList(new String[] { "Default tenant does not exist" });

        ServiceResponse response = subject.save(rule);

        assertThat(response, notNullValue());
        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.ERROR));
        assertThat(response.getResponseMessages(), equalTo(errorMessages));
    }
    @Test
    @UsingDataSet(locations = "/fixtures/tenants.json")
    public void shouldPersistIfRuleIsValid() throws Exception {
        ServiceResponse response = subject.save(rule);

        assertThat(response, notNullValue());
        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.OK));
        assertThat(eventRuleRepository.findByIncomingURI(rule.getIncoming().getUri()), notNullValue());
    }
    @Test
    @UsingDataSet(locations = "/fixtures/tenants.json")
    public void shouldReturnAValidationMessageIfIncomingAndOutgoingChannelsAreTheSame() throws Exception {
        String channel = "channel";

        rule.getIncoming().getData().put("channel",channel);
        rule.getOutgoing().getData().put("channel",channel);

        List<String> errorMessages = Arrays.asList(new String[] { "Incoming and outgoing device channels cannot be the same" });
        ServiceResponse response = subject.save(rule);

        assertThat(response, notNullValue());
        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.ERROR));
        assertThat(response.getResponseMessages(), equalTo(errorMessages));
    }
    @Test
    @UsingDataSet(locations = "/fixtures/event-rules.json")
    public void shouldReturnAllRegisteredRules() throws Exception {
        List<EventRule> allRules = subject.getAll();

        assertThat(allRules, notNullValue());
        assertThat(allRules, hasSize(4));
        assertThat(allRules.get(0).getId(), equalTo("71fb0d48-674b-4f64-a3e5-0256ff3a63af"));
        assertThat(allRules.get(1).getId(), equalTo("71fb0d48-674b-4f64-a3e5-0256ff3a63ab"));
        assertThat(allRules.get(2).getId(), equalTo("71fb0d48-674b-4f64-a3e5-0256ff3a63ac"));
        assertThat(allRules.get(3).getId(), equalTo("71fb0d48-674b-4f64-a3e5-0256ff3a63ad"));
    }
    @Test
    @UsingDataSet(locations = "/fixtures/event-rules.json")
    public void shouldReturnARegisteredRuleByItsID() throws Exception {
        EventRule rule = subject.findById(ruleId);

        assertThat(rule, notNullValue());
    }
    @Test
    @UsingDataSet(locations = "/fixtures/event-rules.json")
    public void shouldReturnARegisteredRuleByItsIncomingUri() throws Exception {
        List<EventRule> rules = subject.findByIncomingUri(this.rule.getIncoming().getUri());

        assertThat(rules, notNullValue());
        assertThat(rules, hasSize(3));
        assertThat(rules.get(0).getId(), equalTo("71fb0d48-674b-4f64-a3e5-0256ff3a63af"));
        assertThat(rules.get(1).getId(), equalTo("71fb0d48-674b-4f64-a3e5-0256ff3a63ab"));
        assertThat(rules.get(2).getId(), equalTo("71fb0d48-674b-4f64-a3e5-0256ff3a63ac"));
    }
    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json","/fixtures/event-rules.json"})
    public void shouldSaveEditedRuleState() throws Exception {
        EventRule rule = subject.findById(ruleId);

        String editedName = "Edited name";
        rule.setName(editedName);
        rule.setActive(false);

        ServiceResponse response = subject.save(rule);

        assertThat(response, notNullValue());
        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.OK));
        assertThat(EventRule.class.cast(response.getResult()).getName(),equalTo(editedName));
        assertThat(EventRule.class.cast(response.getResult()).isActive(),equalTo(false));
    }
}