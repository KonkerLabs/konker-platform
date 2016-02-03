package com.konkerlabs.platform.registry.business.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Document(collection = "eventRules")
@Data
@Builder
public class EventRule {

    private String id;
    @DBRef
    private Tenant tenant;
    private String name;
    private String description;
    private RuleActor incoming;
    private RuleActor outgoing;
    private List<RuleTransformation> transformations = new ArrayList<>();
    private boolean active;

    @Data
    public static class RuleActor {
        private URI uri;
        private Map<String,String> data = new HashMap<>();
    }

    @Data
    public static class RuleTransformation {
        private String type;
        private Map<String,String> data = new HashMap<>();
    }
}
