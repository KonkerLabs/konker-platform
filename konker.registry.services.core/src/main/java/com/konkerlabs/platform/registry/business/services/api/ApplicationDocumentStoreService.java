package com.konkerlabs.platform.registry.business.services.api;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.ApplicationDocumentStore;
import com.konkerlabs.platform.registry.business.model.Tenant;

public interface ApplicationDocumentStoreService {

	enum Validations {
		DEVICE_CUSTOM_DATA_NULL("service.device.custom_data.null"),
		DEVICE_CUSTOM_DATA_ALREADY_REGISTERED("service.device.custom_data.already_registered"),
		DEVICE_CUSTOM_DATA_DOES_NOT_EXIST("service.device.custom_data.does_not_exist"),
		DEVICE_CUSTOM_DATA_INVALID_JSON("service.device.custom_data.invalid_json");

		public String getCode() {
			return code;
		}

		private String code;

		Validations(String code) {
			this.code = code;
		}
	}

	public enum Messages {
		DEVICE_CUSTOM_DATA_REMOVED_SUCCESSFULLY("controller.device.custom_data.removed_succesfully");

		public String getCode() {
			return code;
		}

		private String code;

		Messages(String code) {
			this.code = code;
		}
	}

	ServiceResponse<ApplicationDocumentStore> save(Tenant tenant, Application application, String document, String key, String jsonCustomData);
	ServiceResponse<ApplicationDocumentStore> update(Tenant tenant, Application application, String document, String key, String jsonCustomData);
	ServiceResponse<ApplicationDocumentStore> remove(Tenant tenant, Application application, String document, String key);
    ServiceResponse<ApplicationDocumentStore> findUniqueByTenantApplication(Tenant tenant, Application application, String document, String key);

}