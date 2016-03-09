package com.konkerlabs.platform.registry.test.business.services;

import com.konkerlabs.platform.registry.business.model.DataEnrichmentExtension;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.behaviors.DeviceURIDealer;
import com.konkerlabs.platform.registry.business.model.enumerations.IntegrationType;
import com.konkerlabs.platform.registry.business.repositories.DataEnrichmentExtensionRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.DataEnrichmentExtensionService;
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
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoTestConfiguration.class, BusinessTestConfiguration.class })
@UsingDataSet(locations = { "/fixtures/tenants.json", "/fixtures/enrichment-rest.json" })
public class DataEnrichmentExtensionServiceTest extends BusinessLayerTestSupport {

    private static final String INEXISTENT_TENANT_ID = "INEXISTENT_TENANT_ID";
    private static final String OLD_ENCHRICHMENT_EXTENSION_NAME = "REST-from-Prestashop-01";
    private static final String OLD_ENCHRICHMENT_EXTENSION_ID = "a09b3f34-db24-11e5-8a31-7b3889d9b0eb";
    private static final String INEXISTENT_ENCHRICHMENT_EXTENSION_NAME = "INEXISTENT_NAME";
    private static final String NEW_ENCHRICHMENT_EXTENSION_NAME = "REST-from-Amazon-01";
    private static final String DEVICE_ID_IN_USE = "abc123";
    private static final String CONTAINER_KEY_IN_USE = "magentoData";
    private static final String OLD_INCOMING_URI = "device://konker/abc123";
    private static final String INEXISTENT_INCOMING_URI = "device://konker/999";

    private Tenant inexistentTenant;
    private Tenant aTenant;
    private Tenant emptyTenant;

    private DataEnrichmentExtension oldDataEnrichmentExtension;
    private DataEnrichmentExtension newDataEnrichmentExtension;

