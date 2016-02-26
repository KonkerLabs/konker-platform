package com.konkerlabs.platform.registry.test.web.forms;

import com.konkerlabs.platform.registry.business.model.EventRule;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.behaviors.DeviceURIDealer;
import com.konkerlabs.platform.registry.business.model.behaviors.SmsURIDealer;
import com.konkerlabs.platform.registry.business.services.rules.EventRuleExecutorImpl;
import com.konkerlabs.platform.registry.web.forms.EventRuleForm;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class EventRuleFormTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private EventRuleForm form;
    private EventRule model;
    private DeviceURIDealer deviceUriDealer;
    private SmsURIDealer smsUriDealer;
    private Tenant tenant;

    @Before
    public void setUp() {
        form = new EventRuleForm();
        form.setName("rule_name");
        form.setDescription("rule_description");
        form.setIncomingAuthority("0000000000000004");
        form.setIncomingChannel("command");
        form.setFilterClause("LEDSwitch");
        form.setActive(true);

        tenant = Tenant.builder().name("tenantName").domainName("tenantDomain").build();

        form.setAdditionalSupplier(() -> tenant.getDomainName());

        model = EventRule.builder()
                .name(form.getName())
                .description(form.getDescription())
                .transformation(new EventRule.RuleTransformation(EventRuleExecutorImpl.RuleTransformationType.EXPRESSION_LANGUAGE.name()))
                .active(form.isActive()).build();
        model.getTransformations().get(0).getData().put("value",form.getFilterClause());

        deviceUriDealer = new DeviceURIDealer() {};
        smsUriDealer = new SmsURIDealer() {};
    }

    @Test
    public void shouldRaiseAnExceptionIfTenantDomainNameSupplierIsNull() throws Exception {
        form.setAdditionalSupplier(null);

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Tenant domain name supplier cannot be null");

        form.toModel();
    }

    @Test
    public void shouldRaiseAnExceptionIfTenantDomainNameSupplierReturnsNull() throws Exception {
        form.setAdditionalSupplier(() -> null);

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Tenant domain name supplier cannot return null or empty");

        form.toModel();
    }

    @Test
    public void shouldRaiseAnExceptionIfTenantDomainNameSupplierReturnsAnEmptyString() throws Exception {
        form.setAdditionalSupplier(() -> "");

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Tenant domain name supplier cannot return null or empty");

        form.toModel();
    }

    @Test
    public void shouldTranslateFromDeviceRuleFormToModel() throws Exception {
        form.setOutgoingScheme("device");
        form.setOutgoingDeviceAuthority("0000000000000005");
        form.setOutgoingDeviceChannel("in");

        model.setIncoming(
            new EventRule.RuleActor(deviceUriDealer.toDeviceRuleURI(
                tenant.getDomainName(),form.getIncomingAuthority()
            ))
        );
        model.getIncoming().getData().put("channel",form.getIncomingChannel());
        model.setOutgoing(
            new EventRule.RuleActor(deviceUriDealer.toDeviceRuleURI(
                tenant.getDomainName(),form.getOutgoingDeviceAuthority()
            ))
        );
        model.getOutgoing().getData().put("channel",form.getOutgoingDeviceChannel());

        assertThat(form.toModel(),equalTo(model));
    }
    @Test
    public void shouldTranslateFromSMSRuleFormToModel() throws Exception {
        form.setOutgoingScheme("sms");
        form.setOutgoingSmsPhoneNumber("+5511987654321");

        model.setIncoming(new EventRule.RuleActor(
            deviceUriDealer.toDeviceRuleURI(tenant.getDomainName(),form.getIncomingAuthority())
        ));
        model.getIncoming().getData().put("channel",form.getIncomingChannel());
        model.setOutgoing(new EventRule.RuleActor(
            smsUriDealer.toSmsURI(form.getOutgoingSmsPhoneNumber())
        ));

        assertThat(form.toModel(),equalTo(model));
    }
    @Test
    public void shouldTranslateFromDeviceRuleModelToForm() throws Exception {
        form.setAdditionalSupplier(null);

        form.setOutgoingScheme("device");
        form.setOutgoingDeviceAuthority("0000000000000005");
        form.setOutgoingDeviceChannel("in");

        model.setIncoming(new EventRule.RuleActor(
            deviceUriDealer.toDeviceRuleURI(tenant.getDomainName(),form.getIncomingAuthority())
        ));
        model.getIncoming().getData().put("channel",form.getIncomingChannel());
        model.setOutgoing(new EventRule.RuleActor(
            deviceUriDealer.toDeviceRuleURI(tenant.getDomainName(),form.getOutgoingDeviceAuthority())
        ));
        model.getOutgoing().getData().put("channel",form.getOutgoingDeviceChannel());

        assertThat(new EventRuleForm().fillFrom(model),equalTo(form));
    }
    @Test
    public void shouldTranslateFromSMSRuleModelToForm() throws Exception {
        form.setAdditionalSupplier(null);

        form.setOutgoingScheme("sms");
        form.setOutgoingSmsPhoneNumber("+5511987654321");

        model.setIncoming(new EventRule.RuleActor(
            deviceUriDealer.toDeviceRuleURI(tenant.getDomainName(),form.getIncomingAuthority())
        ));
        model.getIncoming().getData().put("channel",form.getIncomingChannel());
        model.setOutgoing(new EventRule.RuleActor(
            smsUriDealer.toSmsURI(form.getOutgoingSmsPhoneNumber())
        ));

        assertThat(new EventRuleForm().fillFrom(model),equalTo(form));
    }
}