package com.konkerlabs.platform.registry.business.services.api;

import java.util.List;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Tenant;

public interface ApplicationService {

	enum Validations {
		APPLICATION_NULL("service.application.null"),
		NAME_NULL_EMPTY("service.application.name.not_null"),
		NAME_INVALID("service.application.name.invalid"),
		FRIENDLY_NAME_NULL_EMPTY("service.application.friendly.name.not_null");

		public String getCode() {
			return code;
		}

		private String code;

		Validations(String code) {
			this.code = code;
		}
	}

   	/**
	 * Persists a new Application.
	 *
	 * If the application name already exists, an error is created. The
	 * tenant must exist.
	 *
	 * @param tenant
	 * @param application
	 * @return
	 */
	ServiceResponse<Application> register(Tenant tenant, Application application);

	/**
	 * Updates an already existent Tenant.
	 *
	 * If the applicationGuid does not exist in this tenant, an error is created. The
	 * tenant must exist.
	 *
	 * @param tenant
	 * @param application
	 * @return
	 */
	ServiceResponse<Application> update(Tenant tenant, String guid, Application application);

	/**
	 * Removes the application that exists in this tenant.
	 *
	 * @param guid
	 * @return ServiceResponse<Application>
	 */
	ServiceResponse<Application> remove(Tenant tenant, String guid);

	/**
	 * Returns all applications owned by the provided tenant.
	 *
	 *
	 * @param tenant
	 * @return
	 */
	ServiceResponse<List<Application>> findAll(Tenant tenant);


	/**
	 * Returns a application by its applicationGuid and tenant.
	 *
	 * If the application does not exist, returns an error
	 *
	 * @param tenant
	 * @param guid
	 * @return
	 */
	ServiceResponse<Application> getByApplicationGuid(Tenant tenant, String guid);


}