    private URI oldIncomingUri;
    private URI inexistentIncomingUri;

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
                .incoming(new DeviceURIDealer(){}.toDeviceRouteURI(aTenant.getDomainName(),"xx")).build());

        oldIncomingUri = new URI(OLD_INCOMING_URI);
        inexistentIncomingUri = new URI(INEXISTENT_INCOMING_URI);
    }

    // ============================== register ==============================//
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
    public void shouldReturnErrorMessageIfContainerKeyIsDuplicatedWhenRegister() {
        newDataEnrichmentExtension.setIncoming(new DeviceURIDealer(){}.toDeviceRouteURI(
            aTenant.getDomainName(),
            DEVICE_ID_IN_USE
        ));
        newDataEnrichmentExtension.setContainerKey(CONTAINER_KEY_IN_USE);

        ServiceResponse<DataEnrichmentExtension> response = service.register(aTenant, newDataEnrichmentExtension);
        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(), hasItem("Container key already registered for incoming device"));
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

    // ============================== update ==============================//
    @Test
    public void shouldReturnErrorMessageIfTenantIsInexistentWhenUpdate() {
        ServiceResponse<DataEnrichmentExtension> response = service.update(inexistentTenant,
                oldDataEnrichmentExtension);
        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(), hasItem("Tenant does not exist"));
    }

    @Test
    public void shouldReturnErrorMessageIfTenantIsNullWhenUddate() {
        ServiceResponse<DataEnrichmentExtension> response = service.update(null, oldDataEnrichmentExtension);
        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(), hasItem("Tenant cannot be null"));
    }

    @Test
    public void shouldReturnErrorMessageIfDataEnrichmentIsNullWhenUpdate() {
        ServiceResponse<DataEnrichmentExtension> response = service.update(aTenant, null);
        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(), hasItem("Data Enrichment Extension cannot be null"));
    }

    @Test
    public void shouldReturnErrorMessageIfNameDoesNotExistWhenUpdate() {
        ServiceResponse<DataEnrichmentExtension> response = service.update(aTenant, newDataEnrichmentExtension);
        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(), hasItem("Data Enrichment Extension does not exist"));
    }

    @Test
    public void shouldReturnErrorMessageIfValidationFailsWhenUpdate() {
        when(oldDataEnrichmentExtension.applyValidations()).thenReturn((Arrays.asList("Error 1", "Error 2")));

        ServiceResponse<DataEnrichmentExtension> response = service.update(aTenant, oldDataEnrichmentExtension);
        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(), hasItems("Error 1", "Error 2"));
    }

    @Test
    public void shouldReturnErrorMessageIfContainerKeyIsDuplicatedWhenUpdating() {
        oldDataEnrichmentExtension = enrichmentExtensionRepository.findOne(OLD_ENCHRICHMENT_EXTENSION_ID);

        oldDataEnrichmentExtension.setContainerKey(CONTAINER_KEY_IN_USE);

        ServiceResponse<DataEnrichmentExtension> response = service.update(aTenant, oldDataEnrichmentExtension);
        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(), hasItem("Container key already registered for incoming device"));
    }

    @Test
    public void shouldReturnDataEnrichmentExtensionIfValidWhenUpdate() {
        DataEnrichmentExtension x = DataEnrichmentExtension.builder().name(OLD_ENCHRICHMENT_EXTENSION_NAME).tenant(aTenant)
                .description("New Description").containerKey("New Container Key")
                .incoming(URI.create("device://newdevice")).parameter("a", "b").active(true).type(IntegrationType.REST)
                .build();


        ServiceResponse<DataEnrichmentExtension> response = service.update(aTenant, x);
        assertThat(response.getResponseMessages(), empty());
        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.OK));
        assertThat(response.getResult(), not(nullValue()));

        assertThat(response.getResult().getTenant(), equalTo(aTenant));
        assertThat(response.getResult().getName(), equalTo(OLD_ENCHRICHMENT_EXTENSION_NAME));
        assertThat(response.getResult().getDescription(), equalTo("New Description"));
        assertThat(response.getResult().getId(), equalTo(OLD_ENCHRICHMENT_EXTENSION_ID));
    }

    // ============================== getAll ==============================//

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

    // ============================== getByName ==============================//

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
        ServiceResponse<DataEnrichmentExtension> response = service.getByName(aTenant, OLD_ENCHRICHMENT_EXTENSION_NAME);
        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.OK));
        assertThat(response.getResult().getId(), equalTo(OLD_ENCHRICHMENT_EXTENSION_ID));
        assertThat(response.getResponseMessages(), empty());
    }


    // ============================== getByTenantAndByIncomingURI ==============================//
    @Test
    public void shouldReturnErrorMessageIfTenantIsInexistentWhenFindByTenantAndIncomingURI() {
        ServiceResponse<List<DataEnrichmentExtension>> response = service.getByTenantAndByIncomingURI(inexistentTenant,
                oldIncomingUri);
        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(), hasItem("Tenant does not exist"));
    }

    @Test
    public void shouldReturnErrorMessageIfTenantIsNullWhenFindByTenantAndIncomingURI() {
        ServiceResponse<List<DataEnrichmentExtension>> response = service.getByTenantAndByIncomingURI(null, oldIncomingUri);
        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(), hasItem("Tenant cannot be null"));
    }

    @Test
    public void shouldReturnErrorMessageIfIdIsNullWhenFindByTenantAndIncomingURI() {
        ServiceResponse<List<DataEnrichmentExtension>> response = service.getByTenantAndByIncomingURI(aTenant, null);
        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(), hasItem("Incoming URI cannot be null"));
    }

    @Test
    public void shouldReturnEmptyListIfDoesNotExistIncomingURI() {
        ServiceResponse<List<DataEnrichmentExtension>> response = service.getByTenantAndByIncomingURI(aTenant,
                inexistentIncomingUri);
        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.OK));
        assertThat(response.getResult(), notNullValue());
        assertThat(response.getResult(), hasSize(0));
    }

    @Test
    public void shouldReturnRightExtensionIfIsValidWhenFindByTenantAndIncomingURI() {
        ServiceResponse<List<DataEnrichmentExtension>> response = service.getByTenantAndByIncomingURI(aTenant, oldIncomingUri);
        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.OK));
        assertThat(response.getResult().get(0).getId(), equalTo(OLD_ENCHRICHMENT_EXTENSION_ID));
        assertThat(response.getResponseMessages(), empty());
    }

}