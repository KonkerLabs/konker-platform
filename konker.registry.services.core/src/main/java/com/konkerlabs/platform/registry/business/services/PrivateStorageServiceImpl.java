package com.konkerlabs.platform.registry.business.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.konkerlabs.platform.registry.billing.model.TenantDailyUsage;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.services.api.*;
import com.konkerlabs.platform.registry.storage.model.PrivateStorage;
import com.konkerlabs.platform.registry.storage.repositories.PrivateStorageRepository;
import com.konkerlabs.platform.utilities.parsers.json.JsonParsingService;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.WriteResult;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class PrivateStorageServiceImpl implements PrivateStorageService {

    @Autowired
    private JsonParsingService jsonParsingService;

    @Autowired
    private Mongo mongo;

    private PrivateStorageRepository privateStorageRepository;

    private static final String COLLECTION_KEY_PATTERN = "[a-zA-Z0-9\\-_]{2,100}";

    public PrivateStorageServiceImpl(PrivateStorageRepository privateStorageRepository,
                                     JsonParsingService jsonParsingService) {

    }

    @Override
    public ServiceResponse<PrivateStorage> save(Tenant tenant,
                                                Application application,
                                                String collectionName,
                                                String collectionContent) throws JsonProcessingException {
        ServiceResponse<PrivateStorage> validationResponse = validate(tenant, application, collectionName);

        if (!validationResponse.isOk()) {
            return validationResponse;
        }
        privateStorageRepository = PrivateStorageRepository.getInstance(mongo, tenant.getDomainName());

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

        DBObject dbObject = privateStorageRepository.findByKey(collectionName, content.get("_id").toString());
        if (Optional.ofNullable(dbObject).isPresent()) {
            return ServiceResponseBuilder.<PrivateStorage>error()
                    .withMessage(Validations.PRIVATE_STORAGE_COLLECTION_CONTENT_ALREADY_EXISTS.getCode())
                    .build();
        }

        privateStorageRepository.save(collectionName, content);
        return ServiceResponseBuilder.<PrivateStorage>ok()
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
                                                  String collectionName,
                                                  String collectionContent) {
        return null;
    }

    @Override
    public ServiceResponse<PrivateStorage> remove(Tenant tenant,
                                                  Application application,
                                                  String collectionName,
                                                  String key) {
        return null;
    }

    @Override
    public ServiceResponse<PrivateStorage> findAll(Tenant tenant,
                                                   Application application,
                                                   String collectionName) {
        return null;
    }

    @Override
    public ServiceResponse<PrivateStorage> findByTenantApplicationKey(Tenant tenant,
                                                                      Application application,
                                                                      String collectionName,
                                                                      String key) {
        return null;
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
