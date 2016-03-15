package com.konkerlabs.platform.registry.test.business.services;

import static org.junit.rules.ExpectedException.none;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.konkerlabs.platform.registry.business.model.RestDestination;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.repositories.RestDestinationRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.RestDestinationService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.routes.api.EventRouteService;
import com.konkerlabs.platform.registry.test.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;
import com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;

import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoTestConfiguration.class, BusinessTestConfiguration.class })
@UsingDataSet(locations = { "/fixtures/tenants.json", "/fixtures/rest-destinations.json" })
public class RestDestinationServiceTest extends BusinessLayerTestSupport {
    @Rule
    public ExpectedException thrown = none();

    @Autowired
    private RestDestinationService subject;

    @Autowired
    private TenantRepository tenantRepository;

    private Tenant tenant;
    private Tenant emptyTenant;
    private Tenant otherTenant;
    private Tenant inexistentTenant;
    private RestDestination newRestDestination;
    private RestDestination oldRestDestination;

    public static final String THE_DESTINATION_ID = "4e6c441c-eaf9-11e5-a33b-8374b127eaa8";
    public static final String THE_DESTINATION_NAME = "a restful destination";
    public static final String OTHER_TENANT_DESTINATION_ID = "109cd550-eafb-11e5-b610-d3af18d1439d";
    public static final String OTHER_TENANT_DESTINATION_NAME = "another tenant restful destination";
    public static final String INEXISTENT_DESTINATION_ID = "INEXISTENT";

    @Before
    public void setUp() {
        emptyTenant = tenantRepository.findByName("EmptyTenant");
        tenant = tenantRepository.findByName("Konker");
        otherTenant = tenantRepository.findByName("InMetrics");
        inexistentTenant = Tenant.builder().domainName("someInexistentDomain")
                .id("e2bfa8b0-eaf5-11e5-8fd5-a755d49a5c5b").name("someInexistentName").build();

        newRestDestination = spy(RestDestination.builder().name("New Name").active(true)
                .serviceURI(URI.create("http://host.com/")).serviceUsername("user").servicePassword("password").build());
    }

    // ============================== findAll ==============================//
    @Test
    public void shouldReturnEmptyListIfDestinationsDoesNotExistWehnFindAll() {
        ServiceResponse<List<RestDestination>> response = subject.findAll(emptyTenant);
        assertThat(response, isResponseOk());
        assertThat(response.getResult(), empty());
    }

    @Test
    public void shouldReturnErrorMessageIfTenantDoesNotExistWhenFindAll() {
        ServiceResponse<List<RestDestination>> response = subject.findAll(inexistentTenant);
        assertThat(response, hasErrorMessage("Tenant does not exist"));
    }

    @Test
    public void shouldReturnErrorMessageIfTenantIsNullWhenFindAll() {
        ServiceResponse<List<RestDestination>> response = subject.findAll(null);
        assertThat(response, hasErrorMessage("Tenant cannot be null"));
    }

    @Test
    public void shouldReturnDestinationsWhenFindAll() {
        ServiceResponse<List<RestDestination>> response = subject.findAll(tenant);
        assertThat(response, isResponseOk());
        assertThat(response.getResult(), hasSize(greaterThan(1)));

        List<String> ids = response.getResult().stream().map(RestDestination::getId).collect(Collectors.toList());
        assertThat(ids, hasItem(THE_DESTINATION_ID));
        assertThat(ids, not(hasItem(OTHER_TENANT_DESTINATION_ID)));
    }

    @Test
    public void shouldReturnDestinationsWhenOtherTenantFindAll() {
        ServiceResponse<List<RestDestination>> response = subject.findAll(otherTenant);
        assertThat(response, isResponseOk());
        assertThat(response.getResult(), not(empty()));

        List<String> ids = response.getResult().stream().map(RestDestination::getId).collect(Collectors.toList());
        assertThat(ids, not(hasItem(THE_DESTINATION_ID)));
        assertThat(ids, hasItem(OTHER_TENANT_DESTINATION_ID));
    }

    // ============================== getBydID ==============================//

    @Test
    public void shouldReturnDestinationIfExistsWithinTenantWhenGetByID() {
        ServiceResponse<RestDestination> response = subject.getByID(tenant, THE_DESTINATION_ID);
        assertThat(response, isResponseOk());
        assertThat(response.getResult().getName(), equalTo(THE_DESTINATION_NAME));
    }

