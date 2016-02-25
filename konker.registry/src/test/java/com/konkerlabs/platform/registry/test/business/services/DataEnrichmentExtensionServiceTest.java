package com.konkerlabs.platform.registry.test.business.services;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.util.Arrays;
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

import com.konkerlabs.platform.registry.business.model.DataEnrichmentExtension;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.repositories.DataEnrichmentExtensionRepository;
import com.konkerlabs.platform.registry.business.services.api.DataEnrichmentExtensionService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.test.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoTestConfiguration.class, BusinessTestConfiguration.class })
@UsingDataSet(locations = { "/fixtures/tenants.json", "/fixtures/enrichment-rest.json" })
public class DataEnrichmentExtensionServiceTest extends BusinessLayerTestSupport {

    private static final String INEXISTENT_TENANT_ID = "INEXISTENT_TENANT_ID";
    private static final String OLD_ENCHRICHMENT_EXTENSION_NAME = "REST-from-Prestashop-01";
    private static final String OLD_ENCHRICHMENT_EXTENSION_ID = "a09b3f34-db24-11e5-8a31-7b3889d9b0eb";
    private static final String INEXISTENT_ENCHRICHMENT_EXTENSION_NAME = "INEXISTENT_NAME";
    private static final String NEW_ENCHRICHMENT_EXTENSION_NAME = "REST-from-Amazon-01";

    private Tenant inexistentTenant;
    private Tenant aTenant;
    private Tenant emptyTenant;

    private DataEnrichmentExtension oldDataEnrichmentExtension;
    private DataEnrichmentExtension newDataEnrichmentExtension;

    @Autowired
    private DataEnrichmentExtensionService service;

    @Autowired
    private DataEnrichmentExtensionRepository repository;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setup() {
        inexistentTenant = Tenant.builder().id(INEXISTENT_TENANT_ID).build();
        aTenant = Tenant.builder().id("71fb0d48-674b-4f64-a3e5-0256ff3a63af").name("Konker").build();
        emptyTenant = Tenant.builder().name("EmptyTenant").build();
        oldDataEnrichmentExtension = spy(
                DataEnrichmentExtension.builder().name(OLD_ENCHRICHMENT_EXTENSION_NAME).build());
        newDataEnrichmentExtension = spy(DataEnrichmentExtension.builder().name(NEW_ENCHRICHMENT_EXTENSION_NAME)
                .type(DataEnrichmentExtension.EnrichmentType.REST).incoming(URI.create("device://xx")).build());

    }

