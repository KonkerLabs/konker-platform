package com.konkerlabs.platform.registry.business.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
        super.type = AlertTriggerType.SILENCE;
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

    public Optional<Map<String, Object[]>> applyValidations() {

        Map<String, Object[]> validations = new HashMap<>();

        if (getMinutes() < 10) {
            validations.put(Validations.INVALID_MINUTES_VALUE.getCode(),null);
        }

        return Optional.of(validations).filter(map -> !map.isEmpty());
    }

}
