package com.konkerlabs.platform.registry.test.web.forms;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.EventRule;
import com.konkerlabs.platform.registry.web.forms.EventRuleForm;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class EventRuleFormTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private EventRuleForm form;
    private EventRule model;

    @Before
    public void setUp() {
        form = new EventRuleForm();
        form.setName("rule_name");
        form.setDescription("rule_description");
        form.setIncomingAuthority("0000000000000004");
        form.setIncomingChannel("command");
        form.setFilterClause("LEDSwitch");
        form.setActive(true);

        model = EventRule.builder()
                .name(form.getName())
                .description(form.getDescription())
                .transformation(new EventRule.RuleTransformation("EXPRESSION_LANGUAGE"))
                .active(form.isActive()).build();
        model.getTransformations().get(0).getData().put("value",form.getFilterClause());
    }

//    @Test
//    public void shouldRaiseAnExceptionIfOutgoingSchemeIsNull() throws Exception {
//        form.setOutgoingScheme(null);
//
//        thrown.expect(BusinessException.class);
//        thrown.expectMessage("Please choose an outgoing rule type");
//
//        assertThat(form.toModel(),equalTo(model));
//    }

//    @Test
//    public void shouldRaiseAnExceptionIfOutgoingSchemeIsEmpty() throws Exception {
//        form.setOutgoingScheme("");
//
//        thrown.expect(BusinessException.class);
//        thrown.expectMessage("Please choose an outgoing rule type");
//
//        assertThat(form.toModel(),equalTo(model));
//    }

    @Test
    public void shouldTranslateFromDeviceRuleFormToModel() throws Exception {
        form.setOutgoingScheme("device");
        form.setOutgoingDeviceAuthority("0000000000000005");
        form.setOutgoingDeviceChannel("in");

        model.setIncoming(new EventRule.RuleActor(
                new URI("device",form.getIncomingAuthority(),null,null,null))
        );
        model.getIncoming().getData().put("channel",form.getIncomingChannel());
        model.setOutgoing(new EventRule.RuleActor(new URI(
            form.getOutgoingScheme(),form.getOutgoingDeviceAuthority(),null,null,null
        )));
        model.getOutgoing().getData().put("channel",form.getOutgoingDeviceChannel());

        assertThat(form.toModel(),equalTo(model));
    }
    @Test
    public void shouldTranslateFromSMSRuleFormToModel() throws Exception {
        form.setOutgoingScheme("sms");
        form.setOutgoingSmsPhoneNumber("+5511987654321");

        model.setIncoming(new EventRule.RuleActor(
                new URI("device",form.getIncomingAuthority(),null,null,null))
        );
        model.getIncoming().getData().put("channel",form.getIncomingChannel());
        model.setOutgoing(new EventRule.RuleActor(new URI(
                form.getOutgoingScheme(),form.getOutgoingSmsPhoneNumber(),null,null,null
        )));

        assertThat(form.toModel(),equalTo(model));
    }
    @Test
    public void shouldTranslateFromDeviceRuleModelFromForm() throws Exception {
        form.setOutgoingScheme("device");
        form.setOutgoingDeviceAuthority("0000000000000005");
        form.setOutgoingDeviceChannel("in");

        model.setIncoming(new EventRule.RuleActor(
                new URI("device",form.getIncomingAuthority(),null,null,null))
        );
        model.getIncoming().getData().put("channel",form.getIncomingChannel());
        model.setOutgoing(new EventRule.RuleActor(new URI(
                form.getOutgoingScheme(),form.getOutgoingDeviceAuthority(),null,null,null
        )));
        model.getOutgoing().getData().put("channel",form.getOutgoingDeviceChannel());

        assertThat(new EventRuleForm().fillFrom(model),equalTo(form));
    }
    @Test
    public void shouldTranslateFromSMSRuleModelFromForm() throws Exception {
        form.setOutgoingScheme("sms");
        form.setOutgoingSmsPhoneNumber("+5511987654321");

        model.setIncoming(new EventRule.RuleActor(
                new URI("device",form.getIncomingAuthority(),null,null,null))
        );
        model.getIncoming().getData().put("channel",form.getIncomingChannel());
        model.setOutgoing(new EventRule.RuleActor(new URI(
                form.getOutgoingScheme(),form.getOutgoingSmsPhoneNumber(),null,null,null
        )));

        assertThat(new EventRuleForm().fillFrom(model),equalTo(form));
    }
}