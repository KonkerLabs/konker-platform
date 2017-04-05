package com.konkerlabs.platform.registry.business.services.api;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Tenant;

import java.util.List;

public interface ApplicationService {

    enum Validations {
    	TENANT_NULL("service.tenant.validation.tenant.not_null"),
        NO_EXIST_TENANT("service.tenant.validation.no_exist");

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

    ServiceResponse<Application> save(Application application);
    ServiceResponse<List<Application>> findAll(Tenant tenant);
    ServiceResponse<Application> findById(Tenant tenant, String applicationGuid);
    ServiceResponse<Application> remove(Tenant tenant, Application application);

}
