package com.konkerlabs.platform.registry.test.business.model;

import com.konkerlabs.platform.registry.business.model.RestDestination;
import com.konkerlabs.platform.registry.business.model.Tenant;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.nullValue;

public class RestDestinationTest {

    private RestDestination subject;

    @Before
    public void setUp() throws Exception {

        Tenant tenant = Tenant.builder().name("Konker").build();

        subject = RestDestination.builder()
                .tenant(tenant)
                .name("TestActor")
                .serviceURI(new URI("http://outgoing.rest.com"))
                .serviceUsername("user")
                .servicePassword("password")
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
        subject.setServiceURI(new URI(null,null,null,null,null));

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
    public void shouldHaveNoValidationMessagesIfUsernameAndPasswordAreEmptys() throws Exception {
        subject.setServiceUsername("");
        subject.setServicePassword("");
        assertThat(subject.applyValidations(), nullValue());
    }


    @Test
    public void shouldHaveNoValidationMessagesIfRecordIsValid() throws Exception {
        assertThat(subject.applyValidations(), nullValue());
    }
}
