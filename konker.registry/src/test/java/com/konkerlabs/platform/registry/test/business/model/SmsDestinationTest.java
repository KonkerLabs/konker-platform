package com.konkerlabs.platform.registry.test.business.model;

import com.konkerlabs.platform.registry.business.model.SmsDestination;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.behaviors.SmsDestinationURIDealer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.URI;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

public class SmsDestinationTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private SmsDestination subject;
    private String guid;

    @Before
    public void setUp() throws Exception {
        Tenant tenant = Tenant.builder().name("Konker").domainName("konker").build();

        guid = UUID.randomUUID().toString();

        subject = SmsDestination.builder()
            .tenant(tenant)
            .name("SMS destination")
            .description("Description")
            .phoneNumber("+5511987654321")
            .guid(guid)
            .active(true).build();
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

    @Test
    public void shouldReturnValidURIIfNameIsOK() throws Exception {
        URI uri = subject.toURI();
        assertThat(uri, not(nullValue()));
        assertThat(uri.getScheme(), equalTo(SmsDestinationURIDealer.SMS_URI_SCHEME));
        assertThat(uri.getAuthority(), equalTo("konker"));
        assertThat(uri.getPath().replaceAll("\\/",""), equalTo(guid));
    }

    @Test
    public void shouldThrowExceptionIfTenantIsNullWhenToURI() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("tenant domain");

        subject.setTenant(null);
        URI uri = subject.toURI();
        assertThat(uri, nullValue());
    }

    @Test
    public void shouldThrowExceptionIfTenantDomainIsNullWhenToURI() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("tenant domain");

        subject.getTenant().setDomainName(null);;
        URI uri = subject.toURI();
        assertThat(uri, nullValue());
    }

    @Test
    public void shouldThrowExceptionIfTenantDomainIsEmptyWhenToURI() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("tenant domain");

        subject.getTenant().setDomainName("");;
        URI uri = subject.toURI();
        assertThat(uri, nullValue());
    }

    @Test
    public void shouldThrowExceptionIfGUIDIsNullWhenToURI() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("SMS GUID cannot be null or empty");

        subject.setGuid(null);
        URI uri = subject.toURI();
        assertThat(uri, nullValue());
    }

    @Test
    public void shouldThrowExceptionIfGUIDIsEmptyWhenToURI() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("SMS GUID cannot be null or empty");

        subject.setGuid("");
        URI uri = subject.toURI();
        assertThat(uri, nullValue());
    }
}