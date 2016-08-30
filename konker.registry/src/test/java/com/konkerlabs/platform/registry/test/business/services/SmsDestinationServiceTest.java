package com.konkerlabs.platform.registry.test.business.services;

import com.konkerlabs.platform.registry.business.model.SmsDestination;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.NewServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.SmsDestinationService;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.konkerlabs.platform.registry.test.base.matchers.NewServiceResponseMatchers.hasErrorMessage;
import static com.konkerlabs.platform.registry.test.base.matchers.NewServiceResponseMatchers.isResponseOk;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoTestConfiguration.class, BusinessTestConfiguration.class })
@UsingDataSet(locations = { "/fixtures/tenants.json", "/fixtures/sms-destinations.json" })
public class SmsDestinationServiceTest extends BusinessLayerTestSupport {

    @Rule
    public ExpectedException thrown = none();

    @Autowired
    private TenantRepository tenantRepository;

    public static final String THE_DESTINATION_ID = "9cfb75c6-99c9-4452-b7de-8d82cfd4916f";
    public static final String THE_DESTINATION_GUID = "140307f9-7d50-4f37-ac67-80313776bef4";
    public static final String THE_DESTINATION_NAME = "First destination";
    public static final String OTHER_DESTINATION_NAME = "Second destination";
    public static final String OTHER_TENANT_DESTINATION_ID = "8ce19ed1-8a19-400c-935d-1de2a5425b04";
    public static final String OTHER_TENANT_DESTINATION_GUID = "c746732d-571c-475d-9452-97197a3a1f8a";
    public static final String OTHER_TENANT_DESTINATION_NAME = "Third destination";
    public static final String INEXISTENT_DESTINATION_ID = UUID.randomUUID().toString();
    public static final String INEXISTENT_DESTINATION_GUID = UUID.randomUUID().toString();
    public static final String UPDATED_DESTINATION_NAME = "updated restful destination";

    @Autowired
    private SmsDestinationService subject;

    private Tenant tenant;
    private Tenant emptyTenant;
    private Tenant otherTenant;
    private Tenant inexistentTenant;
    private SmsDestination newSmsDestination;
    private SmsDestination oldSmsDestination;

    @Before
    public void setUp() {
        emptyTenant = tenantRepository.findByName("EmptyTenant");
        tenant = tenantRepository.findByName("Konker");
        otherTenant = tenantRepository.findByName("InMetrics");
        inexistentTenant = Tenant.builder().domainName("someInexistentDomain")
                .id("e2bfa8b0-eaf5-11e5-8fd5-a755d49a5c5b").name("someInexistentName").build();

        newSmsDestination = spy(
                SmsDestination.builder().name("New Name").active(true).description("Description")
                        .phoneNumber("+12008765432").build());
        oldSmsDestination = spy(SmsDestination.builder().id(THE_DESTINATION_ID).name(THE_DESTINATION_NAME)
                .tenant(tenant).active(false).phoneNumber("+5511987654321")
                .guid(THE_DESTINATION_GUID).build());

    }

    // ============================== findAll ==============================//
    @Test
    public void shouldReturnEmptyListIfDestinationsDoesNotExistWhenFindAll() {
        NewServiceResponse<List<SmsDestination>> response = subject.findAll(emptyTenant);
        assertThat(response, isResponseOk());
        assertThat(response.getResult(), empty());
    }

