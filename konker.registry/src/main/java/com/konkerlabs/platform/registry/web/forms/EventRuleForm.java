package com.konkerlabs.platform.registry.web.forms;

import lombok.Data;

@Data
public class EventRuleForm {

    private String name;
    private String description;
    private String incomingAuthority;
    private String outgoingAuthority;
    private String filterClause;
    private boolean active;

}
