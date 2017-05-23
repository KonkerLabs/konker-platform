package com.konkerlabs.platform.registry.business.services.api;

import java.util.List;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.HealthAlert;
import com.konkerlabs.platform.registry.business.model.Tenant;

public interface HealthAlertService {

	enum Validations {
		HEALTH_ALERT_NULL("service.devicemodel.null"),
		DEVICE_MODEL_ALREADY_REGISTERED("service.devicemodel.already.registered"),
		HEALTH_ALERT_DOES_NOT_EXIST("service.devicemodel.does.not.exist"),
		HEALTH_ALERT_GUID_IS_NULL("service.devicemodel.name.null"),
		DEVICE_MODEL_NOT_FOUND("service.devicemodel.not_found"),
		DEVICE_MODEL_HAS_DEVICE("service.devicemodel.has.device"),
		DEVICE_MODEL_HAS_ROUTE("service.devicemodel.has.route"),
		DEVICE_MODEL_NOT_REMOVED_IS_DEFAULT("service.devicemodel.not.removed.is.default"),
		DEVICE_MODEL_NOT_UPDATED_IS_DEFAULT("service.devicemodel.not.updated.is.default");

		public String getCode() {
			return code;
		}

		private String code;

		Validations(String code) {
			this.code = code;
		}
	}
	
	public enum Messages {
		HEALTH_ALERT_REMOVED_SUCCESSFULLY("controller.devicemodel.removed.succesfully"),
		DEVICE_MODEL_REMOVED_UNSUCCESSFULLY("controller.devicemodel.removed.unsuccesfully");

		public String getCode() {
			return code;
		}

		private String code;

		Messages(String code) {
			this.code = code;
		}
	}

	ServiceResponse<HealthAlert> register(Tenant tenant, Application application, HealthAlert deviceModel);
	ServiceResponse<HealthAlert> update(Tenant tenant, Application application, String healthAlertGuid, HealthAlert deviceModel);
	ServiceResponse<HealthAlert> remove(Tenant tenant, Application application, String healthAlertGuid);
	ServiceResponse<List<HealthAlert>> findAllByTenantAndApplication(Tenant tenant, Application application);
	ServiceResponse<List<HealthAlert>> findAllByTenantApplicationAndDeviceGuid(Tenant tenant, Application application, String deviceGuid);
	ServiceResponse<HealthAlert> getByTenantApplicationAndHealthAlertGuid(Tenant tenant, Application application, String healthAlertGuid);


}