package com.konkerlabs.platform.registry.test.business.services;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.EventRoute;
import com.konkerlabs.platform.registry.business.model.EventRoute.RouteActor;
import com.konkerlabs.platform.registry.business.model.RestDestination;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.converters.URIReadConverter;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.ApplicationRepository;
import com.konkerlabs.platform.registry.business.repositories.EventRouteRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.RestDestinationService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.test.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.konkerlabs.platform.registry.business.model.validation.CommonValidations.TENANT_NULL;
import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.hasErrorMessage;
import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.isResponseOk;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoTestConfiguration.class, BusinessTestConfiguration.class, URIReadConverter.class})
@UsingDataSet(locations = { "/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/rest-destinations.json" })
public class RestDestinationServiceTest extends BusinessLayerTestSupport {
    @Rule
    public ExpectedException thrown = none();

    @Autowired
    private RestDestinationService subject;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private EventRouteRepository eventRouteRepository;

    private Tenant tenant;
    private Tenant emptyTenant;
    private Tenant otherTenant;
    private Tenant inexistentTenant;

    private Application application;
    private Application emptyApplication;
    private Application otherApplication;
    private Application inexistentApplication;

    private RestDestination newRestDestination;
    private RestDestination oldRestDestination;

    public static final String THE_DESTINATION_ID = "4e6c441c-eaf9-11e5-a33b-8374b127eaa8";
    public static final String THE_DESTINATION_GUID = "dda64780-eb81-11e5-958b-a73dab8b32ee";
    public static final String THE_DESTINATION_NAME = "a restful destination";
    public static final String OTHER_DESTINATION_NAME = "sjhsdf";
    public static final String OTHER_TENANT_DESTINATION_ID = "109cd550-eafb-11e5-b610-d3af18d1439d";
    public static final String OTHER_TENANT_DESTINATION_GUID = "faf3391a-eb81-11e5-8c84-33e0d9a91f0c";
    public static final String OTHER_TENANT_DESTINATION_NAME = "another tenant restful destination";
    public static final String INEXISTENT_DESTINATION_ID = UUID.randomUUID().toString();
    public static final String INEXISTENT_DESTINATION_GUID = UUID.randomUUID().toString();
    public static final String UPDATED_DESTINATION_NAME = "updated restful destination";

    @Before
    public void setUp() {
        emptyTenant = tenantRepository.findByName("EmptyTenant");
        tenant = tenantRepository.findByName("Konker");
        otherTenant = tenantRepository.findByName("InMetrics");
        inexistentTenant = Tenant.builder().domainName("someInexistentDomain")
                .id("e2bfa8b0-eaf5-11e5-8fd5-a755d49a5c5b").name("someInexistentName").build();

        emptyApplication = applicationRepository.findByTenantAndName(emptyTenant.getId(), "empty");
        application = applicationRepository.findByTenantAndName(tenant.getId(), "konker");
        otherApplication = applicationRepository.findByTenantAndName(otherTenant.getId(), "inm");

        newRestDestination = spy(
                RestDestination.builder().name("New Name").active(true).serviceURI("http://host.com/")
                        .serviceUsername("user").servicePassword("password").method("GET").build());
        oldRestDestination = spy(RestDestination.builder().id(THE_DESTINATION_ID).name(THE_DESTINATION_NAME)
                .tenant(tenant).active(false).serviceURI("http://host.com/").serviceUsername("user")
                .servicePassword("password").build());

    }

    // ============================== findAll ==============================//
    @Test
    public void shouldReturnEmptyListIfDestinationsDoesNotExistWhenFindAll() {
        ServiceResponse<List<RestDestination>> response = subject.findAll(emptyTenant, emptyApplication);

        assertThat(response, isResponseOk());
    }

