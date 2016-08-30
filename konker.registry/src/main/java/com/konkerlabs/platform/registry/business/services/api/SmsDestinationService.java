package com.konkerlabs.platform.registry.business.services.api;

import com.konkerlabs.platform.registry.business.model.SmsDestination;
import com.konkerlabs.platform.registry.business.model.Tenant;

import java.util.List;

public interface SmsDestinationService {
    enum Validations {
        SMSDEST_ID_NULL("service.smsdest.id.not_null"),
        SMSDEST_NAME_UNIQUE("service.smsdest.name.in_use"),
        SMSDEST_NOT_FOUND("service.smsdest.not_found");

        private String code;

        public String getCode() {
            return code;
        }

        Validations(String code) {
            this.code = code;
        }
    }

    
    
    NewServiceResponse<List<SmsDestination>> findAll(Tenant tenant);

    NewServiceResponse<SmsDestination> register(Tenant tenant, SmsDestination destination);

    NewServiceResponse<SmsDestination> update(Tenant tenant, String guid, SmsDestination destination);

    NewServiceResponse<SmsDestination> getByGUID(Tenant tenant, String guid);

}
