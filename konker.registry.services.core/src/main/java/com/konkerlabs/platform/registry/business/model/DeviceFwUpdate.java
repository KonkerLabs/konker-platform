package com.konkerlabs.platform.registry.business.model;

import com.konkerlabs.platform.registry.business.model.enumerations.FirmwareUpdateStatus;
import com.konkerlabs.platform.registry.business.model.enumerations.LogLevel;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigInteger;
import java.time.Instant;
import java.util.*;

@Data
@Builder
@Document(collection = "devicesFwUpdates")
public class DeviceFwUpdate {


	@Tolerate
	public DeviceFwUpdate() {
	}

	private String id;
	@DBRef
	private Tenant tenant;
	@DBRef
	private Application application;
	private String deviceGuid;
	private FirmwareUpdateStatus status;
	@DBRef
	private DeviceFirmware deviceFirmware;
	private Instant lastChange;

	public Optional<Map<String, Object[]>> applyValidations() {
		Map<String, Object[]> validations = new HashMap<>();

		return Optional.of(validations).filter(map -> !map.isEmpty());
	}
	
}
