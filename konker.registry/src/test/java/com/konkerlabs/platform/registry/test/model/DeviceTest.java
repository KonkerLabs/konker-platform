package com.konkerlabs.platform.registry.test.model;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Tenant;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
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
            .build();

        device = Device.builder()
            .deviceId("95c14b36ba2b43f1")
            .name("Device name")
            .description("Description")
            .tenant(tenant)
            .build();
    }

    @Test
    public void shouldReturnAValidationMessageIfDeviceIdIsNull() throws Exception {
        device.setDeviceId(null);

        String expectedMessage = "Device ID cannot be null or empty";

        assertThat(device.applyValidations(),hasItem(expectedMessage));
    }
    @Test
    public void shouldReturnAValidationMessageIfDeviceIdIsEmpty() throws Exception {
        device.setDeviceId("");

        String expectedMessage = "Device ID cannot be null or empty";

        assertThat(device.applyValidations(),hasItem(expectedMessage));
    }
    @Test
    public void shouldReturnAValidationMessageIfDeviceIdIsGreaterThan16Characters() throws Exception {
        device.setDeviceId("95c14b36ba2b43f1ac537");

        String expectedMessage = "Device ID cannot be greater than 16 characters";

        assertThat(device.applyValidations(),hasItem(expectedMessage));
    }
    @Test
    public void shouldReturnAValidationMessageIfNameIsNull() throws Exception {
        device.setName(null);

        String expectedMessage = "Device name cannot be null or empty";

        assertThat(device.applyValidations(),hasItem(expectedMessage));
    }
    @Test
    public void shouldReturnAValidationMessageIfNameIsEmpty() throws Exception {
        device.setName("");

        String expectedMessage = "Device name cannot be null or empty";

        assertThat(device.applyValidations(),hasItem(expectedMessage));
    }
    @Test
    public void shouldReturnAValidationMessageIfTenantIsNull() throws Exception {
        device.setTenant(null);

        String expectedMessage = "Tenant cannot be null";

        assertThat(device.applyValidations(),hasItem(expectedMessage));
    }
    @Test
    public void shouldReturnAValidationMessageIfRegistrationDateIsNull() throws Exception {
        device.setRegistrationDate(null);

        String expectedMessage = "Registration date cannot be null";

        assertThat(device.applyValidations(),hasItem(expectedMessage));
    }
    @Test
    public void shouldReturnAValidationMessageIfRegistrationDateIsMoreThan1MinuteInTheFuture() throws Exception {
        device.setRegistrationDate(Instant.now().plus(Duration.ofSeconds(61)));

        device.onRegistration();

        long intervalInSeconds = Duration.between(device.getRegistrationDate(),Instant.now()).abs().getSeconds();

        assertThat(intervalInSeconds,not(greaterThan(60L)));
    }
    @Test
    public void shouldReturnAValidationMessageIfRegistrationDateIsMoreThan1MinuteInThePast() throws Exception {
        device.setRegistrationDate(Instant.now().minus(Duration.ofSeconds(61)));

        device.onRegistration();

        long intervalInSeconds = Duration.between(device.getRegistrationDate(),Instant.now()).abs().getSeconds();

        assertThat(intervalInSeconds,not(greaterThan(60L)));
    }
    @Test
    public void shouldHaveNoValidationMessagesIfRecordIsValid() throws Exception {
        device.onRegistration();

        assertThat(device.applyValidations(),nullValue());
    }
}