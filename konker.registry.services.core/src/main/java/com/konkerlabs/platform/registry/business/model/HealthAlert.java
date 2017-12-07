package com.konkerlabs.platform.registry.business.model;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.konkerlabs.platform.registry.business.model.behaviors.URIDealer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@Document(collection="healthAlerts")
@AllArgsConstructor
@NoArgsConstructor
public class HealthAlert implements URIDealer {

	public static final String URI_SCHEME = "healthAlert";

    public enum Solution {

        MESSAGE_RECEIVED("model.healthalert.solution.message_received"),
        ALERT_DELETED("model.healthalert.solution.alert_deleted"),
        TRIGGER_DELETED("model.healthalert.solution.trigger_deleted");

        private String code;

        public String getCode() {
            return code;
        }

        Solution(String code) {
            this.code = code;
        }

    }

    public enum Description {

    	NO_MESSAGED_RECEIVED("model.healthalert.description.no_message_received"),
        DEVICE_HEALTH_OK("model.healthalert.description.health_ok");

        private String code;

        public String getCode() {
            return code;
        }

        Description(String code) {
            this.code = code;
        }

        public static Description fromCode(String code) {
            for (Description type: Description.values()) {
                if (type.getCode().equalsIgnoreCase(code)) {
                    return type;
                }
            }

            return null;
        }

    }


    public enum HealthAlertSeverity {

        FAIL(1, "model.healthalert.severity.fail"),
        WARN(2, "model.healthalert.severity.warn"),
        OK(3, "model.healthalert.severity.ok"),
        DISABLED(4, "model.healthalert.severity.disabled"),
        UNKNOWN(5, "model.healthalert.severity.unknown"),
        NODATA(6, "model.healthalert.severity.no_data");

        private Integer prior;
        private String code;

        HealthAlertSeverity(Integer prior, String code) {
            this.prior = prior;
            this.code  = code;
        }

        public Integer getPrior() {
            return prior;
        }

        public String getCode() {
            return code;
        }

        public static HealthAlertSeverity fromString(String name) {
            for (HealthAlertSeverity type: HealthAlertSeverity.values()) {
                if (type.name().equalsIgnoreCase(name)) {
                    return type;
                }
            }

            return null;
        }

    }

    public enum Validations {
        DESCRIPTION_NULL_EMPTY("model.healthalert.description.not_null"),
        SEVERITY_NULL("model.healthalert.severity.not_null"),
        TYPE_NULL("model.healthalert.type.not_null"),
        ALERT_ID_NULL("model.healthalert.alert_id.not_null");

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
    private String alertId; // used for custom alerts
	private String guid;
	private HealthAlertSeverity severity;
	private String description;
	private Instant registrationDate;
	private Instant lastChange;
	private Solution solution;
	private boolean solved;

	@DBRef
    private Tenant tenant;
    @DBRef
    private Application application;
    @DBRef
    private AlertTrigger alertTrigger;
    @DBRef
    private Device device;

	@Override
	public String getUriScheme() {
		return URI_SCHEME;
	}

	@Override
	public String getContext() {
		return getTenant() != null ? getTenant().getDomainName() : null;
	}

	public Optional<Map<String, Object[]>> applyValidations() {
		Map<String, Object[]> validations = new HashMap<>();

        Pattern regex = Pattern.compile("[a-zA-Z0-9\u00C0-\u00FF .\\-+_]{2,100}");

        if (StringUtils.isBlank(getAlertId()) || !regex.matcher(getAlertId()).matches()) {
            validations.put(Validations.ALERT_ID_NULL.code, null);
        }
		if (getSeverity() == null) {
            validations.put(Validations.SEVERITY_NULL.code, null);
        }

		return Optional.of(validations).filter(map -> !map.isEmpty());
	}

}
