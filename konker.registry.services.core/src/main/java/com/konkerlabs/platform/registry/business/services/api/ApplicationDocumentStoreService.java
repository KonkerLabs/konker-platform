package com.konkerlabs.platform.registry.business.services.api;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.ApplicationDocumentStore;
import com.konkerlabs.platform.registry.business.model.Tenant;

public interface ApplicationDocumentStoreService {

    public enum Validations {
        APP_DOCUMENT_DATA_NULL("service.application.document_store.null"),
        APP_DOCUMENT_DATA_ALREADY_REGISTERED("service.application.document_store.already_registered"),
        APP_DOCUMENT_DATA_DOES_NOT_EXIST("service.application.document_store.does_not_exist"),
        APP_DOCUMENT_DATA_INVALID_JSON("service.application.document_store.invalid_json");

        public String getCode() {
            return code;
        }

        private String code;

        Validations(String code) {
            this.code = code;
        }
    }

    public enum Messages {
        APP_DOCUMENT_DATA_REMOVED_SUCCESSFULLY("controller.application.document_store.removed_succesfully");

        public String getCode() {
            return code;
        }

        private String code;

        Messages(String code) {
            this.code = code;
        }
    }

    ServiceResponse<ApplicationDocumentStore> save(Tenant tenant, Application application, String collection,
            String key, String jsonCustomData);

    ServiceResponse<ApplicationDocumentStore> update(Tenant tenant, Application application, String collection,
            String key, String jsonCustomData);

    ServiceResponse<ApplicationDocumentStore> remove(Tenant tenant, Application application, String collection,
            String key);

    ServiceResponse<ApplicationDocumentStore> findUniqueByTenantApplication(Tenant tenant, Application application,
            String collection, String key);

}