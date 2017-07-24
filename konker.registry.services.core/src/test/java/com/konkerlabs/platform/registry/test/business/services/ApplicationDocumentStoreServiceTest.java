package com.konkerlabs.platform.registry.test.business.services;

import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.hasErrorMessage;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.ApplicationDocumentStore;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.ApplicationDocumentStoreRepository;
import com.konkerlabs.platform.registry.business.repositories.ApplicationRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.ApplicationDocumentStoreService;
import com.konkerlabs.platform.registry.business.services.api.ApplicationDocumentStoreService.Validations;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.test.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        MongoTestConfiguration.class,
        BusinessTestConfiguration.class
})
@UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
public class ApplicationDocumentStoreServiceTest extends BusinessLayerTestSupport {

    @Autowired
    private ApplicationDocumentStoreService applicationDocumentStoreService;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ApplicationDocumentStoreRepository applicationDocumentStoreRepository;

    private Tenant tenant;
    private Application application;

    private ApplicationDocumentStore customDataB;

    private String jsonA = "{ 'barCode' : '00000' }";
    private String jsonB = "{'a': 2}";

    @Before
    public void setUp() {
    	tenant = tenantRepository.findByName("Konker");
    	application = applicationRepository.findByTenantAndName(tenant.getId(), "smartffkonker");

        customDataB = ApplicationDocumentStore.builder()
                               .tenant(tenant)
                               .application(application)
                               .collection("collectionB")
                               .key("key1")
                               .json(jsonB)
                               .build();
        customDataB = applicationDocumentStoreRepository.save(customDataB);

    }

    @Test
    public void shouldReturnErrorIfSavingWithNullTenant() throws Exception {

        ServiceResponse<ApplicationDocumentStore> serviceResponse = applicationDocumentStoreService.save(null, application, "collectionA", "key1", jsonA);
        assertThat(serviceResponse, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));

    }

    @Test
    public void shouldReturnErrorIfSavingWithNullApplication() throws Exception {

        ServiceResponse<ApplicationDocumentStore> serviceResponse = applicationDocumentStoreService.save(tenant, null, "collectionA", "key1", jsonA);
        assertThat(serviceResponse, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));

    }

    @Test
    public void shouldReturnErrorIfSavingNullJson() throws Exception {

        ServiceResponse<ApplicationDocumentStore> serviceResponse = applicationDocumentStoreService.save(tenant, application, "collectionA", "key1", null);
        assertThat(serviceResponse, hasErrorMessage(Validations.APP_DOCUMENT_DATA_INVALID_JSON.getCode()));

    }

    @Test
    public void shouldReturnErrorIfSavingInvalidJson() throws Exception {

        ServiceResponse<ApplicationDocumentStore> serviceResponse = applicationDocumentStoreService.save(tenant, application, "collectionA", "key1", "a=4");
        assertThat(serviceResponse, hasErrorMessage(Validations.APP_DOCUMENT_DATA_INVALID_JSON.getCode()));

    }

    @Test
    public void shouldSaveApplicationDocumentStore() throws Exception {

        ServiceResponse<ApplicationDocumentStore> serviceResponse = applicationDocumentStoreService.save(tenant, application, "collectionA", "key1", jsonA);
        assertThat(serviceResponse.isOk(), is(true));

    }

    @Test
    public void shouldReturnErrorIfSaveExistingApplicationDocumentStore() throws Exception {

        ServiceResponse<ApplicationDocumentStore> serviceResponse = applicationDocumentStoreService.save(tenant, application, "collectionA", "key1", jsonA);
        assertThat(serviceResponse.isOk(), is(true));

        serviceResponse = applicationDocumentStoreService.save(tenant, application, "collectionA", "key1", jsonA);
        assertThat(serviceResponse, hasErrorMessage(Validations.APP_DOCUMENT_DATA_ALREADY_REGISTERED.getCode()));

    }

    @Test
    public void shouldReturnErrorIfUpdateApplicationDocumentStoreWithInvalidJson() throws Exception {

        ServiceResponse<ApplicationDocumentStore> serviceResponse = applicationDocumentStoreService.update(tenant, application, "collectionB", "key1", "a=4");
        assertThat(serviceResponse, hasErrorMessage(Validations.APP_DOCUMENT_DATA_INVALID_JSON.getCode()));

    }

    @Test
    public void shouldUpdateApplicationDocumentStore() throws Exception {

        assertThat(applicationDocumentStoreRepository.findUniqueByTenantIdApplicationName(tenant.getId(), application.getName(), "collectionB", "key1").getJson(), is(jsonB));

        ServiceResponse<ApplicationDocumentStore> serviceResponse = applicationDocumentStoreService.update(tenant, application, "collectionB", "key1", jsonA);
        assertThat(serviceResponse.isOk(), is(true));

        assertThat(applicationDocumentStoreRepository.findUniqueByTenantIdApplicationName(tenant.getId(), application.getName(), "collectionB", "key1").getJson(), is(jsonA));

    }

    @Test
    public void shouldTryUpdateNonExistingApplicationDocumentStore() throws Exception {

        ServiceResponse<ApplicationDocumentStore> serviceResponse = applicationDocumentStoreService.update(tenant, application, "collectionA", "key1", jsonA);
        assertThat(serviceResponse, hasErrorMessage(Validations.APP_DOCUMENT_DATA_DOES_NOT_EXIST.getCode()));

    }

    @Test
    public void shouldRemoveApplicationDocumentStore() throws Exception {

        assertThat(applicationDocumentStoreRepository.findUniqueByTenantIdApplicationName(tenant.getId(), application.getName(), "collectionB", "key1"), notNullValue());

        ServiceResponse<ApplicationDocumentStore> serviceResponse = applicationDocumentStoreService.remove(tenant, application, "collectionB", "key1");
        assertThat(serviceResponse.isOk(), is(true));

        assertThat(applicationDocumentStoreRepository.findUniqueByTenantIdApplicationName(tenant.getId(), application.getName(), "collectionB", "key1"), nullValue());

    }

    @Test
    public void shouldTryToRemoveNonExistingApplicationDocumentStore() throws Exception {

        ServiceResponse<ApplicationDocumentStore> serviceResponse = applicationDocumentStoreService.remove(tenant, application, "collectionA", "key1");
        assertThat(serviceResponse, hasErrorMessage(Validations.APP_DOCUMENT_DATA_DOES_NOT_EXIST.getCode()));

    }

    @Test
    public void shouldFindByTenantAndApplicationAndModelAndLocation() throws Exception {

        ServiceResponse<ApplicationDocumentStore> serviceResponse = applicationDocumentStoreService.findUniqueByTenantApplication(tenant, application, "collectionB", "key1");
        assertThat(serviceResponse.isOk(), is(true));
        assertThat(serviceResponse.getResult().getJson(), is(jsonB));

    }

    @Test
    public void shouldFindNonExistingByTenantAndApplicationAndModelAndLocation() throws Exception {

        ServiceResponse<ApplicationDocumentStore> serviceResponse = applicationDocumentStoreService.findUniqueByTenantApplication(tenant, application, "collectionA", "key1");
        assertThat(serviceResponse, hasErrorMessage(Validations.APP_DOCUMENT_DATA_DOES_NOT_EXIST.getCode()));

    }

}