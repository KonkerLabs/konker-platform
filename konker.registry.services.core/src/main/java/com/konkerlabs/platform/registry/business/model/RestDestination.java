package com.konkerlabs.platform.registry.business.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.konkerlabs.platform.registry.business.model.behaviors.URIDealer;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.utilities.validations.InterpolableURIValidator;
import com.konkerlabs.platform.utilities.validations.api.Validatable;
import com.mongodb.util.JSON;
import com.mongodb.util.JSONParseException;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@Document(collection = "restDestinations")
public class RestDestination implements URIDealer, Validatable {

    public enum Validations {
        NAME_NULL("model.rest_destination.name.not_null"),
        URL_NULL("model.rest_destination.url.not_null"),
        GUID_NOT_EMPTY("model.rest_destination.guid.not_empty"),
        SERVICE_USERNAME_WITHOUT_PASSWORD("model.rest_destination.service.user_without_password"),
        CUSTOM_BODY_INVALID("service.rest_destination.custom.body.invalid");

        private String code;

        public String getCode() {
            return code;
        }

        Validations(String code) {
            this.code = code;
        }
    }
    
    public enum RestDestinationType {
    	FORWARD_MESSAGE,
    	CUSTOM_BODY;
    }

    @Id
    private String id;
    @DBRef
    private Tenant tenant;
    @DBRef
    private Application application;
    private String guid;
    private String name;
    private String method;
    private Map<String, String> headers;
    private String serviceURI;
    private String serviceUsername;
    private String servicePassword;
    private RestDestinationType type;
    private String body;
    private boolean active;

    public static final String URI_SCHEME = "rest";

    @Override
    public String getUriScheme() {
        return URI_SCHEME;
    }

    @Override
    public String getContext() {
        return getTenant() != null ? getTenant().getDomainName() : null;
    }

    @Override
    public String getGuid() {
        return guid;
    }

    public Optional<Map<String, Object[]>> applyValidations() {
        Map<String, Object[]> validations = new HashMap<>();

        if (tenant == null) {
            validations.put(CommonValidations.TENANT_NULL.getCode(), null);
        }

        if ("".equals(Optional.ofNullable(getName()).orElse("").trim())) {
            validations.put(Validations.NAME_NULL.getCode(), null);
        }

        if ("".equals(Optional.ofNullable(getServiceURI()).orElse(""))) {
            validations.put(Validations.URL_NULL.getCode(), null);
        } else {
            InterpolableURIValidator.to(getServiceURI())
                    .applyValidations()
                    .ifPresent(violations -> {
                        validations.putAll(violations);
                    });
        }

        if (Optional.ofNullable(getServicePassword()).filter(s -> !s.isEmpty()).isPresent()) {
            if ("".equals(Optional.ofNullable(getServiceUsername()).orElse("").trim())) {
                validations.put(Validations.SERVICE_USERNAME_WITHOUT_PASSWORD.getCode(), null);
            }
        }

        if (!Optional.ofNullable(getGuid()).filter(s -> !s.isEmpty()).isPresent())
            validations.put(Validations.GUID_NOT_EMPTY.getCode(), null);
        
        if (RestDestinationType.CUSTOM_BODY.equals(getType()) &&
				isInvalidJson(getBody())) {
        	validations.put(Validations.CUSTOM_BODY_INVALID.getCode(), null);
        }

        return Optional.of(validations).filter(stringMap -> !stringMap.isEmpty());
    }
    
    private boolean isInvalidJson(String body) {
		if (StringUtils.isBlank(body)) {
            return true;
        }
		
		try {
            JSON.parse(body);
        } catch (JSONParseException e) {
            return true;
        }
		
		return false;
	}

    @Data
    @NoArgsConstructor
    public static class RestDestinationHeader {

    	private String key;

    	private String value;

    }

}
