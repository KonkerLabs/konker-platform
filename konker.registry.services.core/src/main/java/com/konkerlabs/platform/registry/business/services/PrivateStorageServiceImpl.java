package com.konkerlabs.platform.registry.business.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.PrivateStorageService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.storage.model.PrivateStorage;
import com.konkerlabs.platform.registry.storage.repositories.PrivateStorageRepository;
import com.konkerlabs.platform.utilities.parsers.json.JsonParsingService;
import com.mongodb.Mongo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class PrivateStorageServiceImpl implements PrivateStorageService {

    private static Logger LOG = LoggerFactory.getLogger(PrivateStorageServiceImpl.class);

    @Autowired
    private JsonParsingService jsonParsingService;

    @Autowired
    @Qualifier("mongoPrivateStorage")
    private Mongo mongo;

    private PrivateStorageRepository privateStorageRepository;

    private static final String COLLECTION_KEY_PATTERN = "[a-zA-Z0-9\\-_]{2,100}";

    public PrivateStorageServiceImpl(PrivateStorageRepository privateStorageRepository,
                                     JsonParsingService jsonParsingService) {

    }

    @Override
    public ServiceResponse<PrivateStorage> save(Tenant tenant,
                                                Application application,
                                                User user,
                                                String collectionName,
                                                String collectionContent) throws JsonProcessingException {
        ServiceResponse<PrivateStorage> validationResponse = validate(tenant, application, collectionName);

        if (!validationResponse.isOk()) {
            return validationResponse;
        }

        if (Optional.ofNullable(user.getApplication()).isPresent()
                && !application.equals(user.getApplication())) {
            return ServiceResponseBuilder.<PrivateStorage>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_HAS_NO_PERMISSION.getCode())
                    .build();
        }

        LOG.info("[Save] Creating Private Storage Repository instance...");
        privateStorageRepository = PrivateStorageRepository.getInstance(mongo, tenant, application);

        if (!jsonParsingService.isValid(collectionContent)) {
            return ServiceResponseBuilder.<PrivateStorage>error()
                    .withMessage(Validations.PRIVATE_STORAGE_INVALID_JSON.getCode())
                    .build();
        }

        if (isPrivateStorageFull(tenant)) {
            return ServiceResponseBuilder.<PrivateStorage>error()
                    .withMessage(Validations.PRIVATE_STORAGE_IS_FULL.getCode())
                    .build();
        }

        Map<String, Object> content = jsonParsingService.toMap(collectionContent);
        if (!content.containsKey("_id")) {
            return ServiceResponseBuilder.<PrivateStorage>error()
                    .withMessage(Validations.PRIVATE_STORAGE_NO_COLLECTION_ID_FIELD.getCode())
                    .build();
        }

        PrivateStorage fromDB = privateStorageRepository.findById(collectionName, content.get("_id").toString());
        if (Optional.ofNullable(fromDB).isPresent()) {
            return ServiceResponseBuilder.<PrivateStorage>error()
                    .withMessage(Validations.PRIVATE_STORAGE_COLLECTION_CONTENT_ALREADY_EXISTS.getCode())
                    .build();
        }

        privateStorageRepository.save(collectionName, content);
        return ServiceResponseBuilder.<PrivateStorage>ok()
                .withResult(PrivateStorage.builder()
                        .collectionName(collectionName)
                        .collectionContent(jsonParsingService.toJsonString(content))
                        .build())
                .build();
    }

    private boolean isPrivateStorageFull(Tenant tenant) {
        Long privateStorageSizeByte = Optional.ofNullable(tenant.getPrivateStorageSize()).orElse(0l);
        Long limitSize = 1073741824l;
        return privateStorageSizeByte.compareTo(limitSize) > 0;
    }

    @Override
    public ServiceResponse<PrivateStorage> update(Tenant tenant,
                                                  Application application,
                                                  User user,
                                                  String collectionName,
                                                  String collectionContent) throws JsonProcessingException {
        ServiceResponse<PrivateStorage> validationResponse = validate(tenant, application, collectionName);

        if (!validationResponse.isOk()) {
            return validationResponse;
        }

        if (Optional.ofNullable(user.getApplication()).isPresent()
                && !application.equals(user.getApplication())) {
            return ServiceResponseBuilder.<PrivateStorage>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_HAS_NO_PERMISSION.getCode())
                    .build();
        }

        LOG.info("[Update] Creating Private Storage Repository instance...");
        privateStorageRepository = PrivateStorageRepository.getInstance(mongo, tenant, application);

        if (!jsonParsingService.isValid(collectionContent)) {
            return ServiceResponseBuilder.<PrivateStorage>error()
                    .withMessage(Validations.PRIVATE_STORAGE_INVALID_JSON.getCode())
                    .build();
        }

        if (isPrivateStorageFull(tenant)) {
            return ServiceResponseBuilder.<PrivateStorage>error()
                    .withMessage(Validations.PRIVATE_STORAGE_IS_FULL.getCode())
                    .build();
        }

        Map<String, Object> content = jsonParsingService.toMap(collectionContent);
        if (!content.containsKey("_id")) {
            return ServiceResponseBuilder.<PrivateStorage>error()
                    .withMessage(Validations.PRIVATE_STORAGE_NO_COLLECTION_ID_FIELD.getCode())
                    .build();
        }

        PrivateStorage fromDB = privateStorageRepository.findById(collectionName, content.get("_id").toString());
        if (!Optional.ofNullable(fromDB).isPresent()) {
            return ServiceResponseBuilder.<PrivateStorage>error()
                    .withMessage(Validations.PRIVATE_STORAGE_COLLECTION_CONTENT_DOES_NOT_EXIST.getCode())
                    .build();
        }

        PrivateStorage privateStorage = privateStorageRepository.update(collectionName, content);
        return ServiceResponseBuilder.<PrivateStorage>ok()
                .withResult(privateStorage)
                .build();
    }

    @Override
    public ServiceResponse<PrivateStorage> remove(Tenant tenant,
                                                  Application application,
                                                  User user,
                                                  String collectionName,
                                                  String id) throws JsonProcessingException {
        ServiceResponse<PrivateStorage> validationResponse = validate(tenant, application, collectionName);

        if (!validationResponse.isOk()) {
            return validationResponse;
        }

        if (Optional.ofNullable(user.getApplication()).isPresent()
                && !application.equals(user.getApplication())) {
            return ServiceResponseBuilder.<PrivateStorage>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_HAS_NO_PERMISSION.getCode())
                    .build();
        }

        LOG.info("[Remove] Creating Private Storage Repository instance...");
        privateStorageRepository = PrivateStorageRepository.getInstance(mongo, tenant, application);

        if (!Optional.ofNullable(id).isPresent()) {
            return ServiceResponseBuilder.<PrivateStorage>error()
                    .withMessage(Validations.PRIVATE_STORAGE_COLLECTION_ID_IS_NULL.getCode())
                    .build();
        }

        PrivateStorage fromDB = privateStorageRepository.findById(collectionName, id);
        if (!Optional.ofNullable(fromDB).isPresent()) {
            return ServiceResponseBuilder.<PrivateStorage>error()
                    .withMessage(Validations.PRIVATE_STORAGE_COLLECTION_CONTENT_DOES_NOT_EXIST.getCode())
                    .build();
        }

        privateStorageRepository.remove(collectionName, id);
        return ServiceResponseBuilder.<PrivateStorage>ok()
                .withMessage(Messages.PRIVATE_STORAGE_REMOVED_SUCCESSFULLY.getCode())
                .build();
    }

    @Override
    public ServiceResponse<List<PrivateStorage>> findAll(Tenant tenant,
                                                        Application application,
                                                        User user,
                                                        String collectionName) throws JsonProcessingException {
        ServiceResponse<List<PrivateStorage>> validationResponse = validate(tenant, application, collectionName);

        if (!validationResponse.isOk()) {
            return validationResponse;
        }

        if (Optional.ofNullable(user.getApplication()).isPresent()
                && !application.equals(user.getApplication())) {
            return ServiceResponseBuilder.<List<PrivateStorage>>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_HAS_NO_PERMISSION.getCode())
                    .build();
        }

        LOG.info("[FindALl] Creating Private Storage Repository instance...");
        privateStorageRepository = PrivateStorageRepository.getInstance(mongo, tenant, application);

        return ServiceResponseBuilder.<List<PrivateStorage>>ok()
                .withResult(privateStorageRepository.findAll(collectionName))
                .build();
    }

    @Override
    public ServiceResponse<PrivateStorage> findById(Tenant tenant,
                                                    Application application,
                                                    User user,
                                                    String collectionName,
                                                    String id) throws JsonProcessingException {
        ServiceResponse<PrivateStorage> validationResponse = validate(tenant, application, collectionName);

        if (!validationResponse.isOk()) {
            return validationResponse;
        }

        if (Optional.ofNullable(user.getApplication()).isPresent()
                && !application.equals(user.getApplication())) {
            return ServiceResponseBuilder.<PrivateStorage>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_HAS_NO_PERMISSION.getCode())
                    .build();
        }

        LOG.info("[FindById] Creating Private Storage Repository instance...");
        privateStorageRepository = PrivateStorageRepository.getInstance(mongo, tenant, application);

        if (!Optional.ofNullable(id).isPresent()) {
            return ServiceResponseBuilder.<PrivateStorage>error()
                    .withMessage(Validations.PRIVATE_STORAGE_COLLECTION_ID_IS_NULL.getCode())
                    .build();
        }

        return ServiceResponseBuilder.<PrivateStorage>ok()
                .withResult(privateStorageRepository.findById(collectionName, id))
                .build();
    }

    @Override
    public ServiceResponse<Set<String>> listCollections(Tenant tenant, Application application, User user) {
        if (!Optional.ofNullable(tenant).isPresent()) {
            return ServiceResponseBuilder.<Set<String>>error().withMessage(CommonValidations.TENANT_NULL.getCode()).build();
        }

        if (!Optional.ofNullable(application).isPresent()) {
            return ServiceResponseBuilder.<Set<String>>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()).build();
        }

        if (Optional.ofNullable(user.getApplication()).isPresent()
                && !application.equals(user.getApplication())) {
            return ServiceResponseBuilder.<Set<String>>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_HAS_NO_PERMISSION.getCode())
                    .build();
        }

        LOG.info("[FindCollections] Creating Private Storage Repository instance...");
        privateStorageRepository = PrivateStorageRepository.getInstance(mongo, tenant, application);

        return ServiceResponseBuilder.<Set<String>>ok()
                .withResult(privateStorageRepository.listCollections())
                .build();
    }

    private <T> ServiceResponse<T> validate(Tenant tenant, Application application, String collectionName) {
        if (!Optional.ofNullable(tenant).isPresent()) {
            return ServiceResponseBuilder.<T>error().withMessage(CommonValidations.TENANT_NULL.getCode()).build();
        }

        if (!Optional.ofNullable(application).isPresent()) {
            return ServiceResponseBuilder.<T>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()).build();
        }

        if (!Optional.ofNullable(collectionName).isPresent()
                || !collectionName.matches(COLLECTION_KEY_PATTERN)) {
            return ServiceResponseBuilder.<T>error()
                    .withMessage(Validations.PRIVATE_STORAGE_INVALID_COLLECTION_NAME.getCode())
                    .build();
        }

        return ServiceResponseBuilder.<T>ok()
                .build();

    }

}
