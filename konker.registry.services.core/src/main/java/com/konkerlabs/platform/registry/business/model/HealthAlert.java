package com.konkerlabs.platform.registry.business.model;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

	@Id
	private String id;
    private String alertId; // used for custom alerts
	private String guid;
	private HealthAlertSeverity severity;
	private String description;
	private Instant registrationDate;
	private Instant lastChange;
	private AlertTrigger.AlertTriggerType type;
	private String triggerGuid;
	private String deviceGuid;
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

	public enum HealthAlertSeverity {

		OK(3, "model.healthalert.severity.ok"),
		WARN(2, "model.healthalert.severity.warn"),
		FAIL(1, "model.healthalert.severity.fail");

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

	}

	public enum Validations {
		DESCRIPTION_NULL_EMPTY("model.healthalert.description.not_null"),
		SEVERITY_NULL("model.healthalert.severity.not_null"),
		TYPE_NULL("model.healthalert.type.not_null");

		public String getCode() {
			return code;
		}

		private String code;

		Validations(String code) {
			this.code = code;
		}
	}

	public Optional<Map<String, Object[]>> applyValidations() {
		Map<String, Object[]> validations = new HashMap<>();

		if (getDescription() == null)
			validations.put(Validations.DESCRIPTION_NULL_EMPTY.code,null);
		if (getSeverity() == null)
			validations.put(Validations.SEVERITY_NULL.code,null);
		if (getType() == null)
			validations.put(Validations.TYPE_NULL.code,null);

		return Optional.of(validations).filter(map -> !map.isEmpty());
	}

}
