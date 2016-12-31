package com.konkerlabs.platform.registry.test.business.model;

import com.konkerlabs.platform.registry.business.model.SmsDestination;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.behaviors.SmsDestinationURIDealer;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

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

        String expectedMessage = CommonValidations.TENANT_NULL.getCode();

        assertThat(subject.applyValidations().get(), hasEntry(expectedMessage, null));
    }

    @Test
    public void shouldReturnAValidationMessageIfNameIsNull() throws Exception {
        subject.setName(null);

        String expectedMessage = SmsDestination.Validations.NAME_NULL_EMPTY.getCode();

        assertThat(subject.applyValidations().get(), hasEntry(expectedMessage, null));
    }

    @Test
    public void shouldReturnAValidationMessageIfNameIsEmpty() throws Exception {
        subject.setName("");

        String expectedMessage = SmsDestination.Validations.NAME_NULL_EMPTY.getCode();

        assertThat(subject.applyValidations().get(), hasEntry(expectedMessage, null));
    }

    @Test
    public void shouldReturnAValidationMessageIfPhoneNumberIsNull() throws Exception {
        subject.setPhoneNumber(null);

        String expectedMessage = SmsDestination.Validations.PHONE_NULL_EMPTY.getCode();

        assertThat(subject.applyValidations().get(), hasEntry(expectedMessage, null));
    }

    @Test
    public void shouldReturnAValidationMessageIfPhoneNumberIsEmpty() throws Exception {
        subject.setPhoneNumber("");

        String expectedMessage = SmsDestination.Validations.PHONE_NULL_EMPTY.getCode();

        assertThat(subject.applyValidations().get(), hasEntry(expectedMessage, null));
    }

    @Test
    public void shouldReturnAValidationMessageIfPhoneNumberIsInvalid() throws Exception {
        String expectedMessage = SmsDestination.Validations.PHONE_FORMAT_INVALID.getCode();

        List<String> invalidPhoneNumbers = Arrays.asList(new String[] {
            "11987654321",
            "+837192ht12",
            "+5511987654321x"
        });

        invalidPhoneNumbers.stream().forEach(invalid -> {
            subject.setPhoneNumber(invalid);
            assertThat(subject.applyValidations().get(), hasEntry(expectedMessage, null));
        });
    }

    @Test
    public void shouldReturnErrorMessageIfGuidIsNull() throws Exception {
        String expectedMessage = SmsDestination.Validations.GUID_NULL_EMPTY.getCode();

        subject.setGuid(null);

        assertThat(subject.applyValidations().get(), hasEntry(expectedMessage, null));
    }

    @Test
    public void shouldReturnErrorMessageIfGuidIsEmpty() throws Exception {
        String expectedMessage = SmsDestination.Validations.GUID_NULL_EMPTY.getCode();

        subject.setGuid("");

        assertThat(subject.applyValidations().get(), hasEntry(expectedMessage, null));
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
        thrown.expectMessage("CONTEXT cannot be null or empty");

        subject.setTenant(null);
        URI uri = subject.toURI();
        assertThat(uri, nullValue());
    }

    @Test
    public void shouldThrowExceptionIfTenantDomainIsNullWhenToURI() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("CONTEXT cannot be null or empty");

        subject.getTenant().setDomainName(null);;
        URI uri = subject.toURI();
        assertThat(uri, nullValue());
    }

    @Test
    public void shouldThrowExceptionIfTenantDomainIsEmptyWhenToURI() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("CONTEXT cannot be null or empty");

        subject.getTenant().setDomainName("");;
        URI uri = subject.toURI();
        assertThat(uri, nullValue());
    }

    @Test
    public void shouldThrowExceptionIfGUIDIsNullWhenToURI() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("GUID cannot be null or empty");

        subject.setGuid(null);
        URI uri = subject.toURI();
        assertThat(uri, nullValue());
    }

    @Test
    public void shouldThrowExceptionIfGUIDIsEmptyWhenToURI() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("GUID cannot be null or empty");

        subject.setGuid("");
        URI uri = subject.toURI();
        assertThat(uri, nullValue());
    }
}