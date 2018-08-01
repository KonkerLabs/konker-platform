package com.konkerlabs.platform.registry.test.business.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.konkerlabs.platform.registry.business.model.behaviors.URIDealer;
import org.junit.Before;
import org.junit.Test;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.behaviors.DeviceURIDealer;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;

public class DeviceTest {

	private Device device;
	private Set<String> tags =new HashSet<>(Arrays.asList("tag1", "tag2"));

	@Before
	public void setUp() {

		Tenant tenant = Tenant.builder()
                .id(UUID.randomUUID().toString())
                .name("Tenant name")
                .domainName("tenant")
                .build();

		device = Device.builder()
				.deviceId("95c14b36ba2b43f1")
				.guid("22821842-7438-4c46-8bb2-5a2f56cd8923")
				.name("Device name")
				.description("Description")
				.tags(tags)
				.tenant(tenant).build();
	}

	@Test
	public void shouldReturnAValidationMessageIfDeviceIdIsNull() throws Exception {
	    device.setDeviceId(null);

		String expectedMessage = Device.Validations.ID_NULL_EMPTY.getCode();
		Optional<Map<String, Object[]>> validations = device.applyValidations();

		assertThat(validations, not(sameInstance(Optional.empty())));
		assertThat(device.applyValidations().get(), hasEntry(expectedMessage,null));
	}

	@Test
	public void shouldReturnAValidationMessageIfDeviceIdIsEmpty() throws Exception {
	    device.setDeviceId("");

		String expectedMessage = Device.Validations.ID_NULL_EMPTY.getCode();
		Optional<Map<String, Object[]>> validations = device.applyValidations();

		assertThat(validations, not(sameInstance(Optional.empty())));
		assertThat(device.applyValidations().get(), hasEntry(expectedMessage,null));
	}

	@Test
	public void shouldReturnAValidationMessageIfDeviceIdIsGreaterThan36Characters() throws Exception {
		device.setDeviceId("5dc5de91-8183-47c3-a640-689b8089eebc###");

		String expectedMessage = Device.Validations.ID_GREATER_THAN_EXPECTED.getCode();
		Optional<Map<String, Object[]>> validations = device.applyValidations();

		assertThat(validations, not(sameInstance(Optional.empty())));
		assertThat(device.applyValidations().get(), hasEntry(expectedMessage,new Object[] {36}));
	}

	@Test
	public void shouldReturnAValidationMessageIfNameIsNull() throws Exception {
		device.setName(null);

		String expectedMessage = Device.Validations.NAME_NULL_EMPTY.getCode();
		Optional<Map<String, Object[]>> validations = device.applyValidations();

		assertThat(validations, not(sameInstance(Optional.empty())));
		assertThat(device.applyValidations().get(), hasEntry(expectedMessage,null));
	}

	@Test
	public void shouldReturnAValidationMessageIfNameIsEmpty() throws Exception {
		device.setName("");

		String expectedMessage = Device.Validations.NAME_NULL_EMPTY.getCode();
		Optional<Map<String, Object[]>> validations = device.applyValidations();

		assertThat(validations, not(sameInstance(Optional.empty())));
		assertThat(device.applyValidations().get(), hasEntry(expectedMessage,null));
	}

	@Test
	public void shouldReturnAValidationMessageIfTenantIsNull() throws Exception {
		device.setTenant(null);

		String expectedMessage = CommonValidations.TENANT_NULL.getCode();
		Optional<Map<String, Object[]>> validations = device.applyValidations();

		assertThat(validations, not(sameInstance(Optional.empty())));
		assertThat(device.applyValidations().get(), hasEntry(expectedMessage,null));
	}

	@Test
	public void shouldReturnAValidationMessageIfRegistrationDateIsNull() throws Exception {
		device.setRegistrationDate(null);

		String expectedMessage = Device.Validations.REGISTRATION_DATE_NULL.getCode();
		Optional<Map<String, Object[]>> validations = device.applyValidations();

		assertThat(validations, not(sameInstance(Optional.empty())));
		assertThat(device.applyValidations().get(), hasEntry(expectedMessage,null));
	}

