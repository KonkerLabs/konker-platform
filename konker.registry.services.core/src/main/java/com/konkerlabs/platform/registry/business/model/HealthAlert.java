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

    public static enum Solution {

        MESSAGE_RECEIVED("Device received a message"),
        ALERT_DELETED("Alert deleted"),
        TRIGGER_DELETED("Alert trigger deleted");

        private String message;

        public String getMessage() {
            return message;
        }

        Solution(String message) {
            this.message = message;
        }

    }

	@Id
	private String id;
	private String guid;
	private HealthAlertSeverity severity;
	private String description;
	private Instant registrationDate;
	private Instant lastChange;
	private HealthAlertType type;
	private String triggerGuid;
	private String deviceGuid;
	private Solution solution;
	private boolean solved;

	@DBRef
    private Tenant tenant;
    @DBRef
    private Application application;


	@Override
	public String getUriScheme() {
		return URI_SCHEME;
	}

	@Override
	public String getContext() {
		return getTenant() != null ? getTenant().getDomainName() : null;
	}

	public enum HealthAlertSeverity {
		OK(3),
		WARN(2),
		FAIL(1);

		private Integer prior;

		HealthAlertSeverity(Integer prior) {
			this.prior = prior;
		}

		public Integer getPrior() {
			return prior;
		}

	}

	public enum HealthAlertType {
		SILENCE;
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

		if (getDescription() == null || getDescription().isEmpty())
			validations.put(Validations.DESCRIPTION_NULL_EMPTY.code,null);
		if (getSeverity() == null)
			validations.put(Validations.SEVERITY_NULL.code,null);
		if (getType() == null)
			validations.put(Validations.TYPE_NULL.code,null);

		return Optional.of(validations).filter(map -> !map.isEmpty());
	}

}
