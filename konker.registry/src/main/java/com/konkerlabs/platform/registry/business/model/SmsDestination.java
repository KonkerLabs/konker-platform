package com.konkerlabs.platform.registry.business.model;

import com.konkerlabs.platform.registry.business.model.behaviors.SmsDestinationURIDealer;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@Data
@Builder
@Document(collection = "smsDestinations")
public class SmsDestination implements SmsDestinationURIDealer {

    private static final Pattern PATTERN_INTERNACIONAL_PHONE_NUMBER = Pattern.compile("^\\+(?:[0-9] ?){6,14}[0-9]$");

    
    public enum Validations {
        GUID_NULL_EMPTY("model.smsdest.id.not_null"),
        NAME_NULL_EMPTY("model.smsdest.name.not_null"),
        PHONE_NULL_EMPTY("model.smsdest.phone.not_null"),
        PHONE_FORMAT_INVALID("model.smsdest.phone.format_invalid")
        ;

        public String getCode() {
            return code;
        }

        private String code;

        Validations(String code) {
            this.code = code;
        }
    }

    
    @Id
    private String id;
    @DBRef
    private Tenant tenant;
    private String name;
    private String description;
    private String phoneNumber;
    private boolean active;
    private String guid;

    public Optional<Map<String, Object[]>> applyValidations() {
        Map<String, Object[]> validations = new HashMap<>();

        if (!Optional.ofNullable(getTenant()).isPresent())
            validations.put(CommonValidations.TENANT_NULL.getCode(), null);
        if (!Optional.ofNullable(getName()).filter(s -> !s.isEmpty()).isPresent())
            validations.put(Validations.NAME_NULL_EMPTY.code, null);
        if (!Optional.ofNullable(getPhoneNumber()).filter(s -> !s.isEmpty()).isPresent())
            validations.put(Validations.PHONE_NULL_EMPTY.code, null);
        
        Optional.ofNullable(getPhoneNumber())
            .ifPresent(phone -> {
                if (!PATTERN_INTERNACIONAL_PHONE_NUMBER.matcher(phone).matches())
                    validations.put(Validations.PHONE_FORMAT_INVALID .code, null);
            });

        if (!Optional.ofNullable(getGuid())
                .filter(s -> !s.isEmpty()).isPresent()) {
            validations.put(Validations.GUID_NULL_EMPTY.code, null);
        }

        return Optional.of(validations).filter(map -> !map.isEmpty());
    }

    public URI toURI() {
        return toSmsURI(Optional.ofNullable(getTenant()).orElse(Tenant.builder().build()).getDomainName(),getGuid());
    }
}
