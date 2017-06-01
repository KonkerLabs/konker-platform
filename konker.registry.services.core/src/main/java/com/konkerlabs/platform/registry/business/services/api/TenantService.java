package com.konkerlabs.platform.registry.business.services.api;

import java.util.List;

import com.konkerlabs.platform.registry.billing.model.TenantDailyUsage;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.enumerations.LogLevel;

public interface TenantService {

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

}
