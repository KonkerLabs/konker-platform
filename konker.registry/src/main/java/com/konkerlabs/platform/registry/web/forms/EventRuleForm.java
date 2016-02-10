package com.konkerlabs.platform.registry.web.forms;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.EventRule;
import com.konkerlabs.platform.registry.web.forms.api.ModelBuilder;
import lombok.Data;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Optional;

@Data
public class EventRuleForm implements ModelBuilder<EventRule,EventRuleForm> {

    private String id;
    private String name;
    private String description;
    private String incomingAuthority;
    private String incomingChannel;
    private String outgoingAuthority;
    private String outgoingChannel;
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
                    .outgoing(new EventRule.RuleActor(new URI("device",getOutgoingAuthority(),null,null,null)))
                    .transformations(Arrays.asList(new EventRule.RuleTransformation[]{contentFilterTransformation}))
                    .active(isActive())
                    .build();
            rule.getIncoming().getData().put("channel",getIncomingChannel());
            rule.getOutgoing().getData().put("channel",getOutgoingChannel());
        } catch (URISyntaxException e) {
            throw new BusinessException("Failed to build model instance based on web form", e);
        }

        return rule;
    }

    @Override
    public EventRuleForm fillFrom(EventRule model) {
        this.setId(model.getId());
        this.setName(model.getName());
        this.setDescription(model.getDescription());
        this.setIncomingAuthority(model.getIncoming().getUri().getAuthority());
        this.setIncomingChannel(model.getIncoming().getData().get("channel"));
        this.setOutgoingAuthority(model.getOutgoing().getUri().getAuthority());
        this.setOutgoingChannel(model.getOutgoing().getData().get("channel"));
        this.setFilterClause(model.getTransformations().get(0).getData().get("value"));
        this.setActive(model.isActive());
        return this;
    }
}
