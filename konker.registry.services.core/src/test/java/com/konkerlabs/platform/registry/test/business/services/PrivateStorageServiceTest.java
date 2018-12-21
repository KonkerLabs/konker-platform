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
import com.konkerlabs.platform.registry.storage.repositories.PrivateStorageRepository;
import com.konkerlabs.platform.registry.test.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoPrivateStorageTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import com.mongodb.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.hasErrorMessage;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        MongoTestConfiguration.class,
        MongoPrivateStorageTestConfiguration.class,
        BusinessTestConfiguration.class
})
@UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
public class PrivateStorageServiceTest extends BusinessLayerTestSupport {

    public static final String COLLECTION_NAME = "konkerCollection";

    @Autowired
    private PrivateStorageService privateStorageService;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired()
    @Qualifier("mongoPrivateStorageTemplate")
    private MongoTemplate mongoTemplate;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private Mongo mongo;

    private PrivateStorageRepository privateStorageRepository;

    private Tenant tenant;
    private Tenant tenantInm;
    private Application application;

    private String jsonA = "{\"_id\":\"adba7f77-33cb-4e77-8560-288d273e7add\",\"barCode\":\"00001\",\"desc\":\"sensor desc\"}";
    private String jsonB = "{\"_id\": \"adba7f77-33cb-4e77-8560-288d273e7ass\", \"a\": 2}";
    private String jsonC = "{\"_id\":\"adba7f77-33cb-4e77-8560-288d273e7add\",\"barCode\":\"00000\"}";
    private String jsonNoId = "{\"key1\": \"adba7f77-33cb-4e77-8560-288d273e7aee\", \"a\": 2}";

    @Before
    public void setUp() {
    	tenant = tenantRepository.findByDomainName("konker");
    	tenantInm = tenantRepository.findByDomainName("inm");
    	application = applicationRepository.findByTenantAndName(tenant.getId(), "smartffkonker");

    }

    @After
    public void tearDown() {
        mongoTemplate.getDb().dropDatabase();
    }

    @Test
    public void shouldReturnErrorIfSavingWithNullTenant() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.save(null, application, COLLECTION_NAME, jsonA);
        assertThat(serviceResponse, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
    }

