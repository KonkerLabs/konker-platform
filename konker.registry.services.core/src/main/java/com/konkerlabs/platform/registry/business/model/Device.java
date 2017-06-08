package com.konkerlabs.platform.registry.business.model;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.konkerlabs.platform.registry.business.model.behaviors.URIDealer;
import com.konkerlabs.platform.registry.business.model.enumerations.LogLevel;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.utilities.validations.api.Validatable;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

@Data
@Builder
@Document(collection = "devices")
public class Device implements URIDealer, Validatable, UserDetails {

    private static final long serialVersionUID = -2667977827998418995L;

	public static final String URI_SCHEME = "device";

	@Override
	public String getUriScheme() {
		return URI_SCHEME;
	}

	@Override
	public String getContext() {
		return getTenant() != null ? getTenant().getDomainName() : null;
	}

	public enum Validations {
		ID_NULL_EMPTY("model.device.id.not_null"),
		ID_GREATER_THAN_EXPECTED("model.device.id.greater_than_expected"),
		NAME_NULL_EMPTY("model.device.name.not_null"),
		REGISTRATION_DATE_NULL("model.device.registration_date.not_null"),
		API_KEY_NULL("model.device.api_key.not_null");

		public String getCode() {
			return code;
		}

		private String code;

		Validations(String code) {
			this.code = code;
		}
	}

	@Tolerate
	public Device() {
	}

	private String id;
	@DBRef
	private Tenant tenant;
	@DBRef
	private Application application;
	private String deviceId;
    private String apiKey;
	private String securityHash;
	private String name;
	private String guid;
	private String description;
	private Instant registrationDate;
	private Instant lastModificationDate;
    private LogLevel logLevel;
    @DBRef
    private DeviceModel deviceModel;
    @DBRef
    private Location location;
	private boolean active;

	public Optional<Map<String, Object[]>> applyValidations() {

		Map<String, Object[]> validations = new HashMap<>();

		if (getDeviceId() == null || getDeviceId().isEmpty())
			validations.put(Validations.ID_NULL_EMPTY.code,null);
		if (getDeviceId() != null && getDeviceId().length() > 16)
			validations.put(Validations.ID_GREATER_THAN_EXPECTED.code,new Object[]{16});
		if (getName() == null || getName().isEmpty())
			validations.put(Validations.NAME_NULL_EMPTY.code,null);
		if (getTenant() == null)
			validations.put(CommonValidations.TENANT_NULL.getCode(),null);
		if (getRegistrationDate() == null)
			validations.put(Validations.REGISTRATION_DATE_NULL.getCode(),null);
		if (getApiKey() == null || getApiKey().isEmpty())
			validations.put(Validations.API_KEY_NULL.getCode(),null);

		return Optional.of(validations).filter(map -> !map.isEmpty());
	}

	public void onRegistration() {
		Instant now = Instant.now();
		setRegistrationDate(now);
		setLastModificationDate(now);
		regenerateApiKey();
	}

	public void regenerateApiKey() {
		setApiKey(new BigInteger(60, new Random()).toString(32));
	}

	public LogLevel getLogLevel() {
		return Optional.ofNullable(logLevel).orElse(LogLevel.WARNING);
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Collections.singletonList(new SimpleGrantedAuthority("DEVICE"));
	}

	@Override
	public String getPassword() {
		return getSecurityHash();
	}

	@Override
	public String getUsername() {
		return getApiKey();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
	
}
