package com.konkerlabs.platform.registry.test.business.model;

import com.konkerlabs.platform.registry.business.model.Gateway;
import com.konkerlabs.platform.registry.business.model.Location;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;

public class GatewayTest {

	private Gateway subject;
	private String guid;

	@Before
	public void setUp() throws Exception {

		Tenant tenant = Tenant.builder().name("Konker").build();

		guid = UUID.randomUUID().toString();

		Location location = Location.builder().id("location_id").build();

		subject = Gateway.builder().tenant(tenant)
                    .guid(guid)
                    .name("Gateway name")
                    .description("Description")
                    .location(location)
                    .build();

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

		String expectedMessage = Gateway.Validations.NAME_NULL_EMPTY.getCode();

		assertThat(subject.applyValidations().get(), hasEntry(expectedMessage, null));
	}

	@Test
	public void shouldReturnAValidationMessageIfNameIsEmpty() throws Exception {
		subject.setName("");

		String expectedMessage = Gateway.Validations.NAME_NULL_EMPTY.getCode();

		assertThat(subject.applyValidations().get(), hasEntry(expectedMessage, null));
	}

	@Test
	public void shouldReturnAValidationMessageIfGUIDIsNull() throws Exception {
		subject.setGuid(null);

		String expectedMessage = Gateway.Validations.GUID_NULL.getCode();

		assertThat(subject.applyValidations().get(), hasEntry(expectedMessage, null));
	}

	@Test
	public void shouldReturnAValidationMessageIfGUIDIsEmpty() throws Exception {
		subject.setGuid("");

		String expectedMessage = Gateway.Validations.GUID_NULL.getCode();

		assertThat(subject.applyValidations().get(), hasEntry(expectedMessage, null));
	}

    @Test
    public void shouldReturnAValidationMessageIfLocationIsNull() throws Exception {
        subject.setLocation(null);

        String expectedMessage = Gateway.Validations.LOCATION_NULL.getCode();

        assertThat(subject.applyValidations().get(), hasEntry(expectedMessage, null));
    }

    @Test
	public void shouldHaveNoValidationMessagesIfRecordIsValid() throws Exception {
		assertThat(subject.applyValidations().isPresent(), is(false));
	}

}
