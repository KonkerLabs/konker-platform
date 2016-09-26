package com.konkerlabs.platform.registry.test.business.services;

import com.konkerlabs.platform.registry.business.model.DataEnrichmentExtension;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.behaviors.DeviceURIDealer;
import com.konkerlabs.platform.registry.business.model.enumerations.IntegrationType;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.DataEnrichmentExtensionRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.DataEnrichmentExtensionService;
import com.konkerlabs.platform.registry.business.services.api.NewServiceResponse;
import com.konkerlabs.platform.registry.test.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.konkerlabs.platform.registry.test.base.matchers.NewServiceResponseMatchers.isResponseOk;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoTestConfiguration.class, BusinessTestConfiguration.class })
@UsingDataSet(locations = { "/fixtures/tenants.json", "/fixtures/enrichment-rest.json" })
public class DataEnrichmentExtensionServiceTest extends BusinessLayerTestSupport {

    private static final String INEXISTENT_TENANT_ID = "INEXISTENT_TENANT_ID";
    private static final String OLD_ENCHRICHMENT_EXTENSION_NAME = "REST-from-Prestashop-01-Test";
    private static final String OLD_ENCHRICHMENT_EXTENSION_ID = "a09b3f34-db24-11e5-8a31-7b3889d9b0eb";
    private static final String INEXISTENT_ENCHRICHMENT_EXTENSION_GUID = "INEXISTENT_GUID";
    private static final String NEW_ENCHRICHMENT_EXTENSION_NAME = "REST-from-Amazon-01";
    private static final String DEVICE_GUID_IN_USE = "8d51c242-81db-11e6-a8c2-0746f010e945";
    private static final String CONTAINER_KEY_IN_USE = "magentoData";
    private static final String OLD_INCOMING_URI = "device://konker/8d51c242-81db-11e6-a8c2-0746f010e945";
    private static final String INEXISTENT_INCOMING_URI = "device://konker/999";
    private Tenant inexistentTenant;

    private Tenant aTenant;
    private Tenant emptyTenant;
    private DataEnrichmentExtension oldDataEnrichmentExtension;

    private DataEnrichmentExtension newDataEnrichmentExtension;
    private URI oldIncomingUri;

    private URI inexistentIncomingUri;

    private String enrichmentGuid;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private DataEnrichmentExtensionService service;

    @Autowired
    private DataEnrichmentExtensionRepository repository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private DataEnrichmentExtensionRepository enrichmentExtensionRepository;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setup() throws URISyntaxException {
        inexistentTenant = Tenant.builder().id(INEXISTENT_TENANT_ID).build();
        aTenant = tenantRepository.findByDomainName("konker");
        emptyTenant = Tenant.builder().name("EmptyTenant").build();
        oldDataEnrichmentExtension = spy(
                DataEnrichmentExtension.builder().name(OLD_ENCHRICHMENT_EXTENSION_NAME).build());
        newDataEnrichmentExtension = spy(DataEnrichmentExtension.builder().name(NEW_ENCHRICHMENT_EXTENSION_NAME)
                .type(IntegrationType.REST)
                .containerKey("containerKey")
                .parameter(DataEnrichmentExtension.URL,"http://host/path")
                .parameter(DataEnrichmentExtension.USERNAME,"")
                .parameter(DataEnrichmentExtension.PASSWORD,"")
                .incoming(new DeviceURIDealer(){}.toDeviceRouteURI(aTenant.getDomainName(),"xx")).build());

        oldIncomingUri = new URI(OLD_INCOMING_URI);
        inexistentIncomingUri = new URI(INEXISTENT_INCOMING_URI);
        enrichmentGuid = "aac07163-4192-4db2-89a8-6f2b2aac514c";
    }

