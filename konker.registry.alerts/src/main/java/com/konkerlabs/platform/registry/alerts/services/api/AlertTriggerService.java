package com.konkerlabs.platform.registry.alerts.services.api;

import com.konkerlabs.platform.registry.business.model.AlertTrigger;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;

import java.util.List;

public interface AlertTriggerService {

    enum Validations {
        ALERT_TRIGGER_NOT_FOUND("service.alert_trigger.not_found");

        private String code;

        public String getCode() {
            return code;
        }

        Validations(String code) {
            this.code = code;
        }
    }

    ServiceResponse<List<AlertTrigger>> listByTenantAndApplication(Tenant tenant, Application application);
    ServiceResponse<AlertTrigger> findByTenantAndApplicationAndGuid(Tenant tenant, Application application, String triggerGuid);

}
