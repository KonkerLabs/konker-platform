package com.konkerlabs.platform.registry.test.business.services;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.empty;

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
    private static final String A_DATA_ENCHRICHMENT_EXTENSION_NAME = "REST-from-Prestashop-01";
    private static final String A_DATA_ENCHRICHMENT_EXTENSION_ID = "a09b3f34-db24-11e5-8a31-7b3889d9b0eb";
    private static final String INEXISTENT_ENCHRICHMENT_EXTENSION_NAME = "INEXISTENT_NAME";

    private Tenant inexistentTenant;
    private DataEnrichmentExtension dataEnrichmentExtension;

    private static final Tenant A_TENANT = Tenant.builder().name("Konker").build();
    private static final Tenant EMPTY_TENANT = Tenant.builder().name("EmptyTenant").build();

    @Autowired
    private DataEnrichmentExtensionService service;

    @Autowired
    private DataEnrichmentExtensionRepository repository;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setup() {
        inexistentTenant = Tenant.builder().id(INEXISTENT_TENANT_ID).build();
    }

    @Test
    public void shouldReturnErrorMessageIfTenantIsInexistentWhenFindByName() {
        ServiceResponse<DataEnrichmentExtension> response = service.findByName(inexistentTenant,
                A_DATA_ENCHRICHMENT_EXTENSION_NAME);
        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(), hasItem("Tenant does not exist"));
    }

    @Test
    public void shouldReturnErrorMessageIfTenantIsNullWhenFindByName() {
        ServiceResponse<DataEnrichmentExtension> response = service.findByName(null,
                A_DATA_ENCHRICHMENT_EXTENSION_NAME);
        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(), hasItem("Tenant cannot be null"));
    }

    @Test
    public void shouldReturnErrorMessageIfIdIsNullWhenFindByName() {
        ServiceResponse<DataEnrichmentExtension> response = service.findByName(A_TENANT, null);
        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(), hasItem("Name cannot be null"));
    }

    @Test
    public void shouldReturnErrorMessageIfDoesNotExist() {
        ServiceResponse<DataEnrichmentExtension> response = service.findByName(A_TENANT,
                INEXISTENT_ENCHRICHMENT_EXTENSION_NAME);
        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(), hasItem("Data Enrichment Extension does not exist"));
    }
    
    @Test
    public void shouldReturnErrorMessageIfNameExistsInAnotherTenant() {
        ServiceResponse<DataEnrichmentExtension> response = service.findByName(EMPTY_TENANT,
                A_DATA_ENCHRICHMENT_EXTENSION_NAME);
        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(), hasItem("Data Enrichment Extension does not exist"));
    }

    @Test
    public void shouldReturnRightExtensionIfIsValid() {
        ServiceResponse<DataEnrichmentExtension> response = service.findByName(A_TENANT,
                A_DATA_ENCHRICHMENT_EXTENSION_NAME);
        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.OK));
        assertThat(response.getResult().getId(), equalTo(A_DATA_ENCHRICHMENT_EXTENSION_ID));
        assertThat(response.getResponseMessages(), empty());
    }

}
