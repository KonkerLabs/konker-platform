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
@Document(collection="applications")
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"name", "qualifier"})
public class Application implements URIDealer {

	@Id
	private String name;
	private String friendlyName;
	private String description;
    private String dataApiDomain; // subscribe and publish events API domain name
    private String dataMqttDomain; // subscribe and publish events MQTT domain name
	private String qualifier;
	private Instant registrationDate;

	@DBRef
	private Tenant tenant;

	public static final String URI_SCHEME = "application";
    public static final String DEFAULT_QUALIFIER = "brsp01a";

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
		return name;
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
