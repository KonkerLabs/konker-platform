package com.konkerlabs.platform.registry.business.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.konkerlabs.platform.registry.config.HealthAlertsConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.konkerlabs.platform.registry.business.model.behaviors.URIDealer;

import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "alertTriggers")
public class AlertTrigger implements URIDealer {

    public static final String URI_SCHEME = "alertTrigger";

    private int minutes;

    public enum Validations {

        NAME_INVALID("model.alert_trigger.invalid_name"),
        INVALID_TYPE("model.alert_trigger.invalid_type"),
        INVALID_MINUTES_VALUE("model.alert_trigger.invalid_minutes_value");

        public String getCode() {
            return code;
        }

        private String code;

        Validations(String code) {
            this.code = code;
        }
    }

    public enum AlertTriggerType {

        SILENCE, CUSTOM;

        public static AlertTriggerType fromString(String name) {
            for (AlertTriggerType type: AlertTriggerType.values()) {
                if (type.name().equalsIgnoreCase(name)) {
                    return type;
                }
            }

            return null;
        }

    }

    @Id
    private String id;
    protected String guid;
    private String name;
    private String description;
    @DBRef
    private Tenant tenant;
    @DBRef
    private Application application;
    @DBRef
    private DeviceModel deviceModel;
    @DBRef
    private Location location;
    protected AlertTriggerType type;

    @Transient
    private Set<Location> mappedLocations = new HashSet<>();

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

    public Map<String, Object[]> applyValidations() {

        Map<String, Object[]> validations = new HashMap<>();

        Pattern regex = Pattern.compile("[a-zA-Z0-9\u00C0-\u00FF .\\-+_]{2,100}");

        if (StringUtils.isBlank(getName()) || !regex.matcher(getName()).matches()) {
            validations.put(Validations.NAME_INVALID.code, null);
        }
        if (type == null) {
            validations.put(Validations.INVALID_TYPE.code, null);
            return validations;
        }
        if (AlertTriggerType.SILENCE.equals(type)) {
            HealthAlertsConfig healthAlertsConfig = new HealthAlertsConfig();
            if (this.getMinutes() < healthAlertsConfig.getSilenceMinimumMinutes()) {
                validations.put(Validations.INVALID_MINUTES_VALUE.getCode(), new Object[]{healthAlertsConfig.getSilenceMinimumMinutes()});
            }
        }

        return validations;

    }

}