    @Test
    public void shouldReturnErrorMessageIfTenantDoesNotExistWhenFindAll() {
        ServiceResponse<List<RestDestination>> response = subject.findAll(inexistentTenant, inexistentApplication);

        assertThat(response, hasErrorMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()));
    }

    @Test
    public void shouldReturnErrorMessageIfTenantIsNullWhenFindAll() {
        ServiceResponse<List<RestDestination>> response = subject.findAll(null, null);
        assertThat(response, hasErrorMessage(TENANT_NULL.getCode()));
    }

    @Test
    public void shouldReturnErrorMessageIfApplicationIsNullWhenFindAll() {
        ServiceResponse<List<RestDestination>> response = subject.findAll(tenant, null);
        assertThat(response, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    @Test
    public void shouldReturnErrorMessageIfApplicationIsInvalidWhenFindAll() {
        ServiceResponse<List<RestDestination>> response = subject.findAll(tenant, otherApplication);
        assertThat(response, hasErrorMessage(ApplicationService.Validations.APPLICATION_NOT_FOUND.getCode()));
    }

    @Test
    public void shouldReturnDestinationsWhenFindAll() {
        ServiceResponse<List<RestDestination>> response = subject.findAll(tenant, application);
        assertThat(response, isResponseOk());
        assertThat(response.getResult(), hasSize(greaterThan(1)));

        List<String> ids = response.getResult().stream().map(RestDestination::getId).collect(Collectors.toList());
        assertThat(ids, hasItem(THE_DESTINATION_ID));
        assertThat(ids, not(hasItem(OTHER_TENANT_DESTINATION_ID)));
    }

    @Test
    public void shouldReturnDestinationsWhenOtherTenantFindAll() {
        ServiceResponse<List<RestDestination>> response = subject.findAll(otherTenant, otherApplication);
        assertThat(response, isResponseOk());
        assertThat(response.getResult(), not(empty()));

        List<String> ids = response.getResult().stream().map(RestDestination::getId).collect(Collectors.toList());
        assertThat(ids, not(hasItem(THE_DESTINATION_ID)));
        assertThat(ids, hasItem(OTHER_TENANT_DESTINATION_ID));
    }

    // ============================== getByID ==============================//

    @Test
    public void shouldReturnDestinationIfExistsWithinTenantWhenGetByID() {
        ServiceResponse<RestDestination> response = subject.getByGUID(tenant, application, THE_DESTINATION_GUID);
        assertThat(response, isResponseOk());
        assertThat(response.getResult().getName(), equalTo(THE_DESTINATION_NAME));
    }

    @Test
    public void shouldReturnOtherDestinationIfExistsWithinOtherTenantWhenGetByID() {
        ServiceResponse<RestDestination> response = subject.getByGUID(otherTenant, otherApplication, OTHER_TENANT_DESTINATION_GUID);
        assertThat(response, isResponseOk());
        assertThat(response.getResult().getName(), equalTo(OTHER_TENANT_DESTINATION_NAME));
    }

    @Test
    public void shouldReturnErrorIfDestinationIsOwnedByAnotherTenantWhenGetByID() {
        ServiceResponse<RestDestination> response = subject.getByGUID(tenant, application, OTHER_TENANT_DESTINATION_ID);
        assertThat(response, hasErrorMessage(RestDestinationService.Validations.DESTINATION_NOT_FOUND.getCode()));
    }

    @Test
    public void shouldReturnErrorIfDestinationDoesNotExistWhenGetByID() {
        ServiceResponse<RestDestination> response = subject.getByGUID(tenant, application, INEXISTENT_DESTINATION_ID);
        assertThat(response, hasErrorMessage(RestDestinationService.Validations.DESTINATION_NOT_FOUND.getCode()));
    }

    @Test
    public void shouldReturnErrorIfTenantIsNullWhenGetByID() {
        ServiceResponse<RestDestination> response = subject.getByGUID(null, null, THE_DESTINATION_ID);
        assertThat(response, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
    }

    @Test
    public void shouldReturnErrorMessageIfApplicationIsNullWhenGetByID() {
        ServiceResponse<RestDestination> response = subject.getByGUID(tenant, null, THE_DESTINATION_ID);
        assertThat(response, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    @Test
    public void shouldReturnErrorMessageIfApplicationIsInvalidWhenGetByID() {
        ServiceResponse<RestDestination> response = subject.getByGUID(tenant, otherApplication, THE_DESTINATION_ID);
        assertThat(response, hasErrorMessage(ApplicationService.Validations.APPLICATION_NOT_FOUND.getCode()));
    }


    @Test
    public void shouldReturnErrorIfIDIsNullWhenGetByID() {
        ServiceResponse<RestDestination> response = subject.getByGUID(tenant, application, null);
        assertThat(response, hasErrorMessage(RestDestinationService.Validations.GUID_NULL.getCode()));
    }

    // ============================== register ==============================//

    @Test
    public void shouldRegisterIfEverythingIsOkWhenRegister() {
        assertThat(newRestDestination.getId(), nullValue());
        ServiceResponse<RestDestination> response = subject.register(tenant, application, newRestDestination);
        assertThat(response, isResponseOk());
        assertThat(response.getResult().getId(), not(nullValue()));
        assertThat(response.getResult().getTenant(), equalTo(tenant));
        assertThat(response.getResult().isActive(), equalTo(Boolean.TRUE));
    }

    @Test
    public void shouldReturnErrorIfValidationsFailWhenRegister() {
        when(newRestDestination.applyValidations()).thenReturn(
            Optional.of(new HashMap() {{
                put("some.error",new Object[] {"some_value"});
            }})
        );
        ServiceResponse<RestDestination> response = subject.register(tenant, application, newRestDestination);
        assertThat(response, hasErrorMessage("some.error",new Object[] {"some_value"}));
        assertThat(newRestDestination.getId(), nullValue());
    }

    @Test
    public void shouldReturnErrorIfTenantIsNullWhenRegister() {
        ServiceResponse<RestDestination> response = subject.register(null, null, newRestDestination);
        assertThat(response, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
        assertThat(newRestDestination.getId(), nullValue());
    }

    @Test
    public void shouldReturnErrorIfTenantInexistentWhenRegister() {
        ServiceResponse<RestDestination> response = subject.register(inexistentTenant, inexistentApplication, newRestDestination);
        assertThat(response, hasErrorMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()));
        assertThat(newRestDestination.getId(), nullValue());
    }

    @Test
    public void shouldReturnErrorIfDestinatioIsNullWhenRegister() {
        ServiceResponse<RestDestination> response = subject.register(inexistentTenant, inexistentApplication, null);
        assertThat(response, hasErrorMessage(CommonValidations.RECORD_NULL.getCode()));
        assertThat(newRestDestination.getId(), nullValue());
    }

    @Test
    public void shouldReturnErrorIfDestinationExistsWhenRegister() {
        newRestDestination.setName(THE_DESTINATION_NAME);
        ServiceResponse<RestDestination> response = subject.register(tenant, application, newRestDestination);
        assertThat(response, hasErrorMessage(RestDestinationService.Validations.NAME_IN_USE.getCode()));
        assertThat(newRestDestination.getId(), nullValue());
    }

    @Test
    public void shouldGenerateNewIdIfIDAlreadyExistsWhenRegister() {
        newRestDestination.setId(THE_DESTINATION_ID);
        ServiceResponse<RestDestination> response = subject.register(tenant, application, newRestDestination);
        assertThat(response, isResponseOk());
        assertThat(response.getResult().getId(), not(equalTo(THE_DESTINATION_ID)));
    }

    @Test
    public void shouldAssociateToNewTenantIfIDAlreadyExistsWhenRegister() {
        newRestDestination.setTenant(otherTenant);
        ServiceResponse<RestDestination> response = subject.register(tenant, application, newRestDestination);
        assertThat(response, isResponseOk());
        assertThat(response.getResult().getTenant(), equalTo(tenant));
        assertThat(response.getResult().getId(), not(nullValue()));
        assertThat(response.getResult().getGuid(), not(nullValue()));
        assertThat(subject.getByGUID(otherTenant, otherApplication, response.getResult().getGuid()),
                hasErrorMessage(RestDestinationService.Validations.DESTINATION_NOT_FOUND.getCode()));
    }

    // ============================== update ==============================//
    @Test
    public void shouldSaveIfEverythingIsOkWhenUpdate() {
        RestDestination before = subject.getByGUID(tenant, application, THE_DESTINATION_GUID).getResult();
        assertThat(before.getName(), not(equalTo(UPDATED_DESTINATION_NAME)));

        oldRestDestination.setName(UPDATED_DESTINATION_NAME);

        ServiceResponse<RestDestination> response = subject.update(tenant, application, THE_DESTINATION_GUID, oldRestDestination);
        RestDestination returned = response.getResult();
        assertThat(response, isResponseOk());
        assertThat(returned.getId(), equalTo(THE_DESTINATION_ID));
        assertThat(returned.getTenant(), equalTo(tenant));
        assertThat(returned.getName(), equalTo(UPDATED_DESTINATION_NAME));

        RestDestination after = subject.getByGUID(tenant, application, THE_DESTINATION_GUID).getResult();
        assertThat(after.getName(), equalTo(UPDATED_DESTINATION_NAME));
    }


    @Test
    public void shouldIgnoreGUIDTenantAndIDInsideDataObjectWhenUpdate() {
        RestDestination before = subject.getByGUID(tenant, application, THE_DESTINATION_GUID).getResult();
        assertThat(before.getName(), not(equalTo(UPDATED_DESTINATION_NAME)));

        oldRestDestination.setName(UPDATED_DESTINATION_NAME);
        oldRestDestination.setId(INEXISTENT_DESTINATION_ID);
        oldRestDestination.setGuid(INEXISTENT_DESTINATION_GUID);
        oldRestDestination.setTenant(otherTenant);

        ServiceResponse<RestDestination> response = subject.update(tenant, application, THE_DESTINATION_GUID, oldRestDestination);
        RestDestination returned = response.getResult();
        assertThat(response, isResponseOk());
        assertThat(returned.getId(), equalTo(THE_DESTINATION_ID));
        assertThat(returned.getGuid(), equalTo(THE_DESTINATION_GUID));
        assertThat(returned.getTenant(), equalTo(tenant));
        assertThat(returned.getName(), equalTo(UPDATED_DESTINATION_NAME));

        RestDestination after = subject.getByGUID(tenant, application, THE_DESTINATION_GUID).getResult();
        assertThat(after.getName(), equalTo(UPDATED_DESTINATION_NAME));
    }

    @Test
    public void shouldReturnErrorIfOwnedByOtherTenantWhenUpdate() {
        RestDestination before = subject.getByGUID(otherTenant, otherApplication, OTHER_TENANT_DESTINATION_GUID).getResult();
        assertThat(before.getName(), not(equalTo(UPDATED_DESTINATION_NAME)));

        oldRestDestination.setId(OTHER_TENANT_DESTINATION_ID);
        oldRestDestination.setName(UPDATED_DESTINATION_NAME);

        ServiceResponse<RestDestination> response = subject.update(tenant, application, OTHER_TENANT_DESTINATION_GUID, oldRestDestination);
        assertThat(response,hasErrorMessage(RestDestinationService.Validations.DESTINATION_NOT_FOUND.getCode()));

        RestDestination after = subject.getByGUID(otherTenant, otherApplication, OTHER_TENANT_DESTINATION_GUID).getResult();
        assertThat(after.getName(), not(equalTo(UPDATED_DESTINATION_NAME)));
    }

    @Test
    public void shouldReturnErrorIfHasValidationErrorsWhenUpdate() {
        RestDestination before = subject.getByGUID(tenant, application, THE_DESTINATION_GUID).getResult();
        assertThat(before.getName(), not(equalTo(UPDATED_DESTINATION_NAME)));

        oldRestDestination.setName(UPDATED_DESTINATION_NAME);
        when(oldRestDestination.applyValidations()).thenReturn(
            Optional.of(new HashMap() {{
                put("some.error", new Object[] {"some_value"});
            }})
        );

        ServiceResponse<RestDestination> response = subject.update(tenant, application, THE_DESTINATION_GUID, oldRestDestination);
        assertThat(response, hasErrorMessage("some.error", new Object[]{"some_value"}));

        RestDestination after = subject.getByGUID(tenant, application, THE_DESTINATION_GUID).getResult();
        assertThat(after.getName(), not(equalTo(UPDATED_DESTINATION_NAME)));
    }

    @Test
    public void shouldReturnErrorIfTenantDoesNotExistWhenUpdate() {
        RestDestination before = subject.getByGUID(tenant, application, THE_DESTINATION_GUID).getResult();
        assertThat(before.getName(), not(equalTo(UPDATED_DESTINATION_NAME)));

        oldRestDestination.setName(UPDATED_DESTINATION_NAME);

        ServiceResponse<RestDestination> response = subject.update(inexistentTenant, inexistentApplication, THE_DESTINATION_GUID, oldRestDestination);
        assertThat(response,hasErrorMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()));

        RestDestination after = subject.getByGUID(tenant, application, THE_DESTINATION_GUID).getResult();
        assertThat(after.getName(), not(equalTo(UPDATED_DESTINATION_NAME)));
    }

    @Test
    public void shouldReturnErrorIfTenantIsNullWhenUpdate() {
        RestDestination before = subject.getByGUID(tenant, application, THE_DESTINATION_GUID).getResult();
        assertThat(before.getName(), not(equalTo(UPDATED_DESTINATION_NAME)));

        oldRestDestination.setName(UPDATED_DESTINATION_NAME);

        ServiceResponse<RestDestination> response = subject.update(null, null, THE_DESTINATION_GUID, oldRestDestination);
        assertThat(response,hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));

        RestDestination after = subject.getByGUID(tenant, application, THE_DESTINATION_GUID).getResult();
        assertThat(after.getName(), not(equalTo(UPDATED_DESTINATION_NAME)));
    }

    @Test
    public void shouldReturnErrorIfIDIsNullWhenUpdate() {
        RestDestination before = subject.getByGUID(tenant, application, THE_DESTINATION_GUID).getResult();
        assertThat(before.getName(), not(equalTo(UPDATED_DESTINATION_NAME)));

        oldRestDestination.setName(UPDATED_DESTINATION_NAME);

        ServiceResponse<RestDestination> response = subject.update(tenant, application, null, oldRestDestination);
        assertThat(response,hasErrorMessage(RestDestinationService.Validations.GUID_NULL.getCode()));

        RestDestination after = subject.getByGUID(tenant, application, THE_DESTINATION_GUID).getResult();
        assertThat(after.getName(), not(equalTo(UPDATED_DESTINATION_NAME)));
    }


    @Test
    public void shouldReturnErrorIfIDDoesNotExistWhenUpdate() {
        RestDestination before = subject.getByGUID(tenant, application, THE_DESTINATION_GUID).getResult();
        assertThat(before.getName(), not(equalTo(UPDATED_DESTINATION_NAME)));

        oldRestDestination.setName(UPDATED_DESTINATION_NAME);

        ServiceResponse<RestDestination> response = subject.update(tenant, application, INEXISTENT_DESTINATION_ID, oldRestDestination);
        assertThat(response,hasErrorMessage(RestDestinationService.Validations.DESTINATION_NOT_FOUND.getCode()));

        RestDestination after = subject.getByGUID(tenant, application, THE_DESTINATION_GUID).getResult();
        assertThat(after.getName(), not(equalTo(UPDATED_DESTINATION_NAME)));
    }

    @Test
    public void shouldReturnErrorIfNameIsDuplicateWhenUpdate() {
        RestDestination before = subject.getByGUID(tenant, application, THE_DESTINATION_GUID).getResult();
        assertThat(before.getName(), not(equalTo(OTHER_DESTINATION_NAME)));

        oldRestDestination.setName(OTHER_DESTINATION_NAME);

        ServiceResponse<RestDestination> response = subject.update(tenant, application, THE_DESTINATION_GUID, oldRestDestination);
        assertThat(response,hasErrorMessage(RestDestinationService.Validations.NAME_IN_USE.getCode()));

        RestDestination after = subject.getByGUID(tenant, application, THE_DESTINATION_GUID).getResult();
        assertThat(after.getName(), not(equalTo(OTHER_DESTINATION_NAME)));
    }

    @Test
    public void shouldRemoveDestinationSuccesfully() {
    	ServiceResponse<RestDestination> response = null;

    	RestDestination tempRestDestination = RestDestination.builder().name("LOMoHYKvTs").method("GET").serviceURI("http://host.com/").build();

    	// register
    	response = subject.register(tenant, application, tempRestDestination);
    	assertThat(response.isOk(), equalTo(true));

    	// remove
    	response = subject.remove(tenant, application, tempRestDestination.getGuid());
    	assertThat(response.isOk(), equalTo(true));

    }

    @Test
    public void shouldNotRemoveDestinationWithRouteInUse() {
    	ServiceResponse<RestDestination> response = null;

    	RestDestination tempRestDestination = RestDestination.builder().name("LOMoHYKvTs").method("GET").serviceURI("http://host.com/").build();

    	// register
    	response = subject.register(tenant, application, tempRestDestination);
    	assertThat(response.isOk(), equalTo(true));

    	// create a route to the rest destination
    	EventRoute route = EventRoute.builder().description("Kj4xqmJQUC").name("2sVevJm0qq").outgoing(RouteActor.builder().uri(tempRestDestination.toURI()).build()).build();
		eventRouteRepository.save(route);

    	// try to remove
    	response = subject.remove(tenant, application, tempRestDestination.getGuid());
    	assertThat(response.isOk(), equalTo(false));
        assertThat(response.getResponseMessages(),
                hasEntry(RestDestinationService.Validations.REST_DESTINATION_IN_USE_ROUTE.getCode(), null));

    }

    @Test
    public void shouldReturnErrorIfDestinationMethodInvalidWhenRegister() {
        newRestDestination.setMethod("XGET");
        ServiceResponse<RestDestination> response = subject.register(tenant, application, newRestDestination);
        assertThat(response, hasErrorMessage(RestDestinationService.Validations.METHOD_INVALID.getCode()));
        assertThat(newRestDestination.getId(), nullValue());
    }

}
