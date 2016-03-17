package com.konkerlabs.platform.registry.test.business.model;

import com.konkerlabs.platform.registry.business.model.SmsDestination;
import com.konkerlabs.platform.registry.business.model.Tenant;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

public class SmsDestinationTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private SmsDestination subject;

    @Before
    public void setUp() throws Exception {
        Tenant tenant = Tenant.builder().name("Konker").domainName("konker").build();

        subject = SmsDestination.builder()
            .tenant(tenant)
            .name("SMS destination")
            .description("Description")
            .active(true).guid(UUID.randomUUID().toString()).build();
    }

    @Test
    public void shouldReturnAValidationMessageIfTenantIsNull() throws Exception {
        subject.setTenant(null);

        String expectedMessage = "Tenant cannot be null";

        assertThat(subject.applyValidations(), hasItem(expectedMessage));
    }

    @Test
    public void shouldReturnAValidationMessageIfNameIsNull() throws Exception {
        subject.setName(null);

        String expectedMessage = "Name cannot be null or empty";

        assertThat(subject.applyValidations(), hasItem(expectedMessage));
    }

    @Test
    public void shouldReturnAValidationMessageIfNameIsEmpty() throws Exception {
        subject.setName("");

        String expectedMessage = "Name cannot be null or empty";

        assertThat(subject.applyValidations(), hasItem(expectedMessage));
    }

    @Test
    public void shouldReturnAValidationMessageIfPhoneNumberIsNull() throws Exception {
        subject.setPhoneNumber(null);

        String expectedMessage = "Phone number cannot be null or empty";

        assertThat(subject.applyValidations(), hasItem(expectedMessage));
    }

    @Test
    public void shouldReturnAValidationMessageIfPhoneNumberIsEmpty() throws Exception {
        subject.setPhoneNumber("");

        String expectedMessage = "Phone number cannot be null or empty";

        assertThat(subject.applyValidations(), hasItem(expectedMessage));
    }

    @Test
    public void shouldRaiseAnExceptionIfGuidIsNull() throws Exception {
        subject.setGuid(null);

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("GUID cannot be null or empty");

        subject.applyValidations();
    }

    @Test
    public void shouldRaiseAnExceptionIfGuidIsEmpty() throws Exception {
        subject.setGuid("");

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("GUID cannot be null or empty");

        subject.applyValidations();
    }
}