    @Test
    public void shouldReturnOtherDestinationIfExistsWithinOtherTenantWhenGetByID() {
        ServiceResponse<RestDestination> response = subject.getByID(otherTenant, OTHER_TENANT_DESTINATION_ID);
        assertThat(response, isResponseOk());
        assertThat(response.getResult().getName(), equalTo(OTHER_TENANT_DESTINATION_NAME));
    }

    @Test
    public void shouldReturnErrorIfDestinationIsOwnedByAnotherTenantWhenGetByID() {
        ServiceResponse<RestDestination> response = subject.getByID(tenant, OTHER_TENANT_DESTINATION_ID);
        assertThat(response, hasErrorMessage("REST Destination does not exist"));
    }

    @Test
    public void shouldReturnErrorIfDestinationDoesNotExistWhenGetByID() {
        ServiceResponse<RestDestination> response = subject.getByID(tenant, INEXISTENT_DESTINATION_ID);
        assertThat(response, hasErrorMessage("REST Destination does not exist"));
    }

    @Test
    public void shouldReturnErrorIfTenantIsNullWhenGetByID() {
        ServiceResponse<RestDestination> response = subject.getByID(null, THE_DESTINATION_ID);
        assertThat(response, hasErrorMessage("Tenant cannot be null"));
    }

    @Test
    public void shouldReturnErrorIfIDIsNullWhenGetByID() {
        ServiceResponse<RestDestination> response = subject.getByID(tenant, null);
        assertThat(response, hasErrorMessage("REST Destination ID cannot be null"));
    }

    // ============================== register ==============================//

    @Test
    public void shouldRegisterIfEverythingIsOkWhenRegister() {
        assertThat(newRestDestination.getId(), nullValue());
        ServiceResponse<RestDestination> response = subject.register(tenant, newRestDestination);
        assertThat(response, isResponseOk());
        assertThat(response.getResult().getId(), not(nullValue()));
        assertThat(response.getResult().getTenant(), equalTo(tenant));
        assertThat(response.getResult().isActive(), equalTo(Boolean.TRUE));
    }

    @Test
    public void shouldReturnErrorIfValidationsFailWhenRegister() {
        when(newRestDestination.applyValidations()).thenReturn(Collections.singletonList("Error Message"));
        ServiceResponse<RestDestination> response = subject.register(tenant, newRestDestination);
        assertThat(response, hasErrorMessage("Error Message"));
        assertThat(newRestDestination.getId(), nullValue());
    }

    @Test
    public void shouldReturnErrorIfTenantIsNullWhenRegister() {
        ServiceResponse<RestDestination> response = subject.register(null, newRestDestination);
        assertThat(response, hasErrorMessage("Tenant cannot be null"));
        assertThat(newRestDestination.getId(), nullValue());
    }

    @Test
    public void shouldReturnErrorIfTenantInexistentWhenRegister() {
        ServiceResponse<RestDestination> response = subject.register(inexistentTenant, newRestDestination);
        assertThat(response, hasErrorMessage("Tenant does not exist"));
        assertThat(newRestDestination.getId(), nullValue());
    }

    @Test
    public void shouldReturnErrorIfDestinatioIsNullWhenRegister() {
        ServiceResponse<RestDestination> response = subject.register(inexistentTenant, null);
        assertThat(response, hasErrorMessage("REST Destination cannot be null"));
        assertThat(newRestDestination.getId(), nullValue());
    }

    @Test
    public void shouldReturnErrorIfDestinationExistsWhenRegister() {
        newRestDestination.setName(THE_DESTINATION_NAME);
        ServiceResponse<RestDestination> response = subject.register(tenant, newRestDestination);
        assertThat(response, hasErrorMessage("Name already exists"));
        assertThat(newRestDestination.getId(), nullValue());
    }

    @Test
    public void shouldGenerateNewIdIfIDAlreadyExistsWhenRegister() {
        newRestDestination.setId(THE_DESTINATION_ID);
        ServiceResponse<RestDestination> response = subject.register(tenant, newRestDestination);
        assertThat(response, isResponseOk());
        assertThat(response.getResult().getId(), not(equalTo(THE_DESTINATION_ID)));
    }

    @Test
    public void shouldAssociateToNewTenantIfIDAlreadyExistsWhenRegister() {
        newRestDestination.setTenant(otherTenant);
        ServiceResponse<RestDestination> response = subject.register(tenant, newRestDestination);
        assertThat(response, isResponseOk());
        assertThat(response.getResult().getTenant(), equalTo(tenant));
        assertThat(response.getResult().getId(), not(nullValue()));
    }

    // ============================== update ==============================//

}