    @Test
    public void shouldReturnErrorIfSavingWithNullApplication() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.save(tenant, null, COLLECTION_NAME, jsonA);
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
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.save(tenant, application, COLLECTION_NAME, null);
        assertThat(serviceResponse, hasErrorMessage(Validations.PRIVATE_STORAGE_INVALID_JSON.getCode()));

    }

    @Test
    public void shouldReturnErrorIfSavingInvalidJson() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.save(tenant, application, COLLECTION_NAME, "a=4");
        assertThat(serviceResponse, hasErrorMessage(Validations.PRIVATE_STORAGE_INVALID_JSON.getCode()));

    }

    @Test
    public void shouldReturnErrorIfSavingInStorageFull() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.save(tenantInm, application, COLLECTION_NAME, jsonA);
        assertThat(serviceResponse, hasErrorMessage(Validations.PRIVATE_STORAGE_IS_FULL.getCode()));

    }

    @Test
    public void shouldReturnErrorIfSavingContentNoIdField() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.save(tenant, application, COLLECTION_NAME, jsonNoId);
        assertThat(serviceResponse, hasErrorMessage(Validations.PRIVATE_STORAGE_NO_COLLECTION_ID_FIELD.getCode()));

    }

    @Test
    public void shouldReturnErrorIfSaveExistingCollectionContent() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.save(tenant, application, COLLECTION_NAME, jsonA);
        assertThat(serviceResponse.isOk(), is(true));

        serviceResponse = privateStorageService.save(tenant, application, COLLECTION_NAME, jsonA);
        assertThat(serviceResponse, hasErrorMessage(Validations.PRIVATE_STORAGE_COLLECTION_CONTENT_ALREADY_EXISTS.getCode()));

    }

    @Test
    public void shouldSave() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.save(tenant, application, COLLECTION_NAME, jsonA);
        assertThat(serviceResponse.isOk(), is(true));
        assertThat(serviceResponse.getResult().getCollectionContent(), is(jsonA));

    }

    @Test
    public void shouldTryUpdateWithNullTenant() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.update(null, application, COLLECTION_NAME, jsonA);
        assertThat(serviceResponse, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
    }

    @Test
    public void shouldTryUpdateWithNullApplication() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.update(tenant, null, COLLECTION_NAME, jsonA);
        assertThat(serviceResponse, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    @Test
    public void shouldTryUpdateWithNullCollectionName() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.update(tenant, application, null, jsonA);
        assertThat(serviceResponse, hasErrorMessage(Validations.PRIVATE_STORAGE_INVALID_COLLECTION_NAME.getCode()));

    }

    @Test
    public void shouldTryUpdateWithInvalidCollectionName() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.update(tenant, application, "collection/A", jsonA);
        assertThat(serviceResponse, hasErrorMessage(Validations.PRIVATE_STORAGE_INVALID_COLLECTION_NAME.getCode()));

    }

    @Test
    public void shouldTryUpdateWithNullJson() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.update(tenant, application, COLLECTION_NAME, null);
        assertThat(serviceResponse, hasErrorMessage(Validations.PRIVATE_STORAGE_INVALID_JSON.getCode()));

    }

    @Test
    public void shouldTryUpdateWithInvalidJson() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.update(tenant, application, COLLECTION_NAME, "a=4");
        assertThat(serviceResponse, hasErrorMessage(Validations.PRIVATE_STORAGE_INVALID_JSON.getCode()));

    }

    @Test
    public void shouldTryUpdateInStorageFull() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.update(tenantInm, application, COLLECTION_NAME, jsonA);
        assertThat(serviceResponse, hasErrorMessage(Validations.PRIVATE_STORAGE_IS_FULL.getCode()));

    }

    @Test
    public void shouldTryUpdateContentNoIdField() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.update(tenant, application, COLLECTION_NAME, jsonNoId);
        assertThat(serviceResponse, hasErrorMessage(Validations.PRIVATE_STORAGE_NO_COLLECTION_ID_FIELD.getCode()));

    }

    @Test
    public void shouldTryUpdateContentNotExists() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.update(tenant, application, COLLECTION_NAME, jsonB);
        assertThat(serviceResponse, hasErrorMessage(Validations.PRIVATE_STORAGE_DOES_NOT_EXIST.getCode()));

    }

    @Test
    public void shouldUpdate() throws Exception {
        privateStorageRepository = PrivateStorageRepository.getInstance(mongo, "konker");
        Map<String, Object> collectionContent = new HashMap<>();
        collectionContent.put("_id", "adba7f77-33cb-4e77-8560-288d273e7add");
        collectionContent.put("barCode", "00000");
        privateStorageRepository.save(COLLECTION_NAME, collectionContent);

        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.update(tenant, application, COLLECTION_NAME, jsonA);
        assertThat(serviceResponse.isOk(), is(true));
        assertThat(serviceResponse.getResult().getCollectionName(), is("konkerCollection"));
        assertThat(serviceResponse.getResult().getCollectionContent(), is(jsonA));

    }

    @Test
    public void shouldTryToRemoveWithNullTenant() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.remove(null, application, COLLECTION_NAME, "adba7f77-33cb-4e77-8560-288d273e7add");
        assertThat(serviceResponse, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));

    }

    @Test
    public void shouldTryToRemoveWithNullApplication() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.remove(tenant, null, COLLECTION_NAME, "adba7f77-33cb-4e77-8560-288d273e7add");
        assertThat(serviceResponse, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));

    }

    @Test
    public void shouldTryToRemoveWithNullCollectionName() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.remove(tenant, application, null, "adba7f77-33cb-4e77-8560-288d273e7add");
        assertThat(serviceResponse, hasErrorMessage(Validations.PRIVATE_STORAGE_INVALID_COLLECTION_NAME.getCode()));

    }

    @Test
    public void shouldTryToRemoveWithInvalidCollectionName() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.remove(tenant, application, "collection/A", "adba7f77-33cb-4e77-8560-288d273e7add");
        assertThat(serviceResponse, hasErrorMessage(Validations.PRIVATE_STORAGE_INVALID_COLLECTION_NAME.getCode()));

    }

    @Test
    public void shouldTryToRemoveWithNullId() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.remove(tenant, application, COLLECTION_NAME, null);
        assertThat(serviceResponse, hasErrorMessage(PrivateStorageService.Validations.PRIVATE_STORAGE_COLLECTION_ID_IS_NULL.getCode()));

    }

    @Test
    public void shouldTryToRemoveWithNonexistentData() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.remove(tenant, application, COLLECTION_NAME, "adba7f77-33cb-4e77-8560-288d273e7add");
        assertThat(serviceResponse, hasErrorMessage(PrivateStorageService.Validations.PRIVATE_STORAGE_DOES_NOT_EXIST.getCode()));

    }

    @Test
    public void shouldRemove() throws Exception {
        privateStorageRepository = PrivateStorageRepository.getInstance(mongo, "konker");
        Map<String, Object> collectionContent = new HashMap<>();
        collectionContent.put("_id", "adba7f77-33cb-4e77-8560-288d273e7add");
        collectionContent.put("barCode", "00000");
        privateStorageRepository.save(COLLECTION_NAME, collectionContent);

        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.remove(tenant, application, COLLECTION_NAME,  "adba7f77-33cb-4e77-8560-288d273e7add");
        assertThat(serviceResponse.isOk(), is(true));
        assertThat(serviceResponse.getResponseMessages(), hasEntry(PrivateStorageService.Messages.PRIVATE_STORAGE_REMOVED_SUCCESSFULLY.getCode(), null));
        assertThat(privateStorageRepository.findById(COLLECTION_NAME, "adba7f77-33cb-4e77-8560-288d273e7add"), nullValue());

    }

    @Test
    public void shouldTryFindAllWithNullTenant() throws Exception {
        ServiceResponse<List<PrivateStorage>> serviceResponse = privateStorageService.findAll(null, application, COLLECTION_NAME);
        assertThat(serviceResponse, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));

    }

    @Test
    public void shouldTryFindAllWithNullApplication() throws Exception {
        ServiceResponse<List<PrivateStorage>> serviceResponse = privateStorageService.findAll(tenant, null, COLLECTION_NAME);
        assertThat(serviceResponse, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));

    }

    @Test
    public void shouldTryFindAllWithNullCollectionName() throws Exception {
        ServiceResponse<List<PrivateStorage>> serviceResponse = privateStorageService.findAll(tenant, application, null);
        assertThat(serviceResponse, hasErrorMessage(Validations.PRIVATE_STORAGE_INVALID_COLLECTION_NAME.getCode()));

    }

    @Test
    public void shouldTryFindAllWithInvalidCollectionName() throws Exception {
        ServiceResponse<List<PrivateStorage>> serviceResponse = privateStorageService.findAll(tenant, application, "collection/A");
        assertThat(serviceResponse, hasErrorMessage(Validations.PRIVATE_STORAGE_INVALID_COLLECTION_NAME.getCode()));

    }

    @Test
    public void shouldTryFindAll() throws Exception {
        privateStorageRepository = PrivateStorageRepository.getInstance(mongo, "konker");
        Map<String, Object> collectionContent = new HashMap<>();
        collectionContent.put("_id", "adba7f77-33cb-4e77-8560-288d273e7add");
        collectionContent.put("barCode", "00000");
        privateStorageRepository.save(COLLECTION_NAME, collectionContent);

        collectionContent.put("_id", "adba7f77-33cb-4e77-8560-288d273e7ass");
        collectionContent.put("a", "2");
        privateStorageRepository.save(COLLECTION_NAME, collectionContent);

        ServiceResponse<List<PrivateStorage>> serviceResponse = privateStorageService.findAll(tenant, application, COLLECTION_NAME);
        assertThat(serviceResponse.isOk(), is(true));
        assertThat(serviceResponse.getResult().size(), is(2));

    }

    @Test
    public void shouldTryFindByIdWithNullTenant() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.findById(null, application, COLLECTION_NAME, "adba7f77-33cb-4e77-8560-288d273e7add");
        assertThat(serviceResponse, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));

    }

    @Test
    public void shouldTryFindByIdWithNullApplication() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.findById(tenant, null, COLLECTION_NAME, "adba7f77-33cb-4e77-8560-288d273e7add");
        assertThat(serviceResponse, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));

    }

    @Test
    public void shouldTryFindByIdWithNullCollectionName() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.findById(tenant, application, null, "adba7f77-33cb-4e77-8560-288d273e7add");
        assertThat(serviceResponse, hasErrorMessage(Validations.PRIVATE_STORAGE_INVALID_COLLECTION_NAME.getCode()));

    }

    @Test
    public void shouldTryFindByIdWithInvalidCollectionName() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.findById(tenant, application, "collection/A", "adba7f77-33cb-4e77-8560-288d273e7add");
        assertThat(serviceResponse, hasErrorMessage(Validations.PRIVATE_STORAGE_INVALID_COLLECTION_NAME.getCode()));

    }

    @Test
    public void shouldTryFindByIdWithNullId() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.findById(tenant, application, COLLECTION_NAME, null);
        assertThat(serviceResponse, hasErrorMessage(PrivateStorageService.Validations.PRIVATE_STORAGE_COLLECTION_ID_IS_NULL.getCode()));

    }

    @Test
    public void shouldTryFindById() throws Exception {
        privateStorageRepository = PrivateStorageRepository.getInstance(mongo, "konker");
        Map<String, Object> collectionContent = new HashMap<>();
        collectionContent.put("_id", "adba7f77-33cb-4e77-8560-288d273e7add");
        collectionContent.put("barCode", "00000");
        privateStorageRepository.save(COLLECTION_NAME, collectionContent);

        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.findById(tenant, application, COLLECTION_NAME, "adba7f77-33cb-4e77-8560-288d273e7add");
        assertThat(serviceResponse.isOk(), is(true));
        assertThat(serviceResponse.getResult().getCollectionName(), is(COLLECTION_NAME));
        assertThat(serviceResponse.getResult().getCollectionContent(), is(jsonC));

    }

}