    @Test
    public void shouldReturnErrorMessageIfTenantDoesNotExistWhenFindAll() {
        NewServiceResponse<List<SmsDestination>> response = subject.findAll(inexistentTenant);
        assertThat(response, hasErrorMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()));
    }

    @Test
    public void shouldReturnErrorMessageIfTenantIsNullWhenFindAll() {
        NewServiceResponse<List<SmsDestination>> response = subject.findAll(null);
        assertThat(response, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
    }

    @Test
    public void shouldReturnDestinationsWhenFindAll() {
        NewServiceResponse<List<SmsDestination>> response = subject.findAll(tenant);
        assertThat(response, isResponseOk());
        assertThat(response.getResult(), hasSize(greaterThan(1)));

        List<String> ids = response.getResult().stream().map(SmsDestination::getId).collect(Collectors.toList());
        assertThat(ids, hasItem(THE_DESTINATION_ID));
        assertThat(ids, not(hasItem(OTHER_TENANT_DESTINATION_ID)));
    }

    @Test
    public void shouldReturnDestinationsWhenOtherTenantFindAll() {
        NewServiceResponse<List<SmsDestination>> response = subject.findAll(otherTenant);
        assertThat(response, isResponseOk());
        assertThat(response.getResult(), not(empty()));

        List<String> ids = response.getResult().stream().map(SmsDestination::getId).collect(Collectors.toList());
        assertThat(ids, not(hasItem(THE_DESTINATION_ID)));
        assertThat(ids, hasItem(OTHER_TENANT_DESTINATION_ID));
    }

    // ============================== getByID ==============================//

    @Test
    public void shouldReturnDestinationIfExistsWithinTenantWhenGetByID() {
        NewServiceResponse<SmsDestination> response = subject.getByGUID(tenant, THE_DESTINATION_GUID);
        assertThat(response, isResponseOk());
        assertThat(response.getResult().getName(), equalTo(THE_DESTINATION_NAME));
    }

    @Test
    public void shouldReturnOtherDestinationIfExistsWithinOtherTenantWhenGetByID() {
        NewServiceResponse<SmsDestination> response = subject.getByGUID(otherTenant, OTHER_TENANT_DESTINATION_GUID);
        assertThat(response, isResponseOk());
        assertThat(response.getResult().getName(), equalTo(OTHER_TENANT_DESTINATION_NAME));
    }

    @Test
    public void shouldReturnErrorIfDestinationIsOwnedByAnotherTenantWhenGetByID() {
        NewServiceResponse<SmsDestination> response = subject.getByGUID(tenant, OTHER_TENANT_DESTINATION_ID);
        assertThat(response, hasErrorMessage(SmsDestinationService.Validations.SMSDEST_NOT_FOUND.getCode()));
    }

    @Test
    public void shouldReturnErrorIfDestinationDoesNotExistWhenGetByID() {
        NewServiceResponse<SmsDestination> response = subject.getByGUID(tenant, INEXISTENT_DESTINATION_ID);
        assertThat(response, hasErrorMessage(SmsDestinationService.Validations.SMSDEST_NOT_FOUND.getCode()));
    }

    @Test
    public void shouldReturnErrorIfTenantIsNullWhenGetByID() {
        NewServiceResponse<SmsDestination> response = subject.getByGUID(null, THE_DESTINATION_ID);
        assertThat(response, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
    }

    @Test
    public void shouldReturnErrorIfIDIsNullWhenGetByID() {
        NewServiceResponse<SmsDestination> response = subject.getByGUID(tenant, null);
        assertThat(response, hasErrorMessage(SmsDestinationService.Validations.SMSDEST_ID_NULL.getCode()));
    }

    // ============================== register ==============================//

    @Test
    public void shouldRegisterIfEverythingIsOkWhenRegister() {
        assertThat(newSmsDestination.getId(), nullValue());
        NewServiceResponse<SmsDestination> response = subject.register(tenant, newSmsDestination);
        assertThat(response, isResponseOk());
        assertThat(response.getResult().getId(), not(nullValue()));
        assertThat(response.getResult().getTenant(), equalTo(tenant));
        assertThat(response.getResult().isActive(), equalTo(Boolean.TRUE));
    }

    @Test
    public void shouldReturnErrorIfValidationsFailWhenRegister() {
        Optional<Map<String, Object[]>> errors = Optional.of(new HashMap<String, Object[]>());
        errors.get().put("Error Message", null);

        doReturn(errors).when(newSmsDestination).applyValidations();
        NewServiceResponse<SmsDestination> response = subject.register(tenant, newSmsDestination);
        assertThat(response, hasErrorMessage("Error Message"));
        assertThat(newSmsDestination.getId(), nullValue());
    }

    @Test
    public void shouldReturnErrorIfTenantIsNullWhenRegister() {
        NewServiceResponse<SmsDestination> response = subject.register(null, newSmsDestination);
        assertThat(response, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
        assertThat(newSmsDestination.getId(), nullValue());
    }

    @Test
    public void shouldReturnErrorIfTenantInexistentWhenRegister() {
        NewServiceResponse<SmsDestination> response = subject.register(inexistentTenant, newSmsDestination);
        assertThat(response, hasErrorMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()));
        assertThat(newSmsDestination.getId(), nullValue());
    }

    @Test
    public void shouldReturnErrorIfDestinatioIsNullWhenRegister() {
        NewServiceResponse<SmsDestination> response = subject.register(inexistentTenant, null);
        assertThat(response, hasErrorMessage(CommonValidations.RECORD_NULL.getCode()));
        assertThat(newSmsDestination.getId(), nullValue());
    }

    @Test
    public void shouldReturnErrorIfDestinationExistsWhenRegister() {
        newSmsDestination.setName(THE_DESTINATION_NAME);
        NewServiceResponse<SmsDestination> response = subject.register(tenant, newSmsDestination);
        assertThat(response, hasErrorMessage(SmsDestinationService.Validations.SMSDEST_NAME_UNIQUE.getCode()));
        assertThat(newSmsDestination.getId(), nullValue());
    }

    @Test
    public void shouldGenerateNewIdIfIDAlreadyExistsWhenRegister() {
        newSmsDestination.setId(THE_DESTINATION_ID);
        NewServiceResponse<SmsDestination> response = subject.register(tenant, newSmsDestination);
        assertThat(response, isResponseOk());
        assertThat(response.getResult().getId(), not(equalTo(THE_DESTINATION_ID)));
    }

    @Test
    public void shouldAssociateToNewTenantIfIDAlreadyExistsWhenRegister() {
        newSmsDestination.setTenant(otherTenant);
        NewServiceResponse<SmsDestination> response = subject.register(tenant, newSmsDestination);
        assertThat(response, isResponseOk());
        assertThat(response.getResult().getTenant(), equalTo(tenant));
        assertThat(response.getResult().getId(), not(nullValue()));
        assertThat(response.getResult().getGuid(), not(nullValue()));
        assertThat(subject.getByGUID(otherTenant, response.getResult().getGuid()),
                hasErrorMessage(SmsDestinationService.Validations.SMSDEST_NOT_FOUND.getCode()));
    }

    // ============================== update ==============================//
    @Test
    public void shouldSaveIfEverythingIsOkWhenUpdate() {
        SmsDestination before = subject.getByGUID(tenant, THE_DESTINATION_GUID).getResult();
        assertThat(before.getName(), not(equalTo(UPDATED_DESTINATION_NAME)));

        oldSmsDestination.setName(UPDATED_DESTINATION_NAME);

        NewServiceResponse<SmsDestination> response = subject.update(tenant, THE_DESTINATION_GUID, oldSmsDestination);
        SmsDestination returned = response.getResult();
        assertThat(response, isResponseOk());
        assertThat(returned.getId(), equalTo(THE_DESTINATION_ID));
        assertThat(returned.getTenant(), equalTo(tenant));
        assertThat(returned.getName(), equalTo(UPDATED_DESTINATION_NAME));

        SmsDestination after = subject.getByGUID(tenant, THE_DESTINATION_GUID).getResult();
        assertThat(after.getName(), equalTo(UPDATED_DESTINATION_NAME));
    }


    @Test
    public void shouldIgnoreGUIDTenantAndIDInsideDataObjectWhenUpdate() {
        SmsDestination before = subject.getByGUID(tenant, THE_DESTINATION_GUID).getResult();
        assertThat(before.getName(), not(equalTo(UPDATED_DESTINATION_NAME)));

        oldSmsDestination.setName(UPDATED_DESTINATION_NAME);
        oldSmsDestination.setId(INEXISTENT_DESTINATION_ID);
        oldSmsDestination.setGuid(INEXISTENT_DESTINATION_GUID);
        oldSmsDestination.setTenant(otherTenant);

        NewServiceResponse<SmsDestination> response = subject.update(tenant, THE_DESTINATION_GUID, oldSmsDestination);
        SmsDestination returned = response.getResult();
        assertThat(response, isResponseOk());
        assertThat(returned.getId(), equalTo(THE_DESTINATION_ID));
        assertThat(returned.getGuid(), equalTo(THE_DESTINATION_GUID));
        assertThat(returned.getTenant(), equalTo(tenant));
        assertThat(returned.getName(), equalTo(UPDATED_DESTINATION_NAME));

        SmsDestination after = subject.getByGUID(tenant, THE_DESTINATION_GUID).getResult();
        assertThat(after.getName(), equalTo(UPDATED_DESTINATION_NAME));
    }

    @Test
    public void shouldReturnErrorIfOwnedByOtherTenantWhenUpdate() {
        SmsDestination before = subject.getByGUID(otherTenant, OTHER_TENANT_DESTINATION_GUID).getResult();
        assertThat(before.getName(), not(equalTo(UPDATED_DESTINATION_NAME)));

        oldSmsDestination.setId(OTHER_TENANT_DESTINATION_ID);
        oldSmsDestination.setName(UPDATED_DESTINATION_NAME);

        NewServiceResponse<SmsDestination> response = subject.update(tenant, OTHER_TENANT_DESTINATION_GUID, oldSmsDestination);
        assertThat(response, hasErrorMessage(SmsDestinationService.Validations.SMSDEST_NOT_FOUND.getCode()));

        SmsDestination after = subject.getByGUID(otherTenant, OTHER_TENANT_DESTINATION_GUID).getResult();
        assertThat(after.getName(), not(equalTo(UPDATED_DESTINATION_NAME)));
    }

    @Test
    public void shouldReturnErrorIfHasValidationErrorsWhenUpdate() {
        SmsDestination before = subject.getByGUID(tenant, THE_DESTINATION_GUID).getResult();
        assertThat(before.getName(), not(equalTo(UPDATED_DESTINATION_NAME)));

        oldSmsDestination.setName(UPDATED_DESTINATION_NAME);
        Optional<Map<String, Object[]>> errors = Optional.of(new HashMap<String, Object[]>());
        errors.get().put("My Error", null);
        
        when(oldSmsDestination.applyValidations()).thenReturn(errors);

        NewServiceResponse<SmsDestination> response = subject.update(tenant, THE_DESTINATION_GUID, oldSmsDestination);
        assertThat(response, hasErrorMessage("My Error"));

        SmsDestination after = subject.getByGUID(tenant, THE_DESTINATION_GUID).getResult();
        assertThat(after.getName(), not(equalTo(UPDATED_DESTINATION_NAME)));
    }

    @Test
    public void shouldReturnErrorIfTenantDoesNotExistWhenUpdate() {
        SmsDestination before = subject.getByGUID(tenant, THE_DESTINATION_GUID).getResult();
        assertThat(before.getName(), not(equalTo(UPDATED_DESTINATION_NAME)));

        oldSmsDestination.setName(UPDATED_DESTINATION_NAME);

        NewServiceResponse<SmsDestination> response = subject.update(inexistentTenant, THE_DESTINATION_GUID, oldSmsDestination);
        assertThat(response, hasErrorMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()));

        SmsDestination after = subject.getByGUID(tenant, THE_DESTINATION_GUID).getResult();
        assertThat(after.getName(), not(equalTo(UPDATED_DESTINATION_NAME)));
    }

    @Test
    public void shouldReturnErrorIfTenantIsNullWhenUpdate() {
        SmsDestination before = subject.getByGUID(tenant, THE_DESTINATION_GUID).getResult();
        assertThat(before.getName(), not(equalTo(UPDATED_DESTINATION_NAME)));

        oldSmsDestination.setName(UPDATED_DESTINATION_NAME);

        NewServiceResponse<SmsDestination> response = subject.update(null, THE_DESTINATION_GUID, oldSmsDestination);
        assertThat(response, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));

        SmsDestination after = subject.getByGUID(tenant, THE_DESTINATION_GUID).getResult();
        assertThat(after.getName(), not(equalTo(UPDATED_DESTINATION_NAME)));
    }

    @Test
    public void shouldReturnErrorIfIDIsNullWhenUpdate() {
        SmsDestination before = subject.getByGUID(tenant, THE_DESTINATION_GUID).getResult();
        assertThat(before.getName(), not(equalTo(UPDATED_DESTINATION_NAME)));

        oldSmsDestination.setName(UPDATED_DESTINATION_NAME);

        NewServiceResponse<SmsDestination> response = subject.update(tenant, null, oldSmsDestination);
        assertThat(response, hasErrorMessage(SmsDestinationService.Validations.SMSDEST_ID_NULL.getCode()));

        SmsDestination after = subject.getByGUID(tenant, THE_DESTINATION_GUID).getResult();
        assertThat(after.getName(), not(equalTo(UPDATED_DESTINATION_NAME)));
    }


    @Test
    public void shouldReturnErrorIfIDDoesNotExistWhenUpdate() {
        SmsDestination before = subject.getByGUID(tenant, THE_DESTINATION_GUID).getResult();
        assertThat(before.getName(), not(equalTo(UPDATED_DESTINATION_NAME)));

        oldSmsDestination.setName(UPDATED_DESTINATION_NAME);

        NewServiceResponse<SmsDestination> response = subject.update(tenant, INEXISTENT_DESTINATION_ID, oldSmsDestination);
        assertThat(response, hasErrorMessage(SmsDestinationService.Validations.SMSDEST_NOT_FOUND.getCode()));

        SmsDestination after = subject.getByGUID(tenant, THE_DESTINATION_GUID).getResult();
        assertThat(after.getName(), not(equalTo(UPDATED_DESTINATION_NAME)));
    }

    @Test
    public void shouldReturnErrorIfNameIsDuplicateWhenUpdate() {
        SmsDestination before = subject.getByGUID(tenant, THE_DESTINATION_GUID).getResult();
        assertThat(before.getName(), not(equalTo(OTHER_DESTINATION_NAME)));

        oldSmsDestination.setName(OTHER_DESTINATION_NAME);

        NewServiceResponse<SmsDestination> response = subject.update(tenant, THE_DESTINATION_ID, oldSmsDestination);
        assertThat(response, hasErrorMessage(SmsDestinationService.Validations.SMSDEST_NAME_UNIQUE.getCode()));

        SmsDestination after = subject.getByGUID(tenant, THE_DESTINATION_GUID).getResult();
        assertThat(after.getName(), not(equalTo(OTHER_DESTINATION_NAME)));
    }
}