package com.konkerlabs.platform.registry.business.model;

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
		NAME_NULL_EMPTY("model.devicemodel.name.not_null");

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

		if (getName() == null || getName().isEmpty())
			validations.put(Validations.NAME_NULL_EMPTY.code,null);

		return Optional.of(validations).filter(map -> !map.isEmpty());
	}



}
