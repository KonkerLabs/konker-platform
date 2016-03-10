package com.konkerlabs.platform.registry.business.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Document(collection = "eventRoutes")
@Data
@Builder
public class EventRoute {

    @Id
    private String id;
    @DBRef
    private Tenant tenant;
    private String name;
    private String description;
    private RuleActor incoming;
    private RuleActor outgoing;
    private String filteringExpression;
    @DBRef
    private Transformation transformation;
//    @Singular
//    private List<RuleTransformation> transformations = new ArrayList<>();
    private boolean active;

    public List<String> applyValidations() {
        List<String> validations = new ArrayList<>();

        if (getTenant()==null)
            validations.add("Tenant cannot be null");
        if (getName()==null || getName().isEmpty())
            validations.add("Name cannot be null or empty");
        if (getIncoming()==null)
            validations.add("Incoming actor cannot be null");
        if (getIncoming() != null && getIncoming().getUri() == null)
            validations.add("Incoming actor URI cannot be null");
        if (getIncoming() != null &&
                getIncoming().getUri() != null &&
                    getIncoming().getUri().toString().isEmpty())
            validations.add("Incoming actor's URI cannot be empty");
        if (getOutgoing()==null)
            validations.add("Outgoing actor cannot be null");
        if (getOutgoing() != null && getOutgoing().getUri() == null)
            validations.add("Outgoing actor URI cannot be null");
        if (getOutgoing() != null &&
                getOutgoing().getUri() != null &&
                getOutgoing().getUri().toString().isEmpty())
            validations.add("Outgoing actor's URI cannot be empty");

        if (validations.isEmpty())
            return null;
        else
            return validations;
    }

    @Data
    public static class RuleActor {
        private URI uri;
        private Map<String,String> data = new HashMap<>();

        public RuleActor(URI uri) {
            this.uri = uri;
        }
    }

//    @Data
//    public static class RuleTransformation {
//        private String type;
//        private Map<String,String> data = new HashMap<>();
//
//        public RuleTransformation(String type) {
//            this.type = type;
//        }
//    }
}
