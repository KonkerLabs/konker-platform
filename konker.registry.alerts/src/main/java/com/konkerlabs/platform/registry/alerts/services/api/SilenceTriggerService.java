package com.konkerlabs.platform.registry.alerts.services.api;

import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;

import java.util.List;

public interface SilenceTriggerService {

    enum Validations {
        GUID_NULL("service.silence_trigger.guid.not_null"),
        SILENCE_TRIGGER_ALREADY_EXISTS("service.silence_trigger.already_exists"),
        SILENCE_TRIGGER_NOT_FOUND("service.silence_trigger.not_found");

        private String code;

        public String getCode() {
            return code;
        }

        Validations(String code) {
            this.code = code;
        }
    }

    enum Messages {
        SILENCE_TRIGGER_REMOVED_SUCCESSFULLY("service.silence_trigger.removed_succesfully");

        public String getCode() {
            return code;
        }

        private String code;

        Messages(String code) {
            this.code = code;
        }
    }

    ServiceResponse<SilenceTrigger> findByTenantAndApplicationAndModelAndLocation(Tenant tenant, Application application, DeviceModel model, Location location);
    ServiceResponse<List<SilenceTrigger>> listByTenantAndApplication(Tenant tenant, Application application);
    ServiceResponse<SilenceTrigger> save(Tenant tenant, Application application, SilenceTrigger trigger);
    ServiceResponse<SilenceTrigger> update(Tenant tenant, Application application, String guid, SilenceTrigger trigger);
    ServiceResponse<SilenceTrigger> remove(Tenant tenant, Application application, String guid);

}
