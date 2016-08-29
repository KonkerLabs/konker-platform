package com.konkerlabs.platform.registry.business.model;

import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.model.validation.Validatable;
import com.konkerlabs.platform.registry.business.model.behaviors.DeviceURIDealer;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.net.URI;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Data
@Builder
@Document(collection = "devices")
public class Device implements DeviceURIDealer, Validatable {

	public enum Validations {
		ID_NULL_EMPTY("model.device.id.not_null"),
		ID_GREATER_THAN_EXPECTED("model.device.id.greater_than_expected"),
		NAME_NULL_EMPTY("model.device.name.not_null"),
		REGISTRATION_DATE_NULL("model.device.registration_date.not_null");

		public String getCode() {
			return code;
		}

		private String code;

		Validations(String code) {
			this.code = code;
		}
	}

    private String id;
	@DBRef
	private Tenant tenant;
	private String deviceId;
    private String apiKey;
	private String name;
	private String description;
	private Instant registrationDate;
	private List<Event> events;
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
			validations.put(Validations.REGISTRATION_DATE_NULL.code,null);

		if (validations.isEmpty())
			return Optional.empty();
		else
			return Optional.of(validations);
	}

	public void onRegistration() {
		setRegistrationDate(Instant.now());
	}

	public Event getLastEvent() {
		return getMostRecentEvents().stream().findFirst().orElse(null);
	}

	// FIXME Needs performance improvement. Sorting those items on the
	// application server and returning all of them is not efficient.
	public List<Event> getMostRecentEvents() {
		return Optional.ofNullable(getEvents()).orElse(Collections.emptyList()).stream()
				.sorted((eventA, eventB) -> eventB.getTimestamp().compareTo(eventA.getTimestamp()))
				.collect(Collectors.toList());
	}

	public URI toURI() {
		return toDeviceRouteURI(getTenant().getDomainName(),getDeviceId());
	}
}
