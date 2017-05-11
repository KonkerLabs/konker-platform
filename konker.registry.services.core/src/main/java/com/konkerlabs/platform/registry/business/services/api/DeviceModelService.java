package com.konkerlabs.platform.registry.business.services.api;

import java.util.List;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.DeviceModel;
import com.konkerlabs.platform.registry.business.model.Tenant;

public interface DeviceModelService {

	enum Validations {
		DEVICE_MODEL_NULL("service.devicemodel.null"),
		DEVICE_MODEL_ALREADY_REGISTERED("service.devicemodel.already.registered"),
		DEVICE_MODEL_DOES_NOT_EXIST("service.devicemodel.does.not.exist"),
		DEVICE_MODEL_NAME_IS_NULL("service.devicemodel.name.null"),
		DEVICE_MODEL_NOT_FOUND("service.devicemodel.not_found"),
		DEVICE_MODEL_HAS_DEVICE("service.devicemodel.has.device"),
		DEVICE_MODEL_HAS_ROUTE("service.devicemodel.has.route");

		public String getCode() {
			return code;
		}

		private String code;

		Validations(String code) {
			this.code = code;
		}
	}
	
	public enum Messages {
		DEVICE_MODEL_REMOVED_SUCCESSFULLY("controller.devicemodel.removed.succesfully");

		public String getCode() {
			return code;
		}

		private String code;

		Messages(String code) {
			this.code = code;
		}
	}

   	/**
	 * Persists a new DeviceModel.
	 *
	 * If the device model name already exists, an error is created. The
	 * tenant and application must exist.
	 *
	 * @param tenant
	 * @param application
	 * @param deviceModel
	 * @return
	 */
	ServiceResponse<DeviceModel> register(Tenant tenant, Application application, DeviceModel deviceModel);

	/**
	 * Updates an already existent DeviceModel.
	 *
	 * If the deviceModel does not exist in this tenant and application, an error is created. The
	 * tenant and application must exist.
	 *
	 * @param tenant
	 * @param application
	 * @param deviceModel
	 * @return
	 */
	ServiceResponse<DeviceModel> update(Tenant tenant, Application application, String name, DeviceModel deviceModel);

	/**
	 * Removes the deviceModel that exists in this tenant and application.
	 *
	 * @param tenant
	 * @param application
	 * @param name
	 * @return ServiceResponse<Application>
	 */
	ServiceResponse<DeviceModel> remove(Tenant tenant, Application application, String name);

	/**
	 * Returns all deviceModel owned by the provided tenant and application.
	 *
	 *
	 * @param tenant
	 * @param application
	 * @return
	 */
	ServiceResponse<List<DeviceModel>> findAll(Tenant tenant, Application application);


	/**
	 * Returns a device model by its applicationName and tenant.
	 *
	 * If the device model does not exist, returns an error
	 *
	 * @param tenant
	 * @param application
	 * @param name
	 * @return
	 */
	ServiceResponse<DeviceModel> getByTenantIdApplicationNameAndName(Tenant tenant, Application application, String name);


}