    //============================== register ==============================//
    @Test
    public void shouldReturnErrorMessageIfTenantIsInexistentWhenRegister() {
        ServiceResponse<DataEnrichmentExtension> response = service.register(inexistentTenant,
                newDataEnrichmentExtension);
        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(), hasItem("Tenant does not exist"));
    }

    @Test
    public void shouldReturnErrorMessageIfTenantIsNullWhenRegister() {
        ServiceResponse<DataEnrichmentExtension> response = service.register(null, newDataEnrichmentExtension);
        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(), hasItem("Tenant cannot be null"));
    }

    @Test
    public void shouldReturnErrorMessageIfDataEnrichmentIsNullWhenRegister() {
        ServiceResponse<DataEnrichmentExtension> response = service.register(aTenant, null);
        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(), hasItem("Data Enrichment Extension cannot be null"));
    }

    @Test
    public void shouldReturnErrorMessageIfNameIsDuplicatedWhenRegister() {
        newDataEnrichmentExtension.setName(OLD_ENCHRICHMENT_EXTENSION_NAME);

        ServiceResponse<DataEnrichmentExtension> response = service.register(aTenant, newDataEnrichmentExtension);
        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(), hasItem("Data Enrichment Extension Name must be unique"));
    }

    @Test
    public void shouldReturnErrorMessageIfValidationFailsWhenRegister() {
        when(newDataEnrichmentExtension.applyValidations()).thenReturn((Arrays.asList("Error 1", "Error 2")));

        ServiceResponse<DataEnrichmentExtension> response = service.register(aTenant, newDataEnrichmentExtension);
        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(), hasItems("Error 1", "Error 2"));
    }

    @Test
    public void shouldReturnDataEnrichmentExtensionIfValidWhenRegister() {
        newDataEnrichmentExtension.setTenant(inexistentTenant);
        newDataEnrichmentExtension.setId("XX");

        ServiceResponse<DataEnrichmentExtension> response = service.register(aTenant, newDataEnrichmentExtension);
        assertThat(response.getResponseMessages(), empty());
        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.OK));
        assertThat(response.getResult(), not(nullValue()));

        assertThat(response.getResult().getTenant(), equalTo(aTenant));
        assertThat(response.getResult().getName(), equalTo(NEW_ENCHRICHMENT_EXTENSION_NAME));
        assertThat(response.getResult().getId(), not(nullValue()));
    }

    //============================== getAll ==============================//

    @Test
    public void shouldReturnErrorMessageIfTenantIsInexistentWhenGetAll() {
        ServiceResponse<List<DataEnrichmentExtension>> response = service.getAll(inexistentTenant);
        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(), hasItem("Tenant does not exist"));
    }

    @Test
    public void shouldReturnErrorMessageIfTenantIsNullWhenGetAll() {
        ServiceResponse<List<DataEnrichmentExtension>> response = service.getAll(null);
        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(), hasItem("Tenant cannot be null"));
    }

    @Test
    public void shouldReturnEmptyListIfNoItemExistWhenGetAll() {
        ServiceResponse<List<DataEnrichmentExtension>> response = service.getAll(emptyTenant);
        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.OK));
        assertThat(response.getResult(), empty());
        assertThat(response.getResponseMessages(), empty());
    }

    @Test
    public void shouldReturnItemsIfTenantIsValidWhenGetAll() {
        ServiceResponse<List<DataEnrichmentExtension>> response = service.getAll(aTenant);
        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.OK));
        assertThat(response.getResult(), not(empty()));
        assertThat(response.getResponseMessages(), empty());

        List<String> foundIds = response.getResult().stream().map(DataEnrichmentExtension::getName)
                .collect(Collectors.toList());
        assertThat(foundIds, hasItems("REST-from-Prestashop-01", "REST-from-Magento-01"));

    }

    //============================== findByName ==============================//

    @Test
    public void shouldReturnErrorMessageIfTenantIsInexistentWhenFindByName() {
        ServiceResponse<DataEnrichmentExtension> response = service.getByName(inexistentTenant,
                OLD_ENCHRICHMENT_EXTENSION_NAME);
        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(), hasItem("Tenant does not exist"));
    }

    @Test
    public void shouldReturnErrorMessageIfTenantIsNullWhenFindByName() {
        ServiceResponse<DataEnrichmentExtension> response = service.getByName(null, OLD_ENCHRICHMENT_EXTENSION_NAME);
        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(), hasItem("Tenant cannot be null"));
    }

    @Test
    public void shouldReturnErrorMessageIfIdIsNullWhenFindByName() {
        ServiceResponse<DataEnrichmentExtension> response = service.getByName(aTenant, null);
        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(), hasItem("Name cannot be null"));
    }

    @Test
    public void shouldReturnErrorMessageIfDoesNotExist() {
        ServiceResponse<DataEnrichmentExtension> response = service.getByName(aTenant,
                INEXISTENT_ENCHRICHMENT_EXTENSION_NAME);
        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(), hasItem("Data Enrichment Extension does not exist"));
    }

    @Test
    public void shouldReturnErrorMessageIfNameExistsInAnotherTenant() {
        ServiceResponse<DataEnrichmentExtension> response = service.getByName(emptyTenant,
                OLD_ENCHRICHMENT_EXTENSION_NAME);
        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(), hasItem("Data Enrichment Extension does not exist"));
    }

    @Test
    public void shouldReturnRightExtensionIfIsValid() {
        ServiceResponse<DataEnrichmentExtension> response = service.getByName(aTenant,
                OLD_ENCHRICHMENT_EXTENSION_NAME);
        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.OK));
        assertThat(response.getResult().getId(), equalTo(OLD_ENCHRICHMENT_EXTENSION_ID));
        assertThat(response.getResponseMessages(), empty());
    }

}
