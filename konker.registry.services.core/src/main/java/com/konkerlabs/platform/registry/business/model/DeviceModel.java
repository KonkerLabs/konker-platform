package com.konkerlabs.platform.registry.business.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.konkerlabs.platform.registry.business.model.behaviors.URIDealer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@Document(collection="devicesModel")
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"guid", "name", "description", "defaultModel"})
public class DeviceModel implements URIDealer {

	@Id
	private String id;
	private String guid;
	private String name;
	private String description;
	private boolean defaultModel;
	
	@DBRef
	private Tenant tenant;
	
	@DBRef
    private Application application;

	public static final String URI_SCHEME = "deviceModel";

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
		NAME_NULL_EMPTY("model.devicemodel.name.not_null"),
		NAME_INVALID("model.devicemodel.name.invalid");

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

		if (getName() == null || getName().isEmpty()) {
			validations.put(Validations.NAME_NULL_EMPTY.code,null);
		}
		if (getName() != null && !regex.matcher(getName()).matches()) {
			validations.put(Validations.NAME_INVALID.code,null);
		}
		

		return Optional.of(validations).filter(map -> !map.isEmpty());
	}



}
