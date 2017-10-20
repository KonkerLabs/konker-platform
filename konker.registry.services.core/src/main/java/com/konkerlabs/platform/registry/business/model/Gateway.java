package com.konkerlabs.platform.registry.business.model;

import com.konkerlabs.platform.registry.business.model.behaviors.URIDealer;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@Data
@Builder
@Document(collection="gateways")
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"guid", "name", "description"})
public class Gateway implements URIDealer {

	@Id
	private String id;
	private String guid;
	private String name;
	private String description;
	@DBRef
	private Tenant tenant;
	@DBRef
    private Application application;
	@DBRef
	private Location location;
	private boolean active;

	public static final String URI_SCHEME = "gateway";

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

	public enum Validations {
		NAME_NULL_EMPTY("model.gateway.name_not_null"),
		NAME_INVALID("model.gateway.name_invalid"),
        GUID_NULL("model.gateway.guid_null"),
        LOCATION_NULL("model.gateway.location_null");

		public String getCode() {
			return code;
		}

		private String code;

		Validations(String code) {
			this.code = code;
		}
	}

	public Optional<Map<String, Object[]>> applyValidations() {
		Pattern regex = Pattern.compile("[a-zA-Z0-9\u00C0-\u00FF .\\-+_]{2,100}");

		Map<String, Object[]> validations = new HashMap<>();

		if (getTenant() == null) {
			validations.put(CommonValidations.TENANT_NULL.getCode(), null);
		}
		if (getName() == null || getName().isEmpty()) {
			validations.put(Validations.NAME_NULL_EMPTY.code,null);
		}
		if (getName() != null && !regex.matcher(getName()).matches()) {
			validations.put(Validations.NAME_INVALID.code,null);
		}
		if (!Optional.ofNullable(getGuid()).filter(s -> !s.isEmpty()).isPresent()) {
			validations.put(Validations.GUID_NULL.getCode(), null);
		}
        if (getLocation() == null || getLocation().getId() == null) {
            validations.put(Validations.LOCATION_NULL.getCode(), null);
        }

		return Optional.of(validations).filter(map -> !map.isEmpty());
	}

}
