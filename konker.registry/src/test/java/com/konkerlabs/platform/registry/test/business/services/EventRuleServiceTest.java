package com.konkerlabs.platform.registry.test.business.services;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.EventRule;
import com.konkerlabs.platform.registry.business.repositories.EventRuleRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.EventRuleService;
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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
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

    @Before
    public void setUp() throws Exception {
        rule = spy(EventRule.builder()
                .name("Rule name")
                .description("Description")
                .incoming(new EventRule.RuleActor(new URI("device://0000000000000004/")))
                .outgoing(new EventRule.RuleActor(new URI("device://0000000000000005/")))
                .transformations(Arrays.asList(new EventRule.RuleTransformation[]{
                        new EventRule.RuleTransformation("CONTENT_MATCH")
                }))
                .active(true)
                .build());
    }
    @Test
    public void shouldRaiseAnExceptionIfRecordIsNull() throws Exception {
        thrown.expect(BusinessException.class);
        thrown.expectMessage("Record cannot be null");

        subject.create(null);
    }
    @Test
    @UsingDataSet(locations = "/fixtures/tenants.json")
    public void shouldReturnResponseMessagesIfRecordIsInvalid() throws Exception {
        List<String> errorMessages = Arrays.asList(new String[] { "Some error" });
        when(rule.applyValidations()).thenReturn(errorMessages);

        ServiceResponse response = subject.create(rule);

        assertThat(response, notNullValue());
        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.ERROR));
        assertThat(response.getResponseMessages(), equalTo(errorMessages));
    }
    @Test
    @UsingDataSet(locations = "/fixtures/empty-tenants.json")
    public void shouldReturnResponseMessagesIfDefaultTenantDoesNotExist() throws Exception {
        List<String> errorMessages = Arrays.asList(new String[] { "Default tenant does not exist" });

        ServiceResponse response = subject.create(rule);

        assertThat(response, notNullValue());
        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.ERROR));
        assertThat(response.getResponseMessages(), equalTo(errorMessages));
    }
    @Test
    @UsingDataSet(locations = "/fixtures/tenants.json")
    public void shouldPersistIfDeviceIsValid() throws Exception {
        ServiceResponse response = subject.create(rule);

        assertThat(response, notNullValue());
        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.OK));
        assertThat(eventRuleRepository.findByIncomingURI(rule.getIncoming().getUri()), notNullValue());
    }
    @Test
    @UsingDataSet(locations = "/fixtures/event-rules.json")
    public void shouldReturnAllRegisteredDevices() throws Exception {
        List<EventRule> allRules = subject.getAll();

        assertThat(allRules, notNullValue());
        assertThat(allRules, hasSize(1));
    }
}
