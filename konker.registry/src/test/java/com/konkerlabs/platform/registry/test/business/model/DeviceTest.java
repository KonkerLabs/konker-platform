package com.konkerlabs.platform.registry.test.business.model;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.behaviors.DeviceURIDealer;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import org.hamcrest.collection.IsEmptyCollection;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class DeviceTest {

	private Device device;

	@Before
	public void setUp() {

		Tenant tenant = Tenant.builder()
                .id(UUID.randomUUID().toString())
                .name("Tenant name")
                .domainName("tenant")
                .build();

		device = Device.builder().deviceId("95c14b36ba2b43f1").name("Device name").description("Description")
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
	public void shouldReturnAValidationMessageIfDeviceIdIsGreaterThan16Characters() throws Exception {
		device.setDeviceId("95c14b36ba2b43f1ac537");

		String expectedMessage = Device.Validations.ID_GREATER_THAN_EXPECTED.getCode();
		Optional<Map<String, Object[]>> validations = device.applyValidations();

		assertThat(validations, not(sameInstance(Optional.empty())));
		assertThat(device.applyValidations().get(), hasEntry(expectedMessage,new Object[] {16}));
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

	@Test
	public void shouldReturnItsLastEvent() throws Exception {
		Event firtEvent = Event.builder().timestamp(Instant.now().minus(Duration.ofMinutes(2))).build();
		Event lastEvent = Event.builder().timestamp(Instant.now()).build();

		device.setEvents(Arrays.asList(new Event[] { firtEvent, lastEvent }));

		assertThat(device.getLastEvent(), equalTo(lastEvent));
	}

	@Test
	public void shouldReturnNullForLastEventIfThereIsNoEventsYet() throws Exception {
		device.setEvents(null);

		assertThat(device.getLastEvent(), nullValue());

		device.setEvents(Collections.emptyList());

		assertThat(device.getLastEvent(), nullValue());
	}

	@Test
	public void shouldReturnMostRecentEventsInDescendingOrder() throws Exception {
		Event firstEvent = Event.builder().timestamp(Instant.now().minus(Duration.ofMinutes(2))).build();
		Event lastEvent = Event.builder().timestamp(Instant.now()).build();

		device.setEvents(Arrays.asList(new Event[] { firstEvent, lastEvent }));

		List<Event> mostRecentEvents = device.getMostRecentEvents();

		assertThat(mostRecentEvents.size(), equalTo(2));
		assertThat(mostRecentEvents.get(0), equalTo(lastEvent));
		assertThat(mostRecentEvents.get(1), equalTo(firstEvent));
	}

	@Test
	public void shouldReturndEmptyListForMostRecentEventsWhenNullEventList() throws Exception {
		device.setEvents(null);

		List<Event> mostRecentEvents = device.getMostRecentEvents();

		assertThat(mostRecentEvents, new IsEmptyCollection<Event>());
	}

	@Test
	public void shouldGenerateItsOwnURI() throws Exception {
        URI expected = new DeviceURIDealer() {}.toDeviceRouteURI(
            device.getTenant().getDomainName(),device.getDeviceId()
        );

        assertThat(device.toURI(),equalTo(expected));
	}
}