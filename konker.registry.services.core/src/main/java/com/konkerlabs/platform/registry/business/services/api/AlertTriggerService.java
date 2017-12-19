package com.konkerlabs.platform.registry.business.services.api;

import java.util.List;

import com.konkerlabs.platform.registry.business.model.*;

public interface AlertTriggerService {

    enum Validations {
        ALERT_TRIGGER_NOT_FOUND("service.alert_trigger.not_found"),
        ALERT_TRIGGER_ALREADY_EXISTS("service.alert_trigger.already_exists"),
        ALERT_TRIGGER_GUID_NULL("service.alert_trigger.guid_null"),
        ALERT_TRIGGER_INVALID_DEVICE_MODEL("service.alert_trigger.invalid_device_model"),
        ALERT_TRIGGER_INVALID_LOCATION("service.alert_trigger.invalid_location");

        private String code;

        public String getCode() {
            return code;
        }

        Validations(String code) {
            this.code = code;
        }
    }

    enum Messages {
        SILENCE_TRIGGER_REMOVED_SUCCESSFULLY("service.alert_trigger.removed_succesfully");

        public String getCode() {
            return code;
        }

        private String code;

        Messages(String code) {
            this.code = code;
        }
    }

    ServiceResponse<AlertTrigger> save(Tenant tenant, Application application, AlertTrigger trigger);
    ServiceResponse<AlertTrigger> remove(Tenant tenant, Application application, String guid);
    ServiceResponse<AlertTrigger> update(Tenant tenant, Application application, String guid, AlertTrigger trigger);

    ServiceResponse<List<AlertTrigger>> listByTenantAndApplication(Tenant tenant, Application application);
    ServiceResponse<AlertTrigger> findByTenantAndApplicationAndGuid(Tenant tenant, Application application, String triggerGuid);
    ServiceResponse<AlertTrigger> findByTenantAndApplicationAndName(Tenant tenant, Application application, String triggerName);

}
