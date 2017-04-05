package com.konkerlabs.platform.registry.business.services.api;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Tenant;

import java.util.List;

public interface ApplicationService {

    enum Validations {
    	TENANT_NULL("service.tenant.validation.tenant.not_null"),
        NO_EXIST_TENANT("service.tenant.validation.no_exist"),
        APPLICATION_NULL("service.application.null");

        private String code;

        Validations(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    enum Errors {
        ERROR_SAVE_TENANT("service.application.error.detail.save");

        private String code;

        Errors(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

    }

    /**
     * Persists a new Application.
     *
     * If the application name already exists, an error is created. The
     * tenant must exist.
     *
     * @param tenant
     * @param application
     * @return
     */
    ServiceResponse<Application> register(Tenant tenant, Application application);

    /**
     * Updates an already existent Tenant.
     *
     * If the applicationGuid does not exist in this tenant, an error is created. The
     * tenant must exist.
     *
     * @param tenant
     * @param application
     * @return
     */
    ServiceResponse<Application> update(Tenant tenant, String guid, Application application);


    /**
     * Find all Applications by Tenant
     * @param tenant
     * @return List<Application>
     */
    ServiceResponse<List<Application>> findAll(Tenant tenant);


    /**
     * Find a application by Name
     * @param tenant
     * @param applicationId
     * @return
     */
    ServiceResponse<Application> findById(Tenant tenant, String applicationId);


    /**
     * Remove application by id
     * @param tenant
     * @param applicationId
     * @return
     */
    ServiceResponse<Application> remove(Tenant tenant, String applicationId);

}
