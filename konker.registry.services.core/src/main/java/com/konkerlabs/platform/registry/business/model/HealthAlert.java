package com.konkerlabs.platform.registry.business.model;

import com.konkerlabs.platform.registry.business.model.behaviors.URIDealer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@Data
@Builder
@Document(collection="healthAlerts")
@AllArgsConstructor
@NoArgsConstructor
public class HealthAlert implements URIDealer {

	public static final String URI_SCHEME = "healthAlert";

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
		OK,
		WARN,
		FAIL;
	}
	
	public enum HealthAlertType {
		SILENCE;
	}

	public enum Validations {
		NAME_NULL_EMPTY("model.application.name.not_null"),
		NAME_INVALID("model.application.name.invalid"),
		FRIENDLY_NAME_NULL_EMPTY("model.application.friendly.name.not_null");

		public String getCode() {
			return code;
		}

		private String code;

		Validations(String code) {
			this.code = code;
		}
	}

	public Optional<Map<String, Object[]>> applyValidations() {
		Pattern regex = Pattern.compile("[^a-zA-Z0-9{2,}]");

		Map<String, Object[]> validations = new HashMap<>();

		if (getName() != null && regex.matcher(getName()).find())
			validations.put(Validations.NAME_INVALID.code,null);
		if (getName() == null || getName().isEmpty())
			validations.put(Validations.NAME_NULL_EMPTY.code,null);
		if (getFriendlyName() == null || getFriendlyName().isEmpty())
			validations.put(Validations.FRIENDLY_NAME_NULL_EMPTY.code,null);

		return Optional.of(validations).filter(map -> !map.isEmpty());
	}

}
