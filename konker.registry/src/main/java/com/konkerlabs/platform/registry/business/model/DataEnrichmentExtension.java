package com.konkerlabs.platform.registry.business.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.konkerlabs.platform.registry.business.model.enumerations.IntegrationType;
import com.konkerlabs.platform.utilities.validations.InterpolableURIValidationUtil;
import com.konkerlabs.platform.utilities.validations.ValidationException;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

@Document(collection = "dataEnrichmentExtensions")
@Data
@Builder
public class DataEnrichmentExtension {

    public static final String URL = "URL";
    public static final String USERNAME = "User";
    public static final String PASSWORD = "Password";
    
    @Id
    private String id;
    @DBRef
    private Tenant tenant;
    @Indexed(unique = true)
    private String guid;
    @Indexed(unique = true)
    private String name;
    private IntegrationType type;
    private String description;

    private URI incoming;
    private String containerKey;

    @Singular
    private Map<String, String> parameters = new HashMap<>();

    private boolean active;

    public List<String> applyValidations() {
        List<String> validations = new ArrayList<>();

        if (getTenant() == null)
            validations.add("Tenant cannot be null");
        
        if (getName() == null || getName().isEmpty())
            validations.add("Name cannot be null or empty");
        
        if (getIncoming() == null)
            validations.add("Incoming device cannot be null");

        if (!"device".equals(Optional.ofNullable(getIncoming()).orElse(URI.create("")).getScheme()))
            validations.add("Incoming must be a device");

        if (getType() == null)
            validations.add("Data enrichment type cannot be null");

        if (getParameters() == null)
            validations.add("Parameters cannot be null");

        Optional.ofNullable(getParameters())
            .ifPresent(map -> {
                if ("".equals(Optional.ofNullable(getParameters().get(URL)).orElse(""))) {
                    validations.add("Service URL cannot be null or empty");
                } else {
                    try {
                        InterpolableURIValidationUtil.validate(getParameters().get(URL));
                    } catch (ValidationException ve) {
                        validations.add(ve.getMessage());
                    }
                }

                if (Optional.ofNullable(getParameters().get(PASSWORD)).filter(s -> !s.isEmpty()).isPresent()) {
                    if ("".equals(Optional.ofNullable(getParameters().get(USERNAME)).orElse("").trim())) {
                        validations.add("Password is set but username is empty");
                    }
                }
            });

        if (getContainerKey() == null || getContainerKey().isEmpty())
            validations.add("Container key cannot be null or empty");

        return Optional.of(validations)
            .filter(strings -> !strings.isEmpty())
            .orElseGet(ArrayList<String>::new);
    }
}
