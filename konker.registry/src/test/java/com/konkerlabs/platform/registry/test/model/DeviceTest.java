package com.konkerlabs.platform.registry.test.model;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Tenant;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.nullValue;

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

        String expectedMessage = "Device id cannot be null or empty";

        assertThat(device.applyValidations(),hasItem(expectedMessage));
    }
    @Test
    public void shouldReturnAValidationMessageIfDeviceIdIsEmpty() throws Exception {
        device.setDeviceId("");

        String expectedMessage = "Device id cannot be null or empty";

        assertThat(device.applyValidations(),hasItem(expectedMessage));
    }
    @Test
    public void shouldReturnAValidationMessageIfDeviceIdIsGreaterThan16Characters() throws Exception {
        device.setDeviceId("95c14b36ba2b43f1ac537");

        String expectedMessage = "Device cannot be greater than 16 characters";

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
    public void shouldHaveNoValidationMessagesIfRecordIsValid() throws Exception {
        assertThat(device.applyValidations(),nullValue());
    }
}