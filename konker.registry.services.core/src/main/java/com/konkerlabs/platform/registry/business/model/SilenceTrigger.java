package com.konkerlabs.platform.registry.business.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.konkerlabs.platform.registry.business.model.HealthAlert.HealthAlertType;

import lombok.Data;

@Data
public class SilenceTrigger extends AlertTrigger {

    public static final String URI_SCHEME = "silencetrigger";

    private int minutes;

    public enum Validations {
        INVALID_MINUTES_VALUE("model.silence_trigger.invalid_minutes_value");

        public String getCode() {
            return code;
        }

        private String code;

        Validations(String code) {
            this.code = code;
        }
    }

    public SilenceTrigger() {
        super.type = HealthAlertType.SILENCE;
    }

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

    public Optional<Map<String, Object[]>> applyValidations(int minimumMinutes) {

        Map<String, Object[]> validations = new HashMap<>();

        if (getMinutes() < minimumMinutes) {
            validations.put(Validations.INVALID_MINUTES_VALUE.getCode(), new Object[] {minimumMinutes});
        }

        return Optional.of(validations).filter(map -> !map.isEmpty());
    }

}
