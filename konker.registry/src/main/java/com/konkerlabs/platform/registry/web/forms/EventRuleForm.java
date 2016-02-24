package com.konkerlabs.platform.registry.web.forms;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.EventRule;
import com.konkerlabs.platform.registry.business.services.rules.EventRuleExecutorImpl;
import com.konkerlabs.platform.registry.web.forms.api.ModelBuilder;
import lombok.Data;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.Arrays;

@Data
public class EventRuleForm implements ModelBuilder<EventRule,EventRuleForm> {

    private static final String URI_TEMPLATE = "{0}://{1}";

    private String id;
    private String name;
    private String description;
    private String incomingAuthority;
    private String incomingChannel;
    private String outgoingScheme = "device";
    private String outgoingDeviceAuthority;
    private String outgoingDeviceChannel;
    private String outgoingSmsPhoneNumber;
    private String filterClause;
    private boolean active;

    @Override
    public EventRule toModel() {
        EventRule rule = null;

//        if (getOutgoingScheme() == null || getOutgoingScheme().isEmpty())
//            throw new BusinessException("Please choose an outgoing rule type");

//        try {
            EventRule.RuleTransformation contentFilterTransformation = new EventRule.RuleTransformation(EventRuleExecutorImpl.RuleTransformationType.EXPRESSION_LANGUAGE.name());
            contentFilterTransformation.getData().put("value",getFilterClause());

            rule = EventRule.builder()
                    .id(id)
                    .name(getName())
                    .description(getDescription())
                    .incoming(new EventRule.RuleActor(
                        URI.create(MessageFormat.format(URI_TEMPLATE,"device",getIncomingAuthority())
                    )))
                    .outgoing(new EventRule.RuleActor(buildOutgoingURI()))
                    .transformations(Arrays.asList(new EventRule.RuleTransformation[]{contentFilterTransformation}))
                    .active(isActive())
                    .build();
            applyIncomingMetadata(rule);
            applyOutgoingMetadata(rule);
//        } catch (URISyntaxException e) {
//            throw new BusinessException("Failed to build model instance based on web form", e);
//        }

        return rule;
    }

    private URI buildOutgoingURI() {
        switch (getOutgoingScheme()) {
            case "device" : return URI.create(
                MessageFormat.format(URI_TEMPLATE,getOutgoingScheme(),getOutgoingDeviceAuthority())
            );
            case "sms" : return URI.create(
                MessageFormat.format(URI_TEMPLATE,getOutgoingScheme(),getOutgoingSmsPhoneNumber())
            );
            default: return null;
        }
    }

    private void applyIncomingMetadata(EventRule rule) {
        rule.getIncoming().getData().put("channel",getIncomingChannel());
    }

    private void applyOutgoingMetadata(EventRule rule) {
        switch (getOutgoingScheme()) {
            case "device" : {
                rule.getOutgoing().getData().put("channel", getOutgoingDeviceChannel());
                break;
            }
            default: break;
        }
    }

    @Override
    public EventRuleForm fillFrom(EventRule model) {
        this.setId(model.getId());
        this.setName(model.getName());
        this.setDescription(model.getDescription());
        this.setIncomingAuthority(model.getIncoming().getUri().getAuthority());
        this.setIncomingChannel(model.getIncoming().getData().get("channel"));
        this.setOutgoingScheme(model.getOutgoing().getUri().getScheme());
        switch (getOutgoingScheme()) {
            case "device" : {
                this.setOutgoingDeviceAuthority(model.getOutgoing().getUri().getAuthority());
                this.setOutgoingDeviceChannel(model.getOutgoing().getData().get("channel"));
                break;
            }
            case "sms" : {
                this.setOutgoingSmsPhoneNumber(model.getOutgoing().getUri().getAuthority());
                break;
            }
            default: break;
        }
        this.setFilterClause(model.getTransformations().get(0).getData().get("value"));
        this.setActive(model.isActive());
        return this;
    }
}
