package com.konkerlabs.platform.registry.test.business.model;

import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.destinations.RestDestination;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
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
                .uri(new URI("http://outgoing.rest.com"))
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
        subject.setUri(null);

        String expectedMessage = "URL cannot be null or empty";

        assertThat(subject.applyValidations(), hasItem(expectedMessage));
    }
    @Test
    public void shouldReturnAValidationMessageIfURIIsEmpty() throws Exception {
        subject.setUri(new URI(null,null,null,null,null));

        String expectedMessage = "URL cannot be null or empty";

        assertThat(subject.applyValidations(), hasItem(expectedMessage));
    }
    @Test
    public void shouldHaveNoValidationMessagesIfRecordIsValid() throws Exception {
        assertThat(subject.applyValidations(), empty());
    }
}
