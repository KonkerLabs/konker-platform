package com.konkerlabs.platform.registry.business.services;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.ApplicationDocumentStore;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.ApplicationDocumentStoreRepository;
import com.konkerlabs.platform.registry.business.services.api.ApplicationDocumentStoreService;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.mongodb.util.JSON;
import com.mongodb.util.JSONParseException;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ApplicationDocumentStoreServiceImpl implements ApplicationDocumentStoreService {

    @Autowired
    private ApplicationDocumentStoreRepository applicationDocumentStoreRepository;

    private static final String COLLECTION_KEY_PATTERN = "[a-zA-Z0-9\\-_]{2,100}";

	@Override
	public ServiceResponse<ApplicationDocumentStore> save(Tenant tenant, Application application, String collection, String key, String jsonCustomData) {

        ServiceResponse<ApplicationDocumentStore> validationsResponse = validate(tenant, application, collection, key);
        if (validationsResponse != null && !validationsResponse.isOk()) {
            return validationsResponse;
        }

        if (!isValidJson(jsonCustomData)){
            return ServiceResponseBuilder.<ApplicationDocumentStore>error()
                    .withMessage(Validations.APP_DOCUMENT_INVALID_JSON.getCode())
                    .build();
        }

        ApplicationDocumentStore keyValueFromDB = applicationDocumentStoreRepository.findUniqueByTenantIdApplicationName(
                tenant.getId(),
                application.getName(),
                collection,
                key);

		if (keyValueFromDB != null) {
            return ServiceResponseBuilder.<ApplicationDocumentStore>error()
                    .withMessage(Validations.APP_DOCUMENT_ALREADY_REGISTERED.getCode())
                    .build();
		}

		ApplicationDocumentStore applicationDocumentStore = ApplicationDocumentStore.builder()
		                                                    .tenant(tenant)
		                                                    .application(application)
		                                                    .collection(collection)
		                                                    .key(key)
		                                                    .json(jsonCustomData)
		                                                    .lastChange(Instant.now())
		                                                    .build();

		ApplicationDocumentStore save = applicationDocumentStoreRepository.save(applicationDocumentStore);

		return ServiceResponseBuilder.<ApplicationDocumentStore>ok().withResult(save).build();
	}

    @Override
    public ServiceResponse<ApplicationDocumentStore> update(Tenant tenant, Application application, String collection, String key, String jsonCustomData) {

        ServiceResponse<ApplicationDocumentStore> validationsResponse = validate(tenant, application, collection, key);
        if (validationsResponse != null && !validationsResponse.isOk()) {
            return validationsResponse;
        }

        if (!isValidJson(jsonCustomData)){
            return ServiceResponseBuilder.<ApplicationDocumentStore>error()
                    .withMessage(Validations.APP_DOCUMENT_INVALID_JSON.getCode())
                    .build();
        }

        ApplicationDocumentStore keyValueFromDB = applicationDocumentStoreRepository.findUniqueByTenantIdApplicationName(
                tenant.getId(),
                application.getName(),
                collection,
                key);

		if (!Optional.ofNullable(keyValueFromDB).isPresent()) {
			return ServiceResponseBuilder.<ApplicationDocumentStore>error()
                    .withMessage(Validations.APP_DOCUMENT_DOES_NOT_EXIST.getCode())
                    .build();
		}

		keyValueFromDB.setJson(jsonCustomData);
		keyValueFromDB.setLastChange(Instant.now());

		Optional<Map<String, Object[]>> validations = keyValueFromDB.applyValidations();
		if (validations.isPresent()) {
			return ServiceResponseBuilder.<ApplicationDocumentStore>error()
					.withMessages(validations.get())
					.build();
		}

		ApplicationDocumentStore updated = applicationDocumentStoreRepository.save(keyValueFromDB);

		return ServiceResponseBuilder.<ApplicationDocumentStore>ok().withResult(updated).build();
	}

    @Override
    public ServiceResponse<ApplicationDocumentStore> remove(Tenant tenant, Application application, String collection, String key) {

        ServiceResponse<ApplicationDocumentStore> validationsResponse = validate(tenant, application, collection, key);
        if (validationsResponse != null && !validationsResponse.isOk()) {
            return validationsResponse;
        }

        ApplicationDocumentStore keyValueFromDB = applicationDocumentStoreRepository.findUniqueByTenantIdApplicationName(
                tenant.getId(),
                application.getName(),
                collection,
                key);

		if (!Optional.ofNullable(keyValueFromDB).isPresent()) {
			return ServiceResponseBuilder.<ApplicationDocumentStore>error()
                    .withMessage(Validations.APP_DOCUMENT_DOES_NOT_EXIST.getCode())
                    .build();
		}

		applicationDocumentStoreRepository.delete(keyValueFromDB);

		return ServiceResponseBuilder.<ApplicationDocumentStore>ok()
				.withMessage(Messages.APP_DOCUMENT_REMOVED_SUCCESSFULLY.getCode())
				.withResult(keyValueFromDB)
				.build();
	}

    @Override
    public ServiceResponse<ApplicationDocumentStore> findUniqueByTenantApplication(Tenant tenant, Application application,
            String collection, String key) {

        ServiceResponse<ApplicationDocumentStore> validationsResponse = validate(tenant, application, collection, key);
        if (validationsResponse != null && !validationsResponse.isOk()) {
            return validationsResponse;
        }

        ApplicationDocumentStore keyValueFromDB = applicationDocumentStoreRepository.findUniqueByTenantIdApplicationName(
                tenant.getId(),
                application.getName(),
                collection,
                key);

		if (!Optional.ofNullable(keyValueFromDB).isPresent()) {
			return ServiceResponseBuilder.<ApplicationDocumentStore> error()
					.withMessage(Validations.APP_DOCUMENT_DOES_NOT_EXIST.getCode()).build();
		}

		return ServiceResponseBuilder.<ApplicationDocumentStore>ok().withResult(keyValueFromDB).build();

    }

    private <T> ServiceResponse<T> validate(Tenant tenant, Application application, String collection, String key) {

        if (!Optional.ofNullable(tenant).isPresent()) {
            return ServiceResponseBuilder.<T>error().withMessage(CommonValidations.TENANT_NULL.getCode()).build();
        }

        if (!Optional.ofNullable(application).isPresent()) {
            return ServiceResponseBuilder.<T>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()).build();
        }

        if (collection == null || !collection.matches(COLLECTION_KEY_PATTERN)) {
            return ServiceResponseBuilder.<T>error()
                    .withMessage(Validations.APP_DOCUMENT_INVALID_COLLECTION_NAME.getCode())
                    .build();
        }

        if (key == null || !key.matches(COLLECTION_KEY_PATTERN)) {
            return ServiceResponseBuilder.<T>error()
                    .withMessage(Validations.APP_DOCUMENT_INVALID_KEY_NAME.getCode())
                    .build();
        }

        return null;

    }

    private boolean isValidJson(String json) {

        if (StringUtils.isBlank(json)) {
            return false;
        } else {
            try {
                JSON.parse(json);
            } catch (JSONParseException e) {
                return false;
            }
        }

        return true;

    }

}
