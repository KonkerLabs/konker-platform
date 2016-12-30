package com.konkerlabs.platform.registry.business.model;

import com.konkerlabs.platform.registry.business.model.behaviors.URIDealer;
import com.konkerlabs.platform.registry.business.model.enumerations.IntegrationType;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.utilities.validations.InterpolableURIValidator;
import com.konkerlabs.platform.utilities.validations.api.Validatable;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Document(collection = "dataEnrichmentExtensions")
@Data
@Builder
public class DataEnrichmentExtension implements URIDealer, Validatable {

    public static final String URL = "URL";
    public static final String USERNAME = "User";
    public static final String PASSWORD = "Password";
    public static final String URI_SCHEME = "dataenrichmentextension";

    public enum Validations {
        NAME_NULL("model.enrichment.name.not_null"),
        INCOMING_DEVICE_NULL("model.enrichment.incoming_device.not_null"),
        INCOMING_URI_NOT_A_DEVICE("model.enrichment.incoming_uri.not_a_device"),
        TYPE_NULL("model.enrichment.type.not_null"),
        PARAMETERS_NULL("model.enrichment.parameters.not_null"),
        CONTAINER_KEY_NOT_EMPTY("model.enrichment.container_key.not_empty"),
        SERVICE_URL_MISSING("model.enrichment.service_url.missing"),
        SERVICE_USERNAME_MISSING("model.enrichment.service_username.missing"),
        SERVICE_PASSWORD_MISSING("model.enrichment.service_password.missing"),
        SERVICE_USERNAME_WITHOUT_PASSWORD("model.enrichment.service.user_without_password");

        private String code;

        public String getCode() {
            return code;
        }

        Validations(String code) {
            this.code = code;
        }
    }
    
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
    private String incomingDisplayName;
    private String containerKey;

    @Singular
    private Map<String, String> parameters = new HashMap<>();

    @Override
    public String getUriScheme() {
        return URI_SCHEME;
    }

    @Override
    public String getContext() {
        return getTenant() != null ? getTenant().getDomainName() : null;
    }

    private boolean active;

    public Optional<Map<String,Object[]>> applyValidations() {
        Map<String,Object[]> validations = new HashMap<>();

        if (getTenant() == null)
            validations.put(CommonValidations.TENANT_NULL.getCode(),null);
        
        if (getName() == null || getName().isEmpty())
            validations.put(Validations.NAME_NULL.getCode(),null);
        
        if (getIncoming() == null)
            validations.put(Validations.INCOMING_DEVICE_NULL.getCode(),null);

        if (!"device".equals(Optional.ofNullable(getIncoming()).orElse(URI.create("")).getScheme()))
            validations.put(Validations.INCOMING_URI_NOT_A_DEVICE.getCode(),null);

        if (getType() == null)
            validations.put(Validations.TYPE_NULL.getCode(),null);

        if (getParameters() == null)
            validations.put(Validations.PARAMETERS_NULL.getCode(),null);

        Optional.ofNullable(getParameters()).filter(params -> !params.isEmpty())
            .ifPresent(attr -> {
                if (!attr.containsKey(URL) || !Optional.ofNullable(attr.get(URL)).filter(s -> !s.isEmpty()).isPresent()) {
                    validations.put(Validations.SERVICE_URL_MISSING.getCode(),null);
                } else {
                    InterpolableURIValidator.to(attr.get(URL))
                            .applyValidations()
                            .ifPresent(stringMap -> {
                                validations.putAll(stringMap);
                            });
                }
                if (!attr.containsKey(USERNAME))
                    validations.put(Validations.SERVICE_USERNAME_MISSING.getCode(),null);
                if (!attr.containsKey(PASSWORD))
                    validations.put(Validations.SERVICE_PASSWORD_MISSING.getCode(),null);

                if (Optional.ofNullable(getParameters().get(PASSWORD)).filter(s -> !s.isEmpty()).isPresent()) {
                    if ("".equals(Optional.ofNullable(getParameters().get(USERNAME)).orElse("").trim())) {
                        validations.put(Validations.SERVICE_USERNAME_WITHOUT_PASSWORD.getCode(),null);
                    }
                }
            });

        if (getContainerKey() == null || getContainerKey().isEmpty())
            validations.put(Validations.CONTAINER_KEY_NOT_EMPTY.getCode(),null);

        return Optional.of(validations).filter(map -> !map.isEmpty());
    }
}
