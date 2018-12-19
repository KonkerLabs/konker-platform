package com.konkerlabs.platform.registry.test.business.services;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.ApplicationRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.PrivateStorageService;
import com.konkerlabs.platform.registry.business.services.api.PrivateStorageService.Validations;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.storage.model.PrivateStorage;
import com.konkerlabs.platform.registry.test.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoPrivateStorageTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import com.mongodb.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Set;

import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.hasErrorMessage;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        MongoTestConfiguration.class,
        MongoPrivateStorageTestConfiguration.class,
        BusinessTestConfiguration.class
})
@UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
public class PrivateStorageServiceTest extends BusinessLayerTestSupport {

    @Autowired
    private PrivateStorageService privateStorageService;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    private Tenant tenant;
    private Tenant tenantInm;
    private Application application;

    private String jsonA = "{ \"_id\": \"adfe-12356-ffde\", \"barCode\" : \"00000\" }";
    private String jsonB = "{\"_id\": \"adfe-123654-ffab\", \"a\": 2}";
    private String jsonNoId = "{\"key1\": \"adfe-123654-ffab\", \"a\": 2}";

    @Before
    public void setUp() {
    	tenant = tenantRepository.findByDomainName("konker");
    	tenantInm = tenantRepository.findByDomainName("inm");
    	application = applicationRepository.findByTenantAndName(tenant.getId(), "smartffkonker");
    }

    @Test
    public void shouldReturnErrorIfSavingWithNullTenant() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.save(null, application, "collectionA", jsonA);
        assertThat(serviceResponse, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
    }

    @Test
    public void shouldReturnErrorIfSavingWithNullApplication() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.save(tenant, null, "collectionA", jsonA);
        assertThat(serviceResponse, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));

    }

    @Test
    public void shouldReturnErrorIfSavingWithNullCollectionName() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.save(tenant, application, null, jsonA);
        assertThat(serviceResponse, hasErrorMessage(Validations.PRIVATE_STORAGE_INVALID_COLLECTION_NAME.getCode()));

    }

    @Test
    public void shouldReturnErrorIfSavingWithInvalidCollectionName() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.save(tenant, application, "collection/A", jsonA);
        assertThat(serviceResponse, hasErrorMessage(Validations.PRIVATE_STORAGE_INVALID_COLLECTION_NAME.getCode()));

    }

    @Test
    public void shouldReturnErrorIfSavingNullJson() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.save(tenant, application, "collectionA", null);
        assertThat(serviceResponse, hasErrorMessage(Validations.PRIVATE_STORAGE_INVALID_JSON.getCode()));

    }

    @Test
    public void shouldReturnErrorIfSavingInvalidJson() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.save(tenant, application, "collectionA", "a=4");
        assertThat(serviceResponse, hasErrorMessage(Validations.PRIVATE_STORAGE_INVALID_JSON.getCode()));

    }

    @Test
    public void shouldReturnErrorIfSavingInStorageFull() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.save(tenantInm, application, "collectionA", jsonA);
        assertThat(serviceResponse, hasErrorMessage(Validations.PRIVATE_STORAGE_IS_FULL.getCode()));

    }

    @Test
    public void shouldReturnErrorIfSavingContentNoIdField() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.save(tenant, application, "collectionA", jsonNoId);
        assertThat(serviceResponse, hasErrorMessage(Validations.PRIVATE_STORAGE_NO_COLLECTION_ID_FIELD.getCode()));

    }

    @Test
    public void shouldReturnErrorIfSaveExistingCollectionContent() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.save(tenant, application, "collectionA", jsonA);
        assertThat(serviceResponse.isOk(), is(true));

        serviceResponse = privateStorageService.save(tenant, application, "collectionA", jsonA);
        assertThat(serviceResponse, hasErrorMessage(Validations.PRIVATE_STORAGE_COLLECTION_CONTENT_ALREADY_EXISTS.getCode()));

    }

    @Test
    public void shouldSaveApplicationDocumentStore() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.save(tenant, application, "collectionA", jsonA);
        assertThat(serviceResponse.isOk(), is(true));

    }

//
//    @Test
//    public void shouldUpdateApplicationDocumentStore() throws Exception {
//
//        assertThat(applicationDocumentStoreRepository.findUniqueByTenantIdApplicationName(tenant.getId(), application.getName(), "collectionB", "key1").getJson(), is(jsonB));
//
//        ServiceResponse<ApplicationDocumentStore> serviceResponse = privateStorageService.update(tenant, application, "collectionB", "key1", jsonA);
//        assertThat(serviceResponse.isOk(), is(true));
//
//        assertThat(applicationDocumentStoreRepository.findUniqueByTenantIdApplicationName(tenant.getId(), application.getName(), "collectionB", "key1").getJson(), is(jsonA));
//
//    }
//
//    @Test
//    public void shouldTryUpdateNonExistingApplicationDocumentStore() throws Exception {
//
//        ServiceResponse<ApplicationDocumentStore> serviceResponse = privateStorageService.update(tenant, application, "collectionA", "key1", jsonA);
//        assertThat(serviceResponse, hasErrorMessage(Validations.APP_DOCUMENT_DOES_NOT_EXIST.getCode()));
//
//    }
//
//    @Test
//    public void shouldRemoveApplicationDocumentStore() throws Exception {
//
//        assertThat(applicationDocumentStoreRepository.findUniqueByTenantIdApplicationName(tenant.getId(), application.getName(), "collectionB", "key1"), notNullValue());
//
//        ServiceResponse<ApplicationDocumentStore> serviceResponse = privateStorageService.remove(tenant, application, "collectionB", "key1");
//        assertThat(serviceResponse.isOk(), is(true));
//
//        assertThat(applicationDocumentStoreRepository.findUniqueByTenantIdApplicationName(tenant.getId(), application.getName(), "collectionB", "key1"), nullValue());
//
//    }
//
//    @Test
//    public void shouldTryToRemoveNonExistingApplicationDocumentStore() throws Exception {
//
//        ServiceResponse<ApplicationDocumentStore> serviceResponse = privateStorageService.remove(tenant, application, "collectionA", "key1");
//        assertThat(serviceResponse, hasErrorMessage(Validations.APP_DOCUMENT_DOES_NOT_EXIST.getCode()));
//
//    }
//
//    @Test
//    public void shouldFindByTenantAndApplicationAndModelAndLocation() throws Exception {
//
//        ServiceResponse<ApplicationDocumentStore> serviceResponse = privateStorageService.findUniqueByTenantApplication(tenant, application, "collectionB", "key1");
//        assertThat(serviceResponse.isOk(), is(true));
//        assertThat(serviceResponse.getResult().getJson(), is(jsonB));
//
//    }
//
//    @Test
//    public void shouldFindNonExistingByTenantAndApplicationAndModelAndLocation() throws Exception {
//
//        ServiceResponse<ApplicationDocumentStore> serviceResponse = privateStorageService.findUniqueByTenantApplication(tenant, application, "collectionA", "key1");
//        assertThat(serviceResponse, hasErrorMessage(Validations.APP_DOCUMENT_DOES_NOT_EXIST.getCode()));
//
//    }

}