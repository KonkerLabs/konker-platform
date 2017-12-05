package com.konkerlabs.platform.registry.business.services.api;

import com.konkerlabs.platform.registry.billing.model.TenantDailyUsage;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.enumerations.LogLevel;

import java.util.List;

public interface TenantService {

    enum Validations {
    	TENANT_NULL("service.tenant.validation.tenant.not_null"),
        NO_EXIST_TENANT("service.tenant.validation.no_exist"), 
        TENANT_NAME_NULL("service.tenant.validation.tenant.name.not_null");

        private String code;

        Validations(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }	
	
    enum Errors {
        ERROR_SAVE_TENANT("service.tenant.error.detail.save");

        private String code;

        Errors(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

    }
    
	ServiceResponse<Tenant> updateLogLevel(Tenant tenant, LogLevel logLevel);

	ServiceResponse<List<TenantDailyUsage>> findTenantDailyUsage(Tenant tenant);
	
	ServiceResponse<Tenant> save(Tenant tenant);

	ServiceResponse<Tenant> findByDomainName(String name);


}
