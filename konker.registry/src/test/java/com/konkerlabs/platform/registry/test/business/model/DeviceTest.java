package com.konkerlabs.platform.registry.test.business.model;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Tenant;

import com.konkerlabs.platform.registry.business.model.behaviors.DeviceURIDealer;
import org.hamcrest.collection.IsEmptyCollection;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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

		String expectedMessage = "Device ID cannot be null or empty";

		assertThat(device.applyValidations(), hasItem(expectedMessage));
	}

	@Test
	public void shouldReturnAValidationMessageIfDeviceIdIsEmpty() throws Exception {
	    device.setDeviceId("");

	
	    String expectedMessage = "Device ID cannot be null or empty";

		assertThat(device.applyValidations(), hasItem(expectedMessage));
	}

	@Test
	public void shouldReturnAValidationMessageIfDeviceIdIsGreaterThan16Characters() throws Exception {
		device.setDeviceId("95c14b36ba2b43f1ac537");

		String expectedMessage = "Device ID cannot be greater than 16 characters";

		assertThat(device.applyValidations(), hasItem(expectedMessage));
	}

	@Test
	public void shouldReturnAValidationMessageIfNameIsNull() throws Exception {
		device.setName(null);

		String expectedMessage = "Device name cannot be null or empty";

		assertThat(device.applyValidations(), hasItem(expectedMessage));
	}

	@Test
	public void shouldReturnAValidationMessageIfNameIsEmpty() throws Exception {
		device.setName("");

		String expectedMessage = "Device name cannot be null or empty";

		assertThat(device.applyValidations(), hasItem(expectedMessage));
	}

	@Test
	public void shouldReturnAValidationMessageIfTenantIsNull() throws Exception {
		device.setTenant(null);

		String expectedMessage = "Tenant cannot be null";

		assertThat(device.applyValidations(), hasItem(expectedMessage));
	}

	@Test
	public void shouldReturnAValidationMessageIfRegistrationDateIsNull() throws Exception {
		device.setRegistrationDate(null);

		String expectedMessage = "Registration date cannot be null";

		assertThat(device.applyValidations(), hasItem(expectedMessage));
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

		assertThat(device.applyValidations(), nullValue());
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