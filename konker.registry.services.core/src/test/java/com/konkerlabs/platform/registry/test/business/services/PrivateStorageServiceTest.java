package com.konkerlabs.platform.registry.test.business.services;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.ApplicationRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.PrivateStorageService;
import com.konkerlabs.platform.registry.business.services.api.PrivateStorageService.Validations;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.UserService;
import com.konkerlabs.platform.registry.config.EmailConfig;
import com.konkerlabs.platform.registry.config.MongoPrivateStorageConfig;
import com.konkerlabs.platform.registry.storage.model.PrivateStorage;
import com.konkerlabs.platform.registry.storage.repositories.PrivateStorageRepository;
import com.konkerlabs.platform.registry.test.base.*;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import com.mongodb.Mongo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.thymeleaf.spring4.SpringTemplateEngine;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.hasErrorMessage;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        MongoTestConfiguration.class,
        MongoPrivateStorageTestConfiguration.class,
        MongoBillingTestConfiguration.class,
        BusinessTestConfiguration.class,
        EmailConfig.class,
        PrivateStorageServiceTest.PrivateStorageServiceTestConfig.class
})
@UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/users.json"})
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
    @Qualifier("mongoPrivateStorage")
    private Mongo mongo;

    @Autowired
    private UserService userService;

    private PrivateStorageRepository privateStorageRepository;

    private Tenant tenant;
    private Tenant tenantInm;
    private Application application;
    private User userAdmin;
    private User userApplication;

    private String jsonA = "{\"_id\":\"adba7f77-33cb-4e77-8560-288d273e7add\",\"barCode\":\"00001\",\"desc\":\"sensor desc\"}";
    private String jsonB = "{\"_id\": \"adba7f77-33cb-4e77-8560-288d273e7ass\", \"a\": 2}";
    private String jsonC = "{\"_id\":\"adba7f77-33cb-4e77-8560-288d273e7add\",\"barCode\":\"00000\"}";
    private String jsonNoId = "{\"key1\": \"adba7f77-33cb-4e77-8560-288d273e7aee\", \"a\": 2}";

    @Before
    public void setUp() throws Exception {
    	tenant = tenantRepository.findByDomainName("konker");
    	tenantInm = tenantRepository.findByDomainName("inm");
    	application = applicationRepository.findByTenantAndName(tenant.getId(), "smartffkonker");
        privateStorageRepository = PrivateStorageRepository.getInstance(mongo, tenant, application);

        MongoPrivateStorageConfig config = new MongoPrivateStorageConfig();
        config.setDbName(MessageFormat.format("{0}_{1}", tenant.getDomainName(), application.getName()));
        mongoTemplate = config.mongoTemplate(mongo);

        userAdmin = userService.findByEmail("admin@konkerlabs.com").getResult();
        userApplication = userService.findByEmail("user.application@konkerlabs.com").getResult();
    }

    @After
    public void tearDown() {
        mongoTemplate.getDb().dropDatabase();
    }

    @Test
    public void shouldReturnErrorIfSavingWithNullTenant() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.save(null, application, userAdmin, COLLECTION_NAME, jsonA);
        assertThat(serviceResponse, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
    }

    @Test
    public void shouldReturnErrorIfSavingWithNullApplication() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.save(tenant, null, userAdmin, COLLECTION_NAME, jsonA);
        assertThat(serviceResponse, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));

    }

    @Test
    public void shouldReturnErrorIfSavingWithNullCollectionName() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.save(tenant, application, userAdmin, null, jsonA);
        assertThat(serviceResponse, hasErrorMessage(Validations.PRIVATE_STORAGE_INVALID_COLLECTION_NAME.getCode()));

    }

    @Test
    public void shouldReturnErrorIfSavingWithInvalidCollectionName() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.save(tenant, application, userAdmin, "collection/A", jsonA);
        assertThat(serviceResponse, hasErrorMessage(Validations.PRIVATE_STORAGE_INVALID_COLLECTION_NAME.getCode()));

    }

    @Test
    public void shouldReturnErrorIfSavingWithInvalidApplication() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.save(tenant, application, userApplication, COLLECTION_NAME, jsonA);
        assertThat(serviceResponse, hasErrorMessage(ApplicationService.Validations.APPLICATION_HAS_NO_PERMISSION.getCode()));

    }

    @Test
    public void shouldReturnErrorIfSavingNullJson() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.save(tenant, application, userAdmin, COLLECTION_NAME, null);
        assertThat(serviceResponse, hasErrorMessage(Validations.PRIVATE_STORAGE_INVALID_JSON.getCode()));

    }

    @Test
    public void shouldReturnErrorIfSavingInvalidJson() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.save(tenant, application, userAdmin, COLLECTION_NAME, "a=4");
        assertThat(serviceResponse, hasErrorMessage(Validations.PRIVATE_STORAGE_INVALID_JSON.getCode()));

    }

    @Test
    public void shouldReturnErrorIfSavingInStorageFull() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.save(tenantInm, application, userAdmin, COLLECTION_NAME, jsonA);
        assertThat(serviceResponse, hasErrorMessage(Validations.PRIVATE_STORAGE_IS_FULL.getCode()));

    }

    @Test
    public void shouldReturnErrorIfSavingContentNoIdField() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.save(tenant, application, userAdmin, COLLECTION_NAME, jsonNoId);
        assertThat(serviceResponse, hasErrorMessage(Validations.PRIVATE_STORAGE_NO_COLLECTION_ID_FIELD.getCode()));

    }

    @Test
    public void shouldReturnErrorIfSaveExistingCollectionContent() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.save(tenant, application, userAdmin, COLLECTION_NAME, jsonA);
        assertThat(serviceResponse.isOk(), is(true));

        serviceResponse = privateStorageService.save(tenant, application, userAdmin, COLLECTION_NAME, jsonA);
        assertThat(serviceResponse, hasErrorMessage(Validations.PRIVATE_STORAGE_COLLECTION_CONTENT_ALREADY_EXISTS.getCode()));

    }

    @Test
    public void shouldSave() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.save(tenant, application, userAdmin, COLLECTION_NAME, jsonA);
        assertThat(serviceResponse.isOk(), is(true));
        assertThat(serviceResponse.getResult().getCollectionContent(), is(jsonA));

    }

    @Test
    public void shouldTryUpdateWithNullTenant() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.update(null, application, userAdmin, COLLECTION_NAME, jsonA);
        assertThat(serviceResponse, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
    }

    @Test
    public void shouldTryUpdateWithNullApplication() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.update(tenant, null, userAdmin, COLLECTION_NAME, jsonA);
        assertThat(serviceResponse, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    @Test
    public void shouldTryUpdateWithNullCollectionName() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.update(tenant, application, userAdmin, null, jsonA);
        assertThat(serviceResponse, hasErrorMessage(Validations.PRIVATE_STORAGE_INVALID_COLLECTION_NAME.getCode()));

    }

    @Test
    public void shouldTryUpdateWithInvalidCollectionName() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.update(tenant, application, userAdmin, "collection/A", jsonA);
        assertThat(serviceResponse, hasErrorMessage(Validations.PRIVATE_STORAGE_INVALID_COLLECTION_NAME.getCode()));

    }

    @Test
    public void shouldTryUpdateWithInvalidApplication() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.update(tenant, application, userApplication, COLLECTION_NAME, jsonA);
        assertThat(serviceResponse, hasErrorMessage(ApplicationService.Validations.APPLICATION_HAS_NO_PERMISSION.getCode()));

    }

    @Test
    public void shouldTryUpdateWithNullJson() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.update(tenant, application, userAdmin, COLLECTION_NAME, null);
        assertThat(serviceResponse, hasErrorMessage(Validations.PRIVATE_STORAGE_INVALID_JSON.getCode()));

    }

    @Test
    public void shouldTryUpdateWithInvalidJson() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.update(tenant, application, userAdmin, COLLECTION_NAME, "a=4");
        assertThat(serviceResponse, hasErrorMessage(Validations.PRIVATE_STORAGE_INVALID_JSON.getCode()));

    }

    @Test
    public void shouldTryUpdateInStorageFull() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.update(tenantInm, application, userAdmin, COLLECTION_NAME, jsonA);
        assertThat(serviceResponse, hasErrorMessage(Validations.PRIVATE_STORAGE_IS_FULL.getCode()));

    }

    @Test
    public void shouldTryUpdateContentNoIdField() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.update(tenant, application, userAdmin, COLLECTION_NAME, jsonNoId);
        assertThat(serviceResponse, hasErrorMessage(Validations.PRIVATE_STORAGE_NO_COLLECTION_ID_FIELD.getCode()));

    }

    @Test
    public void shouldTryUpdateContentNotExists() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.update(tenant, application, userAdmin, COLLECTION_NAME, jsonB);
        assertThat(serviceResponse, hasErrorMessage(Validations.PRIVATE_STORAGE_COLLECTION_CONTENT_DOES_NOT_EXIST.getCode()));

    }

    @Test
    public void shouldUpdate() throws Exception {
        Map<String, Object> collectionContent = new HashMap<>();
        collectionContent.put("_id", "adba7f77-33cb-4e77-8560-288d273e7add");
        collectionContent.put("barCode", "00000");
        privateStorageRepository.save(COLLECTION_NAME, collectionContent);

        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.update(tenant, application, userAdmin, COLLECTION_NAME, jsonA);
        assertThat(serviceResponse.isOk(), is(true));
        assertThat(serviceResponse.getResult().getCollectionName(), is("konkerCollection"));
        assertThat(serviceResponse.getResult().getCollectionContent(), is(jsonA));

    }

    @Test
    public void shouldTryToRemoveWithNullTenant() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.remove(null, application, userAdmin, COLLECTION_NAME, "adba7f77-33cb-4e77-8560-288d273e7add");
        assertThat(serviceResponse, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));

    }

    @Test
    public void shouldTryToRemoveWithNullApplication() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.remove(tenant, null, userAdmin, COLLECTION_NAME, "adba7f77-33cb-4e77-8560-288d273e7add");
        assertThat(serviceResponse, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));

    }

    @Test
    public void shouldTryToRemoveWithNullCollectionName() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.remove(tenant, application, userAdmin, null, "adba7f77-33cb-4e77-8560-288d273e7add");
        assertThat(serviceResponse, hasErrorMessage(Validations.PRIVATE_STORAGE_INVALID_COLLECTION_NAME.getCode()));

    }

    @Test
    public void shouldTryRemoveWithInvalidApplication() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.remove(tenant, application, userApplication, COLLECTION_NAME, "adba7f77-33cb-4e77-8560-288d273e7add");
        assertThat(serviceResponse, hasErrorMessage(ApplicationService.Validations.APPLICATION_HAS_NO_PERMISSION.getCode()));

    }

    @Test
    public void shouldTryToRemoveWithInvalidCollectionName() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.remove(tenant, application, userAdmin, "collection/A", "adba7f77-33cb-4e77-8560-288d273e7add");
        assertThat(serviceResponse, hasErrorMessage(Validations.PRIVATE_STORAGE_INVALID_COLLECTION_NAME.getCode()));

    }

    @Test
    public void shouldTryToRemoveWithNullId() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.remove(tenant, application, userAdmin, COLLECTION_NAME, null);
        assertThat(serviceResponse, hasErrorMessage(PrivateStorageService.Validations.PRIVATE_STORAGE_COLLECTION_ID_IS_NULL.getCode()));

    }

    @Test
    public void shouldTryToRemoveWithNonexistentData() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.remove(tenant, application, userAdmin, COLLECTION_NAME, "adba7f77-33cb-4e77-8560-288d273e7add");
        assertThat(serviceResponse, hasErrorMessage(PrivateStorageService.Validations.PRIVATE_STORAGE_COLLECTION_CONTENT_DOES_NOT_EXIST.getCode()));

    }

    @Test
    public void shouldRemove() throws Exception {
        Map<String, Object> collectionContent = new HashMap<>();
        collectionContent.put("_id", "adba7f77-33cb-4e77-8560-288d273e7add");
        collectionContent.put("barCode", "00000");
        privateStorageRepository.save(COLLECTION_NAME, collectionContent);

        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.remove(tenant, application, userAdmin, COLLECTION_NAME,  "adba7f77-33cb-4e77-8560-288d273e7add");
        assertThat(serviceResponse.isOk(), is(true));
        assertThat(serviceResponse.getResponseMessages(), hasEntry(PrivateStorageService.Messages.PRIVATE_STORAGE_REMOVED_SUCCESSFULLY.getCode(), null));
        assertThat(privateStorageRepository.findById(COLLECTION_NAME, "adba7f77-33cb-4e77-8560-288d273e7add"), nullValue());

    }

    @Test
    public void shouldTryFindAllWithNullTenant() throws Exception {
        ServiceResponse<List<PrivateStorage>> serviceResponse = privateStorageService.findAll(null, application, userAdmin, COLLECTION_NAME);
        assertThat(serviceResponse, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));

    }

    @Test
    public void shouldTryFindAllWithNullApplication() throws Exception {
        ServiceResponse<List<PrivateStorage>> serviceResponse = privateStorageService.findAll(tenant, null, userAdmin, COLLECTION_NAME);
        assertThat(serviceResponse, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));

    }

    @Test
    public void shouldTryFindAllWithNullCollectionName() throws Exception {
        ServiceResponse<List<PrivateStorage>> serviceResponse = privateStorageService.findAll(tenant, application, userAdmin, null);
        assertThat(serviceResponse, hasErrorMessage(Validations.PRIVATE_STORAGE_INVALID_COLLECTION_NAME.getCode()));

    }

    @Test
    public void shouldTryFindAllWithInvalidCollectionName() throws Exception {
        ServiceResponse<List<PrivateStorage>> serviceResponse = privateStorageService.findAll(tenant, application, userAdmin, "collection/A");
        assertThat(serviceResponse, hasErrorMessage(Validations.PRIVATE_STORAGE_INVALID_COLLECTION_NAME.getCode()));

    }

    @Test
    public void shouldTryFindAllWithInvalidApplication() throws Exception {
        ServiceResponse<List<PrivateStorage>> serviceResponse = privateStorageService.findAll(tenant, application, userApplication, COLLECTION_NAME);
        assertThat(serviceResponse, hasErrorMessage(ApplicationService.Validations.APPLICATION_HAS_NO_PERMISSION.getCode()));

    }

    @Test
    public void shouldTryFindAll() throws Exception {
        Map<String, Object> collectionContent = new HashMap<>();
        collectionContent.put("_id", "adba7f77-33cb-4e77-8560-288d273e7add");
        collectionContent.put("barCode", "00000");
        privateStorageRepository.save(COLLECTION_NAME, collectionContent);

        collectionContent.put("_id", "adba7f77-33cb-4e77-8560-288d273e7ass");
        collectionContent.put("a", "2");
        privateStorageRepository.save(COLLECTION_NAME, collectionContent);

        ServiceResponse<List<PrivateStorage>> serviceResponse = privateStorageService.findAll(tenant, application, userAdmin, COLLECTION_NAME);
        assertThat(serviceResponse.isOk(), is(true));
        assertThat(serviceResponse.getResult().size(), is(2));

    }

    @Test
    public void shouldTryFindByIdWithNullTenant() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.findById(null, application, userAdmin, COLLECTION_NAME, "adba7f77-33cb-4e77-8560-288d273e7add");
        assertThat(serviceResponse, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));

    }

    @Test
    public void shouldTryFindByIdWithNullApplication() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.findById(tenant, null, userAdmin, COLLECTION_NAME, "adba7f77-33cb-4e77-8560-288d273e7add");
        assertThat(serviceResponse, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));

    }

    @Test
    public void shouldTryFindByIdWithNullCollectionName() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.findById(tenant, application, userAdmin, null, "adba7f77-33cb-4e77-8560-288d273e7add");
        assertThat(serviceResponse, hasErrorMessage(Validations.PRIVATE_STORAGE_INVALID_COLLECTION_NAME.getCode()));

    }

    @Test
    public void shouldTryFindByIdWithInvalidCollectionName() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.findById(tenant, application, userAdmin, "collection/A", "adba7f77-33cb-4e77-8560-288d273e7add");
        assertThat(serviceResponse, hasErrorMessage(Validations.PRIVATE_STORAGE_INVALID_COLLECTION_NAME.getCode()));

    }

    @Test
    public void shouldTryFindByIdWithInvalidApplication() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.findById(tenant, application, userApplication, COLLECTION_NAME, "adba7f77-33cb-4e77-8560-288d273e7add");
        assertThat(serviceResponse, hasErrorMessage(ApplicationService.Validations.APPLICATION_HAS_NO_PERMISSION.getCode()));

    }

    @Test
    public void shouldTryFindByIdWithNullId() throws Exception {
        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.findById(tenant, application, userAdmin, COLLECTION_NAME, null);
        assertThat(serviceResponse, hasErrorMessage(PrivateStorageService.Validations.PRIVATE_STORAGE_COLLECTION_ID_IS_NULL.getCode()));

    }

    @Test
    public void shouldFindById() throws Exception {
        Map<String, Object> collectionContent = new HashMap<>();
        collectionContent.put("_id", "adba7f77-33cb-4e77-8560-288d273e7add");
        collectionContent.put("barCode", "00000");
        privateStorageRepository.save(COLLECTION_NAME, collectionContent);

        ServiceResponse<PrivateStorage> serviceResponse = privateStorageService.findById(tenant, application, userAdmin, COLLECTION_NAME, "adba7f77-33cb-4e77-8560-288d273e7add");
        assertThat(serviceResponse.isOk(), is(true));
        assertThat(serviceResponse.getResult().getCollectionName(), is(COLLECTION_NAME));
        assertThat(serviceResponse.getResult().getCollectionContent(), is(jsonC));

    }

    @Test
    public void shouldTryListCollectionsWithNullTenant() throws Exception {
        ServiceResponse<Set<String>> serviceResponse = privateStorageService.listCollections(null, application, userAdmin);
        assertThat(serviceResponse, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));

    }

    @Test
    public void shouldTryListCollectionsWithNullApplication() throws Exception {
        ServiceResponse<Set<String>> serviceResponse = privateStorageService.listCollections(tenant, null, userAdmin);
        assertThat(serviceResponse, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));

    }

    @Test
    public void shouldListCollection() throws Exception {
        Map<String, Object> collectionContent = new HashMap<>();
        collectionContent.put("_id", "adba7f77-33cb-4e77-8560-288d273e7add");
        collectionContent.put("barCode", "00001");
        privateStorageRepository.save(COLLECTION_NAME, collectionContent);

        ServiceResponse<Set<String>> serviceResponse = privateStorageService.listCollections(tenant, application, userAdmin);
        assertThat(serviceResponse.isOk(), is(true));
        assertThat(serviceResponse.getResult().size(), is(1));
        assertThat(serviceResponse.getResult().iterator().next(), is(COLLECTION_NAME));

    }

    @Configuration
    static class PrivateStorageServiceTestConfig {
        @Bean
        public JavaMailSender javaMailSender() {
            return Mockito.mock(JavaMailSender.class);
        }

        @Bean
        public SpringTemplateEngine springTemplateEngine() {
            return Mockito.mock(SpringTemplateEngine.class);
        }
    }
}