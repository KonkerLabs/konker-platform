package com.konkerlabs.platform.registry.business.services.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.storage.model.PrivateStorage;

import java.util.List;
import java.util.Map;

public interface PrivateStorageService {

    enum Validations {
        PRIVATE_STORAGE_NULL("service.private.storage.null"),
        PRIVATE_STORAGE_IS_FULL("service.private.storage.is.full"),
        PRIVATE_STORAGE_COLLECTION_CONTENT_ALREADY_EXISTS("service.private.storage.collection.content.already_exists"),
        PRIVATE_STORAGE_COLLECTION_CONTENT_DOES_NOT_EXIST("service.private.storage.collection.content.does_not_exist"),
        PRIVATE_STORAGE_INVALID_JSON("service.private.storage.invalid_json"),
        PRIVATE_STORAGE_INVALID_COLLECTION_NAME("service.private.storage.invalid_collection_name"),
        PRIVATE_STORAGE_NO_COLLECTION_ID_FIELD("service.private.storage.no.collection.id.field"),
        PRIVATE_STORAGE_COLLECTION_ID_IS_NULL("service.private.storage.collection.id.null"),
        PRIVATE_STORAGE_INVALID_KEY_NAME("service.private.storage.invalid_key_name");

        public String getCode() {
            return code;
        }

        private String code;

        Validations(String code) {
            this.code = code;
        }
    }

    enum Messages {
        PRIVATE_STORAGE_REMOVED_SUCCESSFULLY("controller.private.storage.removed_succesfully");

        public String getCode() {
            return code;
        }

        private String code;

        Messages(String code) {
            this.code = code;
        }
    }

    ServiceResponse<PrivateStorage> save(Tenant tenant,
                                         Application application,
                                         User user,
                                         String collectionName,
                                         String collectionContent) throws JsonProcessingException;

    ServiceResponse<PrivateStorage> update(Tenant tenant,
                                           Application application,
                                           User user,
                                           String collectionName,
                                           String collectionContent) throws JsonProcessingException;

    ServiceResponse<PrivateStorage> remove(Tenant tenant,
                                           Application application,
                                           User user,
                                           String collectionName,
                                           String id) throws JsonProcessingException;

    ServiceResponse<List<PrivateStorage>> findAll(Tenant tenant,
                                                  Application application,
                                                  User user,
                                                  String collectionName) throws JsonProcessingException;

    ServiceResponse<PrivateStorage> findById(Tenant tenant,
                                             Application application,
                                             User user,
                                             String collectionName,
                                             String id) throws JsonProcessingException;

    ServiceResponse<List<PrivateStorage>> findByQuery(Tenant tenant,
                                                      Application application,
                                                      User user,
                                                      String collectionName,
                                                      Map<String, String> queryParam,
                                                      String sort,
                                                      int pageNumber,
                                                      int pageSize) throws JsonProcessingException;

    ServiceResponse<List<String>> listCollections(Tenant tenant,
                                                 Application application,
                                                 User user);
}