    // ============================== register ==============================//
    @Test
    public void shouldReturnErrorMessageIfTenantIsInexistentWhenRegister() {
        NewServiceResponse<DataEnrichmentExtension> response = service.register(inexistentTenant,
                newDataEnrichmentExtension);
        assertThat(response.getStatus(), equalTo(NewServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(), hasEntry(CommonValidations.TENANT_DOES_NOT_EXIST.getCode(),null));
    }

    @Test
    public void shouldReturnErrorMessageIfTenantIsNullWhenRegister() {
        NewServiceResponse<DataEnrichmentExtension> response = service.register(null, newDataEnrichmentExtension);
        assertThat(response.getStatus(), equalTo(NewServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(), hasEntry(CommonValidations.TENANT_NULL.getCode(),null));
    }

    @Test
    public void shouldReturnErrorMessageIfDataEnrichmentIsNullWhenRegister() {
        NewServiceResponse<DataEnrichmentExtension> response = service.register(aTenant, null);
        assertThat(response.getStatus(), equalTo(NewServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(), hasEntry(CommonValidations.RECORD_NULL.getCode(),null));
    }

    @Test
    public void shouldReturnErrorMessageIfNameIsDuplicatedWhenRegister() {
        newDataEnrichmentExtension.setName("REST-from-Prestashop-01");

        NewServiceResponse<DataEnrichmentExtension> response = service.register(aTenant, newDataEnrichmentExtension);
        assertThat(response.getStatus(), equalTo(NewServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(),
                hasEntry(DataEnrichmentExtensionService.Validations.ENRICHMENT_NAME_UNIQUE.getCode(),null));
    }

    @Test
    public void shouldReturnErrorMessageIfContainerKeyIsDuplicatedWhenRegister() {
        newDataEnrichmentExtension.setIncoming(new DeviceURIDealer(){}.toDeviceRouteURI(
            aTenant.getDomainName(),
            DEVICE_GUID_IN_USE
        ));
        newDataEnrichmentExtension.setContainerKey(CONTAINER_KEY_IN_USE);

        NewServiceResponse<DataEnrichmentExtension> response = service.register(aTenant, newDataEnrichmentExtension);
        assertThat(response.getStatus(), equalTo(NewServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(),
                hasEntry(DataEnrichmentExtensionService.Validations.ENRICHMENT_CONTAINER_KEY_ALREADY_REGISTERED.getCode(),null));
    }

    @Test
    public void shouldReturnErrorMessageIfValidationFailsWhenRegister() {
        when(newDataEnrichmentExtension.applyValidations()).thenReturn(
            Optional.of(new HashMap<String,Object[]>() {{
                put("some.error",new Object[] {"some_value"});
            }})
        );

        NewServiceResponse<DataEnrichmentExtension> response = service.register(aTenant, newDataEnrichmentExtension);
        assertThat(response.getStatus(), equalTo(NewServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(), hasEntry("some.error", new Object[] {"some_value"}));
    }

    @Test
    public void shouldReturnDataEnrichmentExtensionIfValidWhenRegister() {
        newDataEnrichmentExtension.setTenant(inexistentTenant);
        newDataEnrichmentExtension.setId("XX");

        NewServiceResponse<DataEnrichmentExtension> response = service.register(aTenant, newDataEnrichmentExtension);
        assertThat(response.getResponseMessages().isEmpty(), is(true));
        assertThat(response.getStatus(), equalTo(NewServiceResponse.Status.OK));
        assertThat(response.getResult(), not(nullValue()));

        assertThat(response.getResult().getTenant(), equalTo(aTenant));
        assertThat(response.getResult().getName(), equalTo(NEW_ENCHRICHMENT_EXTENSION_NAME));
        assertThat(response.getResult().getId(), not(nullValue()));
    }

    // ============================== update ==============================//
    @Test
    public void shouldReturnErrorMessageIfTenantIsInexistentWhenUpdate() {
        NewServiceResponse<DataEnrichmentExtension> response = service.update(inexistentTenant, enrichmentGuid,
                oldDataEnrichmentExtension);
        assertThat(response.getStatus(), equalTo(NewServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(), hasEntry(CommonValidations.TENANT_DOES_NOT_EXIST.getCode(),null));
    }

    @Test
    public void shouldReturnErrorMessageIfTenantIsNullWhenUddate() {
        NewServiceResponse<DataEnrichmentExtension> response = service.update(null, enrichmentGuid, oldDataEnrichmentExtension);
        assertThat(response.getStatus(), equalTo(NewServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(), hasEntry(CommonValidations.TENANT_NULL.getCode(),null));
    }

    @Test
    public void shouldReturnErrorMessageIfDataEnrichmentIsNullWhenUpdate() {
        NewServiceResponse<DataEnrichmentExtension> response = service.update(aTenant, enrichmentGuid, null);
        assertThat(response.getStatus(), equalTo(NewServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(), hasEntry(CommonValidations.RECORD_NULL.getCode(),null));
    }

    @Test
    public void shouldReturnErrorMessageIfGUIDDoesNotExistWhenUpdate() {
        enrichmentGuid = "999999";
        NewServiceResponse<DataEnrichmentExtension> response = service.update(aTenant, enrichmentGuid, newDataEnrichmentExtension);
        assertThat(response.getStatus(), equalTo(NewServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(),
                hasEntry(DataEnrichmentExtensionService.Validations.ENRICHMENT_NOT_FOUND.getCode(),null));
    }

    @Test
    public void shouldReturnErrorMessageIfValidationFailsWhenUpdate() {
        when(oldDataEnrichmentExtension.applyValidations()).thenReturn(
            Optional.of(new HashMap<String,Object[]>() {{
                put("some.error",new Object[] {"some_value"});
            }})
        );

        NewServiceResponse<DataEnrichmentExtension> response = service.update(aTenant, enrichmentGuid, oldDataEnrichmentExtension);
        assertThat(response.getStatus(), equalTo(NewServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(), hasEntry("some.error", new Object[] {"some_value"}));
    }

    @Test
    public void shouldReturnErrorMessageIfContainerKeyIsDuplicatedWhenUpdating() {
        oldDataEnrichmentExtension = enrichmentExtensionRepository.findOne(OLD_ENCHRICHMENT_EXTENSION_ID);
        oldDataEnrichmentExtension.setGuid("aac07163-4192-4db2-89a8-6f2b2aac514b");
        oldDataEnrichmentExtension.setName("REST-from-Magento-01-updated");
        oldDataEnrichmentExtension.setContainerKey("prestashopData");

        NewServiceResponse<DataEnrichmentExtension> response = service.update(aTenant, enrichmentGuid, oldDataEnrichmentExtension);
        assertThat(response.getStatus(), equalTo(NewServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(),
                hasEntry(DataEnrichmentExtensionService.Validations.ENRICHMENT_CONTAINER_KEY_ALREADY_REGISTERED.getCode(),null));
    }

    @Test
    public void shouldReturnDataEnrichmentExtensionIfValidWhenUpdate() {
        DataEnrichmentExtension x = DataEnrichmentExtension.builder().name(OLD_ENCHRICHMENT_EXTENSION_NAME).tenant(aTenant)
                .description("New Description").containerKey("New Container Key")
                .incoming(URI.create("device://newdevice"))
                .parameter(DataEnrichmentExtension.URL, "http://host:8089/path")
                .parameter(DataEnrichmentExtension.USERNAME,"")
                .parameter(DataEnrichmentExtension.PASSWORD,"")
                .active(true).type(IntegrationType.REST)
                .build();

        NewServiceResponse<DataEnrichmentExtension> response = service.update(aTenant, enrichmentGuid, x);
        assertThat(response.getResponseMessages().isEmpty(), is(true));
        assertThat(response.getStatus(), equalTo(NewServiceResponse.Status.OK));
        assertThat(response.getResult(), not(nullValue()));

        assertThat(response.getResult().getTenant(), equalTo(aTenant));
        assertThat(response.getResult().getName(), equalTo(OLD_ENCHRICHMENT_EXTENSION_NAME));
        assertThat(response.getResult().getDescription(), equalTo("New Description"));
        assertThat(response.getResult().getGuid(), equalTo(enrichmentGuid));
    }

    // ============================== getAll ==============================//

    @Test
    public void shouldReturnErrorMessageIfTenantIsInexistentWhenGetAll() {
        NewServiceResponse<List<DataEnrichmentExtension>> response = service.getAll(inexistentTenant);
        assertThat(response.getStatus(), equalTo(NewServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(), hasEntry(CommonValidations.TENANT_DOES_NOT_EXIST.getCode(),null));
    }

    @Test
    public void shouldReturnErrorMessageIfTenantIsNullWhenGetAll() {
        NewServiceResponse<List<DataEnrichmentExtension>> response = service.getAll(null);
        assertThat(response.getStatus(), equalTo(NewServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(), hasEntry(CommonValidations.TENANT_NULL.getCode(),null));
    }

    @Test
    public void shouldReturnEmptyListIfNoItemExistWhenGetAll() {
        NewServiceResponse<List<DataEnrichmentExtension>> response = service.getAll(emptyTenant);
        assertThat(response.getStatus(), equalTo(NewServiceResponse.Status.OK));
        assertThat(response.getResult(), empty());
        assertThat(response.getResponseMessages().isEmpty(), is(true));
    }

    @Test
    public void shouldReturnItemsIfTenantIsValidWhenGetAll() {
        NewServiceResponse<List<DataEnrichmentExtension>> response = service.getAll(aTenant);
        assertThat(response.getStatus(), equalTo(NewServiceResponse.Status.OK));
        assertThat(response.getResult(), not(empty()));
        assertThat(response.getResponseMessages().isEmpty(), is(true));

        List<String> foundIds = response.getResult().stream().map(DataEnrichmentExtension::getName)
                .collect(Collectors.toList());
        assertThat(foundIds, hasItems("REST-from-Prestashop-01", "REST-from-Magento-01"));

    }

    // ============================== getByGUID ==============================//

    @Test
    public void shouldReturnErrorMessageIfTenantIsInexistentWhenFindByGUID() {
        NewServiceResponse<DataEnrichmentExtension> response = service.getByGUID(inexistentTenant,
                enrichmentGuid);
        assertThat(response.getStatus(), equalTo(NewServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(), hasEntry(CommonValidations.TENANT_DOES_NOT_EXIST.getCode(),null));
    }

    @Test
    public void shouldReturnErrorMessageIfTenantIsNullWhenFindByGUID() {
        NewServiceResponse<DataEnrichmentExtension> response = service.getByGUID(null, enrichmentGuid);
        assertThat(response.getStatus(), equalTo(NewServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(), hasEntry(CommonValidations.TENANT_NULL.getCode(),null));
    }

    @Test
    public void shouldReturnErrorMessageIfIdIsNullWhenFindByGUID() {
        NewServiceResponse<DataEnrichmentExtension> response = service.getByGUID(aTenant, null);
        assertThat(response.getStatus(), equalTo(NewServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(),
                hasEntry(DataEnrichmentExtensionService.Validations.ENRICHMENT_ID_NULL.getCode(),null));
    }

    @Test
    public void shouldReturnErrorMessageIfDoesNotExist() {
        NewServiceResponse<DataEnrichmentExtension> response = service.getByGUID(aTenant,
                INEXISTENT_ENCHRICHMENT_EXTENSION_GUID);
        assertThat(response.getStatus(), equalTo(NewServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(),
                hasEntry(DataEnrichmentExtensionService.Validations.ENRICHMENT_NOT_FOUND.getCode(),null));
    }

    // TODO: This test must be reviewed
    @Test
    @Ignore
    public void shouldReturnErrorMessageIfGUIDExistsInAnotherTenant() {
        NewServiceResponse<DataEnrichmentExtension> response = service.getByGUID(emptyTenant,
                enrichmentGuid);
        assertThat(response.getStatus(), equalTo(NewServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
//        assertThat(response.getResponseMessages(), hasItem("Data Enrichment Extension does not exist"));
    }

    @Test
    public void shouldReturnRightExtensionIfIsValid() {
        NewServiceResponse<DataEnrichmentExtension> response = service.getByGUID(aTenant, enrichmentGuid);
        assertThat(response.getStatus(), equalTo(NewServiceResponse.Status.OK));
        assertThat(response.getResult().getGuid(), equalTo(enrichmentGuid));
        assertThat(response.getResponseMessages().isEmpty(), is(true));
    }


    // ============================== getByTenantAndByIncomingURI ==============================//
    @Test
    public void shouldReturnErrorMessageIfTenantIsInexistentWhenFindByTenantAndIncomingURI() {
        NewServiceResponse<List<DataEnrichmentExtension>> response = service.getByTenantAndByIncomingURI(inexistentTenant,
                oldIncomingUri);
        assertThat(response.getStatus(), equalTo(NewServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(), hasEntry(CommonValidations.TENANT_DOES_NOT_EXIST.getCode(),null));
    }

    @Test
    public void shouldReturnErrorMessageIfTenantIsNullWhenFindByTenantAndIncomingURI() {
        NewServiceResponse<List<DataEnrichmentExtension>> response = service.getByTenantAndByIncomingURI(null, oldIncomingUri);
        assertThat(response.getStatus(), equalTo(NewServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(), hasEntry(CommonValidations.TENANT_NULL.getCode(),null));
    }

    @Test
    public void shouldReturnErrorMessageIfIdIsNullWhenFindByTenantAndIncomingURI() {
        NewServiceResponse<List<DataEnrichmentExtension>> response = service.getByTenantAndByIncomingURI(aTenant, null);
        assertThat(response.getStatus(), equalTo(NewServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(),
                hasEntry(DataEnrichmentExtensionService.Validations.ENRICHMENT_INCOMING_URI_NULL.getCode(),null));
    }

    @Test
    public void shouldReturnEmptyListIfDoesNotExistIncomingURI() {
        NewServiceResponse<List<DataEnrichmentExtension>> response = service.getByTenantAndByIncomingURI(aTenant,
                inexistentIncomingUri);
        assertThat(response.getStatus(), equalTo(NewServiceResponse.Status.OK));
        assertThat(response.getResult(), notNullValue());
        assertThat(response.getResult(), hasSize(0));
    }

    @Test
    public void shouldReturnRightExtensionIfIsValidWhenFindByTenantAndIncomingURI() {
        NewServiceResponse<List<DataEnrichmentExtension>> response = service.getByTenantAndByIncomingURI(aTenant, oldIncomingUri);
        assertThat(response.getStatus(), equalTo(NewServiceResponse.Status.OK));
        assertThat(response.getResult().get(0).getId(), equalTo(OLD_ENCHRICHMENT_EXTENSION_ID));
        assertThat(response.getResponseMessages().isEmpty(), is(true));
    }

    // ============================== remove ==============================//
    @Test
    public void shouldReturnErrorMessageIfTenantIsInexistentWhenRemove() {
        NewServiceResponse<DataEnrichmentExtension> response = service.remove(inexistentTenant, enrichmentGuid);
        assertThat(response.getStatus(), equalTo(NewServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(), hasEntry(CommonValidations.TENANT_DOES_NOT_EXIST.getCode(),null));
    }

    @Test
    public void shouldReturnErrorMessageIfTenantIsNullWhenRemove() {
        NewServiceResponse<DataEnrichmentExtension> response = service.remove(null, enrichmentGuid);
        assertThat(response.getStatus(), equalTo(NewServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(), hasEntry(CommonValidations.TENANT_NULL.getCode(),null));
    }

    @Test
    public void shouldReturnErrorMessageIfDataEnrichmentIsNullWhenRemove() {
        NewServiceResponse<DataEnrichmentExtension> response = service.remove(aTenant, null);
        assertThat(response.getStatus(), equalTo(NewServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(),
                hasEntry(DataEnrichmentExtensionService.Validations.ENRICHMENT_ID_NULL.getCode(),null));
    }

    @Test
    public void shouldReturnErrorMessageIfDataEnrichmentIsEmptyWhenRemove() {
        NewServiceResponse<DataEnrichmentExtension> response = service.remove(aTenant, "");
        assertThat(response.getStatus(), equalTo(NewServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(),
                hasEntry(DataEnrichmentExtensionService.Validations.ENRICHMENT_ID_NULL.getCode(),null));
    }

    @Test
    public void shouldReturnErrorMessageIfGUIDDoesNotExistWhenRemove() {
        enrichmentGuid = "999999";
        NewServiceResponse<DataEnrichmentExtension> response = service.update(aTenant, enrichmentGuid, newDataEnrichmentExtension);
        assertThat(response.getStatus(), equalTo(NewServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(),
                hasEntry(DataEnrichmentExtensionService.Validations.ENRICHMENT_NOT_FOUND.getCode(),null));
    }

    @Test
    public void shouldRemoveSuccessfully() throws Exception {
        NewServiceResponse<DataEnrichmentExtension> response = service.remove(aTenant,enrichmentGuid);

        DataEnrichmentExtension removedRoute = service.getByGUID(aTenant, enrichmentGuid).getResult();

        assertThat(response,isResponseOk());
        assertThat(response.getResult(),notNullValue());
        assertThat(response.getResult().getGuid(),equalTo(enrichmentGuid));

        assertThat(removedRoute, nullValue());
    }
}