	@Test
	public void shouldReturnAValidationMessageIfApiKeyIsNull() throws Exception {
		//API Key generation should be performed by onRegistration callback

		String expectedMessage = Device.Validations.API_KEY_NULL.getCode();
		Optional<Map<String, Object[]>> validations = device.applyValidations();

		assertThat(validations, not(sameInstance(Optional.empty())));
		assertThat(device.applyValidations().get(), hasEntry(expectedMessage,null));
	}

	@Test
	public void shouldReturnAValidationMessageIfRegistrationDateIsMoreThan1MinuteInTheFuture() throws Exception {
		device.setRegistrationDate(Instant.now().plus(Duration.ofSeconds(61)));

		device.onRegistration();

		long intervalInSeconds = Duration.between(device.getRegistrationDate(), Instant.now()).abs().getSeconds();

		assertThat(intervalInSeconds, not(greaterThan(60L)));
	}

	@Test
	public void shouldReturnAValidationMessageIfRegistrationDateIsMoreThan1MinuteInThePast() throws Exception {
		device.setRegistrationDate(Instant.now().minus(Duration.ofSeconds(61)));

		device.onRegistration();

		long intervalInSeconds = Duration.between(device.getRegistrationDate(), Instant.now()).abs().getSeconds();

		assertThat(intervalInSeconds, not(greaterThan(60L)));
	}

	@Test
	public void shouldHaveNoValidationMessagesIfRecordIsValid() throws Exception {
		device.onRegistration();

		assertThat(device.applyValidations(), sameInstance(Optional.empty()));
	}

//	@Test
//	public void shouldReturnItsLastEvent() throws Exception {
//		Event firtEvent = Event.builder().timestamp(Instant.now().minus(Duration.ofMinutes(2))).build();
//		Event lastEvent = Event.builder().timestamp(Instant.now()).build();
//
//		device.setEvents(Arrays.asList(new Event[] { firtEvent, lastEvent }));
//
//		assertThat(device.getLastEvent(), equalTo(lastEvent));
//	}

//	@Test
//	public void shouldReturnNullForLastEventIfThereIsNoEventsYet() throws Exception {
//		device.setEvents(null);
//
//		assertThat(device.getLastEvent(), nullValue());
//
//		device.setEvents(Collections.emptyList());
//
//		assertThat(device.getLastEvent(), nullValue());
//	}

//	@Test
//	public void shouldReturnMostRecentEventsInDescendingOrder() throws Exception {
//		Event firstEvent = Event.builder().timestamp(Instant.now().minus(Duration.ofMinutes(2))).build();
//		Event lastEvent = Event.builder().timestamp(Instant.now()).build();
//
//		device.setEvents(Arrays.asList(new Event[] { firstEvent, lastEvent }));
//
//		List<Event> mostRecentEvents = device.getMostRecentEvents();
//
//		assertThat(mostRecentEvents.size(), equalTo(2));
//		assertThat(mostRecentEvents.get(0), equalTo(lastEvent));
//		assertThat(mostRecentEvents.get(1), equalTo(firstEvent));
//	}

//	@Test
//	public void shouldReturndEmptyListForMostRecentEventsWhenNullEventList() throws Exception {
//		device.setEvents(null);
//
//		List<Event> mostRecentEvents = device.getMostRecentEvents();
//
//		assertThat(mostRecentEvents, new IsEmptyCollection<Event>());
//	}

	@Test
	public void shouldGenerateItsOwnURI() throws Exception {
        URI expected = new URIDealer() {
			@Override
			public String getUriScheme() {
				return Device.URI_SCHEME;
			}

			@Override
			public String getContext() {
				return device.getTenant().getDomainName();
			}

			@Override
			public String getGuid() {
				return device.getGuid();
			}
		}.toURI();

        assertThat(device.toURI(),equalTo(expected));
	}
}
