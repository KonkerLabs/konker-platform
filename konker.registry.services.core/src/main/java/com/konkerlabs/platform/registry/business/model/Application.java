package com.konkerlabs.platform.registry.business.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.konkerlabs.platform.registry.business.model.behaviors.URIDealer;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Document(collection="applications")
public class Application implements URIDealer {
	
	
	@Id
	private String name;
	private String friendlyName;
	private String description;
	private String guid;
	
	@DBRef
	private Tenant tenant;

	public static final String URI_SCHEME = "application";

	@Override
	public String getUriScheme() {
		return URI_SCHEME;
	}

	@Override
	public String getContext() {
		return getTenant() != null ? getTenant().getDomainName() : null;
	}
	
	enum Validations {
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
	
}
