package com.konkerlabs.platform.registry.test.business.model;

import com.konkerlabs.platform.registry.business.model.RestDestination;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.behaviors.RESTDestinationURIDealer;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class RestDestinationTest {

    private RestDestination subject;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {

        Tenant tenant = Tenant.builder().name("Konker").domainName("konker").build();

        subject = RestDestination.builder().tenant(tenant).guid("2e941f28-eb82-11e5-8cc4-ef309bb052d7").name("TestActor")
                .serviceURI("http://outgoing.rest.com").serviceUsername("user").servicePassword("password")
                .build();
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
    public void shouldReturnAValidationMessageIfURIIsNull() throws Exception {
        subject.setServiceURI(null);

        String expectedMessage = "URL cannot be null or empty";

        assertThat(subject.applyValidations(), hasItem(expectedMessage));
    }

    @Test
    public void shouldReturnAValidationMessageIfURIIsEmpty() throws Exception {
        subject.setServiceURI("");

        String expectedMessage = "URL cannot be null or empty";

        assertThat(subject.applyValidations(), hasItem(expectedMessage));
    }

    @Test
    public void shouldReturnAValidationMessageIfPassowrdIsSetAndUsernameIsNull() throws Exception {
        subject.setServiceUsername(null);
        assertThat(subject.applyValidations(), hasItem("Password is set but username is empty"));
    }

    @Test
    public void shouldReturnAValidationMessageIfPassowrdIsSetAndUsernameIsEmpty() throws Exception {
        subject.setServiceUsername("   ");
        assertThat(subject.applyValidations(), hasItem("Password is set but username is empty"));
    }

    @Test
    public void shouldHaveNoValidationMessagesIfUsernameIsSetButPasswordIsNull() throws Exception {
        subject.setServicePassword(null);
        assertThat(subject.applyValidations(), nullValue());
    }

    @Test
    public void shouldReturnAValidationMessageIfInterpolatesHost() throws Exception {
        subject.setServiceURI("http://@{#var}/");
        assertThat(subject.applyValidations(), not(empty()));
    }

    @Test
    public void shouldHaveNoValidationMessagesIfInterpolatesPath() throws Exception {
        subject.setServiceURI("http://host/@{#var}/");
        assertThat(subject.applyValidations(), nullValue());
    }

    @Test
    public void shouldHaveNoValidationMessagesIfUsernameIsSetButPasswordIsEmpty() throws Exception {
        subject.setServicePassword("   ");
        assertThat(subject.applyValidations(), nullValue());
    }

    @Test
    public void shouldHaveNoValidationMessagesIfUsernameAndPasswordAreNull() throws Exception {
        subject.setServiceUsername(null);
        subject.setServicePassword(null);
        assertThat(subject.applyValidations(), nullValue());
    }

    @Test
    public void shouldHaveNoValidationMessagesIfUsernameAndPasswordAreEmpty() throws Exception {
        subject.setServiceUsername("");
        subject.setServicePassword("");
        assertThat(subject.applyValidations(), nullValue());
    }

    @Test
    public void shouldHaveNoValidationMessagesIfRecordIsValid() throws Exception {
        assertThat(subject.applyValidations(), nullValue());
    }

    @Test
    public void shouldReturnValidURIIfNameIsOK() throws Exception {
        URI uri = subject.toURI();
        assertThat(uri, not(nullValue()));
        assertThat(uri.getScheme(), equalTo(RESTDestinationURIDealer.REST_DESTINATION_URI_SCHEME));
        assertThat(uri.getAuthority(), equalTo("konker"));
        assertThat(uri.getPath(), equalTo("/2e941f28-eb82-11e5-8cc4-ef309bb052d7"));
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
    public void shouldThrowExceptionIfNameIsNullWhenToURI() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("REST Destination ID");

        subject.setGuid(null);
        URI uri = subject.toURI();
        assertThat(uri, nullValue());
    }

    @Test
    public void shouldThrowExceptionIfGUIDIsEmptyWhenToURI() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("REST Destination ID");

        subject.setGuid("");;
        URI uri = subject.toURI();
        assertThat(uri, nullValue());
    }

}
