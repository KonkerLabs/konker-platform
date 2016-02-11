package com.konkerlabs.platform.registry.web.forms;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.EventRule;
import com.konkerlabs.platform.registry.web.forms.api.ModelBuilder;
import lombok.Data;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

@Data
public class EventRuleForm implements ModelBuilder<EventRule,EventRuleForm> {

    private String id;
    private String name;
    private String description;
    private String incomingAuthority;
    private String incomingChannel;
    private String outgoingScheme;
    private String outgoingDeviceAuthority;
    private String outgoingDeviceChannel;
    private String outgoingSmsPhoneNumber;
    private String filterClause;
    private boolean active;

    @Override
    public EventRule toModel() throws BusinessException {
        EventRule rule = null;

        try {
            EventRule.RuleTransformation contentFilterTransformation = new EventRule.RuleTransformation("CONTENT_MATCH");
            contentFilterTransformation.getData().put("value",getFilterClause());

            rule = EventRule.builder()
                    .id(id)
                    .name(getName())
                    .description(getDescription())
                    .incoming(new EventRule.RuleActor(new URI("device",getIncomingAuthority(),null,null,null)))
                    .outgoing(new EventRule.RuleActor(buildOutgoingURI()))
                    .transformations(Arrays.asList(new EventRule.RuleTransformation[]{contentFilterTransformation}))
                    .active(isActive())
                    .build();
            applyIncomingMetadata(rule);
            applyOutgoingMetadata(rule);
        } catch (URISyntaxException e) {
            throw new BusinessException("Failed to build model instance based on web form", e);
        }

        return rule;
    }

    private URI buildOutgoingURI() throws URISyntaxException {
        switch (getOutgoingScheme()) {
            case "device" : return new URI(getOutgoingScheme(),getOutgoingDeviceAuthority(),null,null,null);
            case "sms" : return new URI(getOutgoingScheme(),getOutgoingSmsPhoneNumber(),null,null,null);
            default: return new URI(null,null,null,null